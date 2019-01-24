package com.moe365.moepi.net;

import com.moe365.moepi.net.packet.DataPacket;

public interface RemoteSocket {
	void sendPacket(DataPacket packet);
}
