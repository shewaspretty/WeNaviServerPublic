package com.hrlee.transnaviserver.springboot.util.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ArrayListMapElement<K, V> {

    private K key;
    private V value;
}
