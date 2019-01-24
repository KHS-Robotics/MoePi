package com.moe365.moepi.net.channel;

/**
 * An integer mapping for the reason codes given when websockets close
 * 
 * @author mailmindlin
 */
public enum UnsubscriptionReason {
	/**
	 * Disconnect because
	 */
	NETWORK_DISCONNECT,
	/**
	 * The disconnect completed as a result of an intentional action, such as a
	 * browser close, or a disconnect button push
	 */
	OK,
	/**
	 * The disconnect happened immediately after the client/server async
	 * handshake, when one party decided that it could not support the protocol
	 * offered by the other.
	 */
	INVALID_PROTOCOL,;
}
