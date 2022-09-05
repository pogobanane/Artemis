package de.tum.in.www1.artemis.service.scheduled.distributed.callables.programming;

import java.util.Optional;
import java.util.concurrent.Callable;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

import de.tum.in.www1.artemis.domain.Result;
import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseParticipation;
import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseGradingService;

@SpringAware
public class ProcessNewProgrammingExerciseResult implements Callable<Optional<Result>>, java.io.Serializable {

    private transient ProgrammingExerciseGradingService programmingExerciseGradingService;

    private final ProgrammingExerciseParticipation participation;

    private final Object requestBody;

    public ProcessNewProgrammingExerciseResult(@NotNull ProgrammingExerciseParticipation participation, @NotNull Object requestBody) {
        this.participation = participation;
        this.requestBody = requestBody;
    }

    @Override
    public Optional<Result> call() throws GitAPIException {
        SecurityUtils.setAuthorizationObject();
        return this.programmingExerciseGradingService.processNewProgrammingExerciseResult(participation, requestBody);
    }

    @Autowired
    public void setProgrammingExerciseGradingService(final ProgrammingExerciseGradingService programmingExerciseGradingService) {
        this.programmingExerciseGradingService = programmingExerciseGradingService;
    }
}
