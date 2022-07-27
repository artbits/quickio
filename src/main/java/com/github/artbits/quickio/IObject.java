package com.github.artbits.quickio;

import java.io.Serializable;

public class IObject implements Serializable {

    private long id;

    public long id() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public void save() {
        Operator.save(this);
    }

    public boolean delete() {
        if (id != 0) {
            return Operator.delete(id);
        }
        return false;
    }

}
