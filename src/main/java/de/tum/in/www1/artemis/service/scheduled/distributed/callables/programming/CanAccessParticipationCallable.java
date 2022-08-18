package de.tum.in.www1.artemis.service.scheduled.distributed.callables.programming;

import java.util.concurrent.Callable;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseParticipation;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseParticipationService;

@SpringAware
public class CanAccessParticipationCallable implements Callable<Boolean>, java.io.Serializable {

    private transient ProgrammingExerciseParticipationService programmingExerciseParticipationService;

    private final ProgrammingExerciseParticipation participation;

    public CanAccessParticipationCallable(@NotNull ProgrammingExerciseParticipation participation) {
        this.participation = participation;
    }

    @Override
    public Boolean call() throws GitAPIException {
        return this.programmingExerciseParticipationService.canAccessParticipation(participation);
    }

    @Autowired
    public void setProgrammingExerciseParticipationService(final ProgrammingExerciseParticipationService programmingExerciseParticipationService) {
        this.programmingExerciseParticipationService = programmingExerciseParticipationService;
    }
}
