package de.tum.in.www1.artemis.service.iris.exception;

import java.util.Map;

public class IrisParseResponseException extends IrisException {

    public IrisParseResponseException(Throwable cause) {
        super("artemisApp.iris.error.parseResponse", Map.of("cause", cause.getMessage()));
    }
}
