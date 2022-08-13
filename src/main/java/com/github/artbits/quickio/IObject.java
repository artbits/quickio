package com.github.artbits.quickio;

import java.io.Serializable;

public class IObject implements Serializable {

    long id;

    public final long id() {
        return id;
    }

    public final long timestamp() {
        return Snowflake.timestamp(id);
    }

}
