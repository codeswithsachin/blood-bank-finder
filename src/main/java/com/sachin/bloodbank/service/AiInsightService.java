package com.sachin.bloodbank.service;

import com.sachin.bloodbank.model.BloodBank;
import com.sachin.bloodbank.model.BloodGroup;
import com.sachin.bloodbank.model.BloodRequest;
import com.sachin.bloodbank.repository.BloodBankRepository;
import com.sachin.bloodbank.repository.BloodRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Rule-based ("expert system" style, not a live LLM call — see
 * PROJECT_CONTEXT.md) analysis of current stock and demand, surfaced on the
 * admin dashboard as "AI Insights". Every rule here is a simple, explainable
 * threshold check — intentionally so it stays auditable and demo-safe.
 */
@Service
public class AiInsightService {
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final BloodBankRepository bloodBanks;
    private final BloodRequestRepository requests;
    private final DonorService donorService;

    public AiInsightService(BloodBankRepository bloodBanks, BloodRequestRepository requests,
                             DonorService donorService) {
        this.bloodBanks = bloodBanks;
        this.requests = requests;
        this.donorService = donorService;
    }

    public record Insight(String level, String message) {
    }

    public List<Insight> generate() {
        List<Insight> insights = new ArrayList<>();

        // Rule 1: aggregate stock per blood group across all banks — flag low totals.
        Map<BloodGroup, Integer> totals = new EnumMap<>(BloodGroup.class);
        for (BloodBank bank : bloodBanks.findAll()) {
            totals.merge(bank.getBloodGroup(), bank.getUnitsAvailable(), Integer::sum);
        }
        for (BloodGroup group : BloodGroup.values()) {
            int total = totals.getOrDefault(group, 0);
            if (total == 0) {
                insights.add(new Insight("critical", "No recorded stock at all for " + group.getSymbol() + ", consider an urgent donor drive."));
            } else if (total < LOW_STOCK_THRESHOLD) {
                insights.add(new Insight("warning", "Low stock: only " + total + " unit(s) of " + group.getSymbol() + " across all listed blood banks."));
            }
        }

        // Rule 1b: flag stock entries nearing their expiry date (within 7 days).
        LocalDate soon = LocalDate.now().plusDays(7);
        for (BloodBank bank : bloodBanks.findAll()) {
            if (bank.getExpiryDate() != null && !bank.getExpiryDate().isAfter(soon)) {
                boolean alreadyExpired = bank.getExpiryDate().isBefore(LocalDate.now());
                insights.add(new Insight(alreadyExpired ? "critical" : "warning",
                        bank.getUnitsAvailable() + " unit(s) of " + bank.getBloodGroup().getSymbol()
                                + " at " + bank.getName() + (alreadyExpired ? " have expired (" : " expire on (")
                                + bank.getExpiryDate() + ") and should be reviewed for use or discard."));
            }
        }

        // Rule 2: open requests with zero currently-eligible matching donors.
        for (BloodRequest request : requests.findByStatusOrderByCreatedAtDesc(com.sachin.bloodbank.model.RequestStatus.OPEN)) {
            long eligible = donorService.search(request.getBloodGroup(), request.getCity()).size();
            if (eligible == 0) {
                insights.add(new Insight("critical",
                        "Open request for " + request.getBloodGroup().getSymbol() + " in " + request.getCity()
                                + " has no currently eligible registered donors, needs external outreach."));
            }
        }

        if (insights.isEmpty()) {
            insights.add(new Insight("info", "No urgent issues detected right now. Stock and demand look balanced."));
        }

        return insights;
    }
}
