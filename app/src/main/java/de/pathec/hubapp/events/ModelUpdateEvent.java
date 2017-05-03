package de.pathec.hubapp.events;

public class ModelUpdateEvent<T> extends ModelBaseEvent<T> {

    public ModelUpdateEvent(String model, T item) {
        super(model, item);
    }
}
