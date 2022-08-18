package de.tum.in.www1.artemis.service.scheduled.distributed.callables.programming;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseStudentParticipation;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseParticipationService;

public class FindStudentParticipationByExerciseAndStudentIdCallable implements Callable<ProgrammingExerciseStudentParticipation>, java.io.Serializable {

    private transient ProgrammingExerciseParticipationService programmingExerciseParticipationService;

    private final Exercise exercise;

    private final String username;

    public FindStudentParticipationByExerciseAndStudentIdCallable(Exercise exercise, String username) {
        this.exercise = exercise;
        this.username = username;
    }

    @Override
    public ProgrammingExerciseStudentParticipation call() {
        return programmingExerciseParticipationService.findStudentParticipationByExerciseAndStudentId(exercise, username);
    }

    @Autowired
    public void setProgrammingExerciseParticipationService(final ProgrammingExerciseParticipationService programmingExerciseParticipationService) {
        this.programmingExerciseParticipationService = programmingExerciseParticipationService;
    }
}
