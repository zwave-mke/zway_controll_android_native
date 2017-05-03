package de.pathec.hubapp.events;

import de.pathec.hubapp.model.hub.HubItem;

public class ConnectionEvent {
    Integer mType;
    Integer mStatus;
    String mMessage;
    HubItem mHubItem;

    public ConnectionEvent(Integer type, Integer status, String message, HubItem hubItem) {
        mType = type;
        mStatus = status;
        mMessage = message;
        mHubItem = hubItem;
    }

    public Integer getType() {
        return mType;
    }

    public Integer getStatus() { return mStatus; }

    public String getMessage() {
        return mMessage;
    }

    public HubItem getHubItem() { return mHubItem; }

    @Override
    public String toString() {
        return "ConnectionEvent{" +
                "mType=" + mType +
                ", mStatus=" + mStatus +
                ", mMessage='" + mMessage + '\'' +
                ", mHubItem=" + mHubItem.toString() +
                '}';
    }
}
