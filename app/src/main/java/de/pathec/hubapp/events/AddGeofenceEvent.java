package de.pathec.hubapp.events;

public class AddGeofenceEvent {
    private Boolean mStatus;

    public AddGeofenceEvent(Boolean status) {
        mStatus = status;
    }

    public Boolean getStatus() {
        return mStatus;
    }
}
