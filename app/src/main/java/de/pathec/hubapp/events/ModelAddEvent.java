package de.pathec.hubapp.events;

public class ModelAddEvent<T> extends ModelBaseEvent<T> {

    public ModelAddEvent(String model, T item) {
        super(model, item);
    }
}
