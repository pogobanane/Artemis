package de.tum.in.www1.artemis.service.feedback;

import de.tum.in.www1.artemis.domain.Feedback;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTO;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTOType;

public class FeedbackDTOServiceImpl implements FeedbackDTOService {

    @Override
    public FeedbackDTO createFromFeedback(Feedback feedback) {
        return new FeedbackDTO(FeedbackDTOType.Reviewer, "artemisApp.result.detail.feedback", feedback.getText(), feedback.getDetailText(), feedback.isPositive(),
                feedback.getCredits());
    }
}
