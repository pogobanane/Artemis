package de.tum.in.www1.artemis.web.rest.errors;

import java.io.Serial;

import de.tum.in.www1.artemis.domain.Course;

// TODO: not sure if this is extending the correct Exception and then using the right method call
public class CourseShortnameAlreadyExistsException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CourseShortnameAlreadyExistsException() {
        super(ErrorConstants.COURSE_SHORTNAME_ALREADY_EXISTS_TYPE, "A course with the same short name already exists. Please choose a different short name.", Course.ENTITY_NAME,
                "courseShortnameAlreadyExists", false);
    }
}
