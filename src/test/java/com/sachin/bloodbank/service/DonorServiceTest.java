package com.sachin.bloodbank.service;

import com.sachin.bloodbank.model.BloodGroup;
import com.sachin.bloodbank.model.Donor;
import com.sachin.bloodbank.repository.DonationRecordRepository;
import com.sachin.bloodbank.repository.DonorRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DonorServiceTest {

    private final DonorRepository repository = mock(DonorRepository.class);
    private final DonationRecordRepository donationRecords = mock(DonationRecordRepository.class);
    private final DonorService service = new DonorService(repository, donationRecords, 90);

    private Donor donorWithLastDonation(LocalDate date) {
        Donor donor = new Donor();
        donor.setName("Test Donor");
        donor.setBloodGroup(BloodGroup.O_POSITIVE);
        donor.setCity("Vadodara");
        donor.setPhone("+919999999999");
        donor.setLastDonationDate(date);
        donor.setActive(true);
        return donor;
    }

    @Test
    void excludesDonorWhoDonatedWithinGapWindow() {
        Donor recentDonor = donorWithLastDonation(LocalDate.now().minusDays(10));
        when(repository.findByBloodGroupAndCityIgnoreCaseAndActiveTrue(BloodGroup.O_POSITIVE, "Vadodara"))
                .thenReturn(List.of(recentDonor));

        List<Donor> eligible = service.search(BloodGroup.O_POSITIVE, "Vadodara");

        assertThat(eligible).isEmpty();
    }

    @Test
    void includesDonorPastTheGapWindow() {
        Donor eligibleDonor = donorWithLastDonation(LocalDate.now().minusDays(120));
        when(repository.findByBloodGroupAndCityIgnoreCaseAndActiveTrue(BloodGroup.O_POSITIVE, "Vadodara"))
                .thenReturn(List.of(eligibleDonor));

        List<Donor> eligible = service.search(BloodGroup.O_POSITIVE, "Vadodara");

        assertThat(eligible).hasSize(1);
    }

    @Test
    void includesDonorWhoHasNeverDonated() {
        Donor newDonor = donorWithLastDonation(null);
        when(repository.findByBloodGroupAndCityIgnoreCaseAndActiveTrue(BloodGroup.O_POSITIVE, "Vadodara"))
                .thenReturn(List.of(newDonor));

        List<Donor> eligible = service.search(BloodGroup.O_POSITIVE, "Vadodara");

        assertThat(eligible).hasSize(1);
    }
}
