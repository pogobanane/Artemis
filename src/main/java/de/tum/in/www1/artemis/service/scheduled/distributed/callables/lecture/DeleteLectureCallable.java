package de.tum.in.www1.artemis.service.scheduled.distributed.callables.lecture;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

import de.tum.in.www1.artemis.domain.Lecture;
import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.LectureService;

@SpringAware
public class DeleteLectureCallable implements Callable<Void>, java.io.Serializable {

    private transient LectureService lectureService;

    private final Lecture lecture;

    public DeleteLectureCallable(Lecture lecture) {
        this.lecture = lecture;
    }

    @Override
    public Void call() {
        SecurityUtils.setAuthorizationObject();
        lectureService.delete(lecture);
        return null;
    }

    @Autowired
    public void setLectureService(final LectureService lectureService) {
        this.lectureService = lectureService;
    }
}
