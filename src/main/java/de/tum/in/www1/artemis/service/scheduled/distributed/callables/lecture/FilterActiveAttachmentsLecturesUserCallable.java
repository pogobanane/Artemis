package de.tum.in.www1.artemis.service.scheduled.distributed.callables.lecture;

import java.util.Set;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.spring.context.SpringAware;

import de.tum.in.www1.artemis.domain.Lecture;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.service.LectureService;

@SpringAware
public class FilterActiveAttachmentsLecturesUserCallable implements Callable<Set<Lecture>>, java.io.Serializable {

    private transient LectureService lectureService;

    private final Set<Lecture> lectures;

    private final User user;

    public FilterActiveAttachmentsLecturesUserCallable(Set<Lecture> lectures, User user) {
        this.lectures = lectures;
        this.user = user;
    }

    @Override
    public Set<Lecture> call() throws Exception {
        return lectureService.filterActiveAttachments(lectures, user);
    }

    @Autowired
    public void setLectureService(final LectureService lectureService) {
        this.lectureService = lectureService;
    }
}
