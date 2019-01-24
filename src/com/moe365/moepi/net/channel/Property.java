package com.moe365.moepi.net.channel;

import java.util.List;
import java.util.function.ObjIntConsumer;

public interface Property {
	PropertyType getType();
	String getName();
	int getId();
	int get();
	int set(int value);
	int getMax();
	int getMin();
	int getStep();
	List<String> getValues();
	void onUpdate(ObjIntConsumer<Property> handler);
}
