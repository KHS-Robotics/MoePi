package com.moe365.moepi.net.packet;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(CLASS)
@Target(TYPE)
public @interface PacketTypeCode {
	public static final int SERVER_HELLO = 0,
			CLIENT_HELLO = 1,
			ERROR = 2,
			ACK = 3,
			CHANNEL_ENUMERATION_REQUEST = 4,
			CHANNEL_ENUMERATION = 5,
			CHANNEL_SUBSCRIBE = 6,
			CHANNEL_UNSUBSCRIBE = 7,
			CHANNEL_CLOSE = 8,
			CHANNEL_METADATA_REQUEST = 9,
			CHANNEL_METADATA = 10,
			PROPERTY_ENUMERATION_REQUEST = 11,
			PROPERTY_ENUMERATION = 12,
			PROPERTY_VALUES_REQUEST = 13,
			PROPERTY_VALUES = 14,
			PROPERTY_INFO_REQUEST = 15,
			PROPERTY_INFO = 16,
			STREAM_META = 17,
			STREAM_FRAME = 18
			;
	int value();
}
