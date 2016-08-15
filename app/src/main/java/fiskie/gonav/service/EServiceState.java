package fiskie.gonav.service;

public enum EServiceState {
    CONNECTING("Scanner is waiting to start."),
    AVAILABLE("Scanner is ready."),
    ACTIVE("Scanner is searching in the background."),
    KILLED("Scanner has been killed."),
    UNPREPARED("Scanner has not been started."),
    NO_PROVIDER("Scanner cannot run because there is no authentication provider."),
    AUTH_FAILURE_LOGIN_FAILED("Scanner could not start because of a general authentication failure."),
    AUTH_FAILURE_UNKNOWN("Scanner could not start because of an unknown failure during authentication."),
    REMOTE_NETWORK_FAILURE("Scanner is not running because of a remote network failure.");

    String descriptor;

    EServiceState(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
