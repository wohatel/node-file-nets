package com.murong.nets.util;

import lombok.Data;

/**
 * @author yaochuang
 */
@Data
public class KeyValue<K, V, D> {
    private K key;
    private V value;
    private D data;

    public KeyValue() {

    }

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public KeyValue(K key, V value, D data) {
        this.key = key;
        this.value = value;
        this.data = data;
    }

}