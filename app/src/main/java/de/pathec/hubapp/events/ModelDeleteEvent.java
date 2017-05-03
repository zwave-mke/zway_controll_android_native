package de.pathec.hubapp.events;

public class ModelDeleteEvent<T> extends ModelBaseEvent<T> {

    public ModelDeleteEvent(String model, T item) {
        super(model, item);
    }
}
