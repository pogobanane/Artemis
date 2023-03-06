package de.tum.in.www1.artemis.service.feedback;

import de.tum.in.www1.artemis.domain.Feedback;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTO;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTOType;

public class FeedbackDTOServiceImpl implements FeedbackDTOService {

    public FeedbackDTO createFromFeedback(Feedback feedback) {
        var feedbackDTO = (FeedbackDTO) feedback;
        return feedbackDTO.feedbackDTOType(FeedbackDTOType.Reviewer);
    }
}
