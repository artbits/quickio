package com.github.artbits.quickio;

import java.io.Serializable;

final class Data implements Serializable {

    String model;
    Object object;

    Data(Object object) {
        model = object.getClass().getSimpleName();
        this.object = object;
    }

}
