package de.pathec.hubapp.events;

public class RemoveGeofenceEvent {
    private Boolean mStatus;

    public RemoveGeofenceEvent(Boolean status) {
        mStatus = status;
    }

    public Boolean getStatus() {
        return mStatus;
    }
}
