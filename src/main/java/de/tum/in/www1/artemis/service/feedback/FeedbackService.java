package de.tum.in.www1.artemis.service.feedback;

import java.util.List;

import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.Feedback;
import de.tum.in.www1.artemis.domain.Result;
import de.tum.in.www1.artemis.domain.enumeration.ExerciseType;
import de.tum.in.www1.artemis.service.ResultService;
import de.tum.in.www1.artemis.web.rest.dto.FeedbackDTO;

public class FeedbackService {

    private final ResultService resultService;

    public FeedbackService(ResultService resultService) {
        this.resultService = resultService;
    }

    public List<FeedbackDTO> getByResult(Result result) {
        List<Feedback> feedbacks = resultService.getFeedbacksForResult(result);
        var feedbackDTOService = getFeedbackDTOServiceByExerciseType(result.getParticipation().getExercise());

        return feedbacks.stream().map(feedbackDTOService::createFromFeedback).toList();
    }

    private FeedbackDTOService getFeedbackDTOServiceByExerciseType(Exercise exercise) {
        if (exercise.getExerciseType() == ExerciseType.PROGRAMMING) {
            return new ProgrammingFeedbackDTOService();
        }

        return new FeedbackDTOServiceImpl();
    }
}
