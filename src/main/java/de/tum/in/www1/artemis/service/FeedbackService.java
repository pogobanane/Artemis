package de.tum.in.www1.artemis.service;

import java.util.List;

import de.tum.in.www1.artemis.domain.Feedback;
import de.tum.in.www1.artemis.domain.Result;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTO;

public class FeedbackService {

    private final ResultService resultService;

    public FeedbackService(ResultService resultService) {
        this.resultService = resultService;
    }

    public List<FeedbackDTO> getByResult(Result result) {
        List<Feedback> feedbacks = resultService.getFeedbacksForResult(result);

        // TODO: Add parsing of feedback to feedbackDTO i.e. feedbackItem.service.ts:create
        return feedbacks.stream().map(feedback -> new FeedbackDTO(null, null, null)).toList();
    }
}
