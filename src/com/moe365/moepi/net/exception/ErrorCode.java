package com.moe365.moepi.net.exception;

public enum ErrorCode {
	INVALID_CHANNEL,
	UNKNOWN_PACKET_TYPE,
	MALFORMED_PACKET,
	INTERNAL_ERROR,
	NOT_SUBSCRIBED_TO_CHANNEL,
	//Differs from UNKNOWN_'' b/c this one knows what type of packet it is, but just doesn't like it.
	ILLEGAL_PACKET_TYPE,
}