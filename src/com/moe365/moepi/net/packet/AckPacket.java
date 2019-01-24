package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

/**
 * <p>
 * A packet that acknowledges that another has recieved, explicitly declaring
 * that no error has occurred as a result of any action invoked by the previous
 * packet.
 * </p>
 * No <var>ACK</var> packet should be used to reply to an ACK packet.
 * 
 * @author mailmindlin
 */
@PacketTypeCode(PacketTypeCode.ACK)
public class AckPacket extends AbstractMutableDataPacket {
	public AckPacket() {
		super();
		this.setTypeCode(PacketTypeCode.ACK);
	}

	public AckPacket(int ackId) {
		super();
		this.setAckId(ackId);
		this.setTypeCode(PacketTypeCode.ACK);
	}
	
	public AckPacket(ByteBuffer buf) {
		super(buf);
	}

	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH;
	}
}