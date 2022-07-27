package com.github.artbits.quickio;

import java.util.LinkedHashMap;

public class Condition {

    private final LinkedHashMap<String, String[]> map = new LinkedHashMap<>();

    Condition() {

    }

    public Condition $eq(String field, String value) {
        setMap(field, value, "eq");
        return this;
    }

    public Condition $lt(String field, String value) {
        setMap(field, value, "lt");
        return this;
    }

    public Condition $lte(String field, String value) {
        setMap(field, value, "lte");
        return this;
    }

    public Condition $gt(String field, String value) {
        setMap(field, value, "gt");
        return this;
    }

    public Condition $gte(String field, String value) {
        setMap(field, value, "gte");
        return this;
    }

    public Condition $ne(String field, String value) {
        setMap(field, value, "ne");
        return this;
    }

    void setMap(String field, String value, String symbol) {
        map.put(field, new String[]{value, symbol});
    }

    LinkedHashMap<String, String[]> getMap() {
        return map;
    }

}
