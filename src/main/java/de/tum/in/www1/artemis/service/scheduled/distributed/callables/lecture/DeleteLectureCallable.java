package de.tum.in.www1.artemis.service.scheduled.distributed.callables.lecture;

import com.hazelcast.spring.context.SpringAware;
import de.tum.in.www1.artemis.domain.Lecture;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.LectureService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Set;
import java.util.concurrent.Callable;

@SpringAware
public class  DeleteLectureCallable implements Callable<Void>, java.io.Serializable {

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
