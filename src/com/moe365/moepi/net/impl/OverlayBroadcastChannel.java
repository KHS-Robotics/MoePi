package com.moe365.moepi.net.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.channel.DataChannelDirection;
import com.moe365.moepi.net.channel.DataChannelMediaType;
import com.moe365.moepi.net.packet.DataPacket;
import com.moe365.moepi.net.packet.OverlayPacket;

public class OverlayBroadcastChannel extends AbstractWsDataChannel implements Runnable {

	public OverlayBroadcastChannel(WsDataSource source, int id, int srcId, String name) {
		super(source, id, name);
		this.subscribers = ConcurrentHashMap.newKeySet();
		metadata.put("name", this.name);
		metadata.put("sourceChannelId", Integer.toString(srcId));
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DataChannelMediaType getType() {
		return DataChannelMediaType.OBJECT_STREAM;
	}

	@Override
	public DataChannelDirection getDirection() {
		return DataChannelDirection.SERVER_TO_CLIENT;
	}

	@Override
	public DataPacket parseNext(ByteBuffer buf, int typeCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onRecievePacket(DataPacket packet, DataChannelClient client) {
		// TODO Auto-generated method stub
		
	}
	
	public void broadcastRectangles(List<PreciseRectangle> rectangles) {
		//System.out.println("Sending: " + rectangles);
		this.broadcastPacket(new OverlayPacket(rectangles));
	}
	
}
