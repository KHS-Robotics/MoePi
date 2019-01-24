package com.moe365.moepi.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentIntKeyMap<V> implements ConcurrentMap<Integer, V>{
	private static final int MAX_CAPACITY = 1<<30;
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	private int[] keys;
	private V[] values;
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean containsKey(int key) {
		// TODO Auto-generated method stub
				return false;
	}
	@Override
	public boolean containsKey(Object key) {
		return containsKey((int)(Integer)key);
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	public V get(int key) {
		//TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public V get(Object key) {
		return get((int)(Integer)key);
	}
	
	public V put(int key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V put(Integer key, V value) {
		return put((int)key, value);
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends V> m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Integer> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<Integer, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V putIfAbsent(Integer key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object key, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean replace(Integer key, V oldValue, V newValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public V replace(Integer key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

}
