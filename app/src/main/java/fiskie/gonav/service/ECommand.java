package fiskie.gonav.service;

public enum ECommand {
    SERVICE_START("Start the background service."),
    SERVICE_STOP("Stop the background service."),
    SERVICE_STATUS("Ping the status of the background service."),
    INVOKE_SCAN("Invoke a manual scan."),
    BROADCAST_CURRENT_ENCOUNTERS("Request an intent containing a list of active Pokémon sightings.");

    String description;

    ECommand(String description) {
        this.description = description;
    }
}
