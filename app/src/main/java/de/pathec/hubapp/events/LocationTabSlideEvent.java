package de.pathec.hubapp.events;

public class LocationTabSlideEvent {
    private Integer mDirection;

    public LocationTabSlideEvent(Integer direction) {
        mDirection = direction;
    }

    public Integer getDirection() {
        return mDirection;
    }
}
