package com.moe365.moepi.net.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.channel.DataChannelDirection;
import com.moe365.moepi.net.channel.DataChannelMediaType;
import com.moe365.moepi.net.channel.UnsubscriptionReason;
import com.moe365.moepi.net.impl.WsDataSource.WsClient;
import com.moe365.moepi.net.packet.DataPacket;

import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.encoder.h264.H264Encoder;

public class H264BroadcastChannel extends AbstractWsDataChannel {
	protected final ConcurrentHashMap<WsClient, H264Encoder> encoderMap = new ConcurrentHashMap<>();
	
	public H264BroadcastChannel(WsDataSource source, int id, String name) {
		super(source, id, name);
		this.subscribers = encoderMap.keySet();
	}
	
	@Override
	public DataChannelMediaType getType() {
		return DataChannelMediaType.AUDIO_VIDEO;
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

	@Override
	protected boolean onSubscription(DataChannelClient client) {
		return super.onSubscription(client);
	}

	@Override
	protected void onUnsubscription(DataChannelClient client, UnsubscriptionReason reason) {
		super.onUnsubscription(client, reason);
	}
	
	public void broadcastFrame(VideoFrame frame) {
		
	}
}
