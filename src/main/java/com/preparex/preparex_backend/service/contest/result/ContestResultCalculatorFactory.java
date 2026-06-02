package com.preparex.preparex_backend.service.contest.result;

import com.preparex.preparex_backend.enums.ContestType;
import com.preparex.preparex_backend.repository.ContestProblemRepository;
import com.preparex.preparex_backend.repository.ContestRegistrationRepository;
import com.preparex.preparex_backend.repository.ContestSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory that returns the correct ContestResultCalculator based on ContestType.
 */
@Component
@RequiredArgsConstructor
public class ContestResultCalculatorFactory {

    private final ContestSubmissionRepository submissionRepository;
    private final ContestRegistrationRepository registrationRepository;
    private final ContestProblemRepository problemRepository;

    public ContestResultCalculator getCalculator(ContestType type) {
        return switch (type) {
            case JEE_MAINS_MOCK, NEET_MOCK, SUBJECT_WISE ->
                    new JeeMainsCalculator(submissionRepository, registrationRepository, problemRepository);
            case JEE_ADVANCED_MOCK ->
                    new JeeAdvancedCalculator(submissionRepository, registrationRepository, problemRepository);
        };
    }
}
