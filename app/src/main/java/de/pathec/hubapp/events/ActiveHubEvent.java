package de.pathec.hubapp.events;

public class ActiveHubEvent {
    private Integer mActiveHubId;

    public ActiveHubEvent(Integer activeHubId) {
        mActiveHubId = activeHubId;
    }

    public Integer getActiveHubId() {
        return mActiveHubId;
    }
}
