package de.tum.in.www1.artemis.service.scheduled.distributed.callables.programming;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import de.tum.in.www1.artemis.service.programming.ProgrammingExerciseService;

@SpringAware
public class PreCheckProjectExistsOnVCSOrCICallable implements Callable<Boolean>, java.io.Serializable {

    private transient ProgrammingExerciseService programmingExerciseService;

    private final ProgrammingExercise programmingExercise;

    private final String targetCourseShortName;

    public PreCheckProjectExistsOnVCSOrCICallable(ProgrammingExercise programmingExercise, String targetCourseShortName) {
        this.programmingExercise = programmingExercise;
        this.targetCourseShortName = targetCourseShortName;
    }

    @Override
    public Boolean call() {
        return programmingExerciseService.preCheckProjectExistsOnVCSOrCI(programmingExercise, targetCourseShortName);
    }

    @Autowired
    public void setProgrammingExerciseService(final ProgrammingExerciseService programmingExerciseService) {
        this.programmingExerciseService = programmingExerciseService;
    }
}
