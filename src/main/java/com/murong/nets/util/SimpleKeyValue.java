package com.murong.nets.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yaochuang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleKeyValue<K, V> {
    private K key;
    private V value;
}
