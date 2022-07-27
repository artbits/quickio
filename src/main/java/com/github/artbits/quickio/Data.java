package com.github.artbits.quickio;

import java.io.Serializable;

final class Data implements Serializable {

    private final String model;
    private final Object object;

    Data(Object object) {
        model = object.getClass().getName();
        this.object = object;
    }

    public String getModel() {
        return model;
    }

    public Object getObject() {
        return object;
    }

}
