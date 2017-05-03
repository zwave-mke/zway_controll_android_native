package de.pathec.hubapp.events;

abstract class ModelBaseEvent<T> {
    private String mModel;
    private T mItem;

    ModelBaseEvent(String model, T item) {
        mModel = model;
        mItem = item;
    }

    public String getModel() {
        return mModel;
    }

    public T getItem() {
        return mItem;
    }
}
