package com.moe365.moepi.net.packet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

@PacketTypeCode(PacketTypeCode.SERVER_HELLO)
public class ServerHelloPacket extends AbstractMutableDataPacket {
	int protocolVersion;

	public ServerHelloPacket() {
		super();
		this.setTypeCode(PacketTypeCode.SERVER_HELLO);
	}

	public ServerHelloPacket(int protocolVersion) {
		this();
		this.protocolVersion = protocolVersion;
	}

	public ServerHelloPacket(ByteBuffer buf) {
		super(buf);
		this.protocolVersion = buf.getInt();
		this.setTypeCode(PacketTypeCode.SERVER_HELLO);
	}

	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 4;
	}

	public ServerHelloPacket setProtocolVersion(int version) {
		this.protocolVersion = version;
		return this;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.protocolVersion = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(getProtocolVersion());
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putInt(this.getProtocolVersion());
		return buf;
	}
}
