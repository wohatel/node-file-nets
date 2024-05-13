package com.murong.nets.util;

import lombok.Data;

/**
 * @author yaochuang
 */
@Data
public class KeyValueData<K, V, D> {
    private K key;
    private V value;
    private D data;

    private String other;

    public KeyValueData() {

    }

    public KeyValueData(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public KeyValueData(K key, V value, D data) {
        this.key = key;
        this.value = value;
        this.data = data;
    }

    public KeyValueData(K key, V value, D data, String other) {
        this.key = key;
        this.value = value;
        this.data = data;
        this.other = other;
    }

}
