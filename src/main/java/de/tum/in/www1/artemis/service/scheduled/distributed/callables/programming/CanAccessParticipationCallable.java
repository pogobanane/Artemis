package de.tum.in.www1.artemis.service.scheduled.distributed.callables.programming;

import java.util.concurrent.Callable;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hazelcast.spring.context.SpringAware;

import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseParticipation;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseParticipationService;

@SpringAware
public class CanAccessParticipationCallable implements Callable<Boolean>, java.io.Serializable {

    private transient ProgrammingExerciseParticipationService programmingExerciseParticipationService;

    private final SecurityContext securityContext;

    private final ProgrammingExerciseParticipation participation;

    public CanAccessParticipationCallable(SecurityContext securityContext, @NotNull ProgrammingExerciseParticipation participation) {
        this.securityContext = securityContext;
        this.participation = participation;
    }

    @Override
    public Boolean call() throws GitAPIException {
        SecurityContextHolder.setContext(securityContext);
        return this.programmingExerciseParticipationService.canAccessParticipation(participation);
    }

    @Autowired
    public void setProgrammingExerciseParticipationService(final ProgrammingExerciseParticipationService programmingExerciseParticipationService) {
        this.programmingExerciseParticipationService = programmingExerciseParticipationService;
    }
}
