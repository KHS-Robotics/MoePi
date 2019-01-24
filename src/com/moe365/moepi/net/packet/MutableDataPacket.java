package com.moe365.moepi.net.packet;

public interface MutableDataPacket extends DataPacket {
	MutableDataPacket setId(int id);
	MutableDataPacket setAckId(int ack);
	MutableDataPacket setChannelId(int channel);
	MutableDataPacket setTypeCode(int type);
}
