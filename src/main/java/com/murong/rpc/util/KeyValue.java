package com.murong.rpc.util;

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

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }
}
