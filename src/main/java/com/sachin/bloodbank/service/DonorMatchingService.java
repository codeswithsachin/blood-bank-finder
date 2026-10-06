package com.sachin.bloodbank.service;

import com.sachin.bloodbank.model.BloodGroup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Matches a needed blood group against every medically compatible donor
 * group (not just an exact match), using the real transfusion-compatibility
 * matrix in {@link BloodGroup}, and ranks the results with a simple weighted
 * score.
 *
 * Deliberately returns only aggregate counts per compatible blood group —
 * never individual donor names, phone numbers, or records — because this
 * engine is exposed on a public, unauthenticated page (/find-donors). A
 * donor's personal contact details should only ever reach a genuine
 * requester through the existing request/notification flow
 * (BloodRequestService), not be listable by anyone who visits the site.
 */
@Service
public class DonorMatchingService {
    private final DonorService donorService;

    public DonorMatchingService(DonorService donorService) {
        this.donorService = donorService;
    }

    public record CompatibilityMatch(BloodGroup donorGroup, long eligibleCount, int matchScore, String reason) {
    }

    /** All donor groups that can safely donate to the given recipient group. */
    private Set<BloodGroup> compatibleDonorGroupsFor(BloodGroup recipient) {
        Set<BloodGroup> result = EnumSet.noneOf(BloodGroup.class);
        for (BloodGroup g : BloodGroup.values()) {
            if (g.canDonateTo(recipient)) {
                result.add(g);
            }
        }
        return result;
    }

    public List<CompatibilityMatch> findCompatibleSummary(BloodGroup neededGroup, String city) {
        List<CompatibilityMatch> results = new ArrayList<>();

        for (BloodGroup donorGroup : compatibleDonorGroupsFor(neededGroup)) {
            long eligibleCount = donorService.search(donorGroup, city).size();
            if (eligibleCount == 0) continue;

            int score;
            String reason;
            if (donorGroup == neededGroup) {
                score = 100;
                reason = "Exact blood group match";
            } else if (donorGroup == BloodGroup.O_NEGATIVE) {
                score = 90;
                reason = "Universal donor, compatible with every blood group";
            } else {
                score = 70;
                reason = "Medically compatible donor group";
            }

            results.add(new CompatibilityMatch(donorGroup, eligibleCount, score, reason));
        }

        results.sort((a, b) -> Integer.compare(b.matchScore(), a.matchScore()));
        return results;
    }
}
