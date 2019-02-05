package com.moe365.moepi.client.packets;

import java.nio.ByteBuffer;

/**
 * An abstract
 * 
 * @author mailmindlin
 */
public interface RioPacket {
	
	/**
	 * Denotes a packet that should be ignored. No idea why we would need to use
	 * this, though.
	 */
	public static final short STATUS_NOP = 0;

	/**
	 * Denotes a packet telling the Rio that no target(s) were found.
	 */
	public static final short STATUS_NONE_FOUND = 1;

	/**
	 * Denotes a packet telling the Rio that one target has been detected.
	 */
	public static final short STATUS_ONE_FOUND = 2;

	/**
	 * Denotes a packet telling the Rio that two targets have been
	 * found.
	 */
	public static final short STATUS_TWO_FOUND = 3;

	/**
	 * Denotes a packet telling the Rio that three targets have been
	 * found.
	 */
	public static final short STATUS_THREE_FOUND = 4;

	/**
	 * Denotes a packet telling the Rio that four targets have been
	 * found.
	 */
	public static final short STATUS_FOUR_FOUND = 5;

	/**
	 * Denotes a packet telling the Rio that five targets have been
	 * found.
	 */
	public static final short STATUS_FIVE_FOUND = 6;

	/**
	 * Denotes a packet telling the Rio that six targets have been
	 * found.
	 */
	public static final short STATUS_SIX_FOUND = 7;
	
	// Statuses >= 0x8000 are special metadata things, and shouldn't be
	// discarded, ever
	/**
	 * A packet that contains an error message
	 */
	public static final short STATUS_ERROR = (short) 0x8000;
	/**
	 * A packet that notifies the reciever that the sender has just connected.
	 * If this packet is recieved, the reciever should reset its last-recieved
	 * packet id to the id of this packet.
	 */
	public static final short STATUS_HELLO_WORLD = (short) 0x8001;
	/**
	 * Signals that the sender is terminating in an expected manner.
	 */
	public static final short STATUS_GOODBYE = (short) 0x8002;
	
	/**
	 * Get the status code for this packet
	 * 
	 * @return this packet's status
	 */
	int getStatus();
	
	/**
	 * Get the length (in bytes) of the payload
	 * 
	 * @return this packet's payload length
	 */
	int getLength();
	
	/**
	 * Write payload to buffer
	 * 
	 * @param buffer
	 *            Buffer to write this packet's payload to
	 */
	void writeTo(ByteBuffer buffer);
}