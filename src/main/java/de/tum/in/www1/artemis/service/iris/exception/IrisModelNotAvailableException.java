package de.tum.in.www1.artemis.service.iris.exception;

import java.util.Map;

public class IrisModelNotAvailableException extends IrisException {

    public IrisModelNotAvailableException(String model, String pyrisErrorMessage) {
        super("artemisApp.iris.error.noModelAvailable", Map.of("model", model, "pyrisErrorMessage", pyrisErrorMessage));
    }
}
