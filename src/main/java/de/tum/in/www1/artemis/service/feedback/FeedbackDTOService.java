package de.tum.in.www1.artemis.service.feedback;

import de.tum.in.www1.artemis.domain.Feedback;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTO;

public interface FeedbackDTOService {

    FeedbackDTO createFromFeedback(Feedback feedback);
}
