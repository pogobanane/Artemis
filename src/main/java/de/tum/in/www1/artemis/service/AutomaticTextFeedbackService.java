package de.tum.in.www1.artemis.service;

import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.tum.in.www1.artemis.domain.Result;
import de.tum.in.www1.artemis.domain.TextSubmission;
import de.tum.in.www1.artemis.repository.FeedbackRepository;
import de.tum.in.www1.artemis.repository.TextBlockRepository;

@Service
@Profile("athena")
public class AutomaticTextFeedbackService {

    private final FeedbackRepository feedbackRepository;

    private final TextBlockRepository textBlockRepository;

    public AutomaticTextFeedbackService(FeedbackRepository feedbackRepository, TextBlockRepository textBlockRepository) {
        this.feedbackRepository = feedbackRepository;
        this.textBlockRepository = textBlockRepository;
    }

    /**
     * Suggest Feedback for a Submission.
     * Otherwise, an empty Feedback Element is created for simplicity.
     * Feedbacks are stored inline with the provided Result object.
     *
     * @param result Result for the Submission
     */
    @Transactional(readOnly = true) // TODO: remove transactional
    public void suggestFeedback(@NotNull Result result) {
        final TextSubmission textSubmission = (TextSubmission) result.getSubmission();
        final var blocks = textBlockRepository.findAllBySubmissionId(textSubmission.getId());
        textSubmission.setBlocks(blocks);
        // ...
    }

}
