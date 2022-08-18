package de.tum.in.www1.artemis.service.scheduled.distributed.callables.programming;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseService;

@SpringAware
public class DeleteProgrammingExerciseCallable implements Callable<Void>, java.io.Serializable {

    private transient ProgrammingExerciseService programmingExerciseService;

    private final Long programmingExerciseId;

    private final Boolean deleteBaseReposBuildPlans;

    public DeleteProgrammingExerciseCallable(Long programmingExerciseId, Boolean deleteBaseReposBuildPlans) {
        this.programmingExerciseId = programmingExerciseId;
        this.deleteBaseReposBuildPlans = deleteBaseReposBuildPlans;
    }

    @Override
    public Void call() {
        SecurityUtils.setAuthorizationObject();
        programmingExerciseService.delete(programmingExerciseId, deleteBaseReposBuildPlans);
        return null;
    }

    @Autowired
    public void setProgrammingExerciseService(final ProgrammingExerciseService programmingExerciseService) {
        this.programmingExerciseService = programmingExerciseService;
    }
}
