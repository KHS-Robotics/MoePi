package com.moe365.moepi.net.impl;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.channel.DataChannelDirection;
import com.moe365.moepi.net.channel.DataChannelMediaType;
import com.moe365.moepi.net.packet.DataPacket;
import com.moe365.moepi.net.packet.StreamFramePacket;

import au.edu.jcu.v4l4j.VideoFrame;

public class MjpegBroadcastChannel extends AbstractWsDataChannel implements Runnable {

	protected final int STATUS_EMPTY = 0;
	protected final int STATUS_FILLED = 1;
	protected final int STATUS_READING = 2;
	protected final int STATUS_WRITING = 3;
	
	protected final AtomicInteger imageStatus = new AtomicInteger(0);
	protected final AtomicInteger backlog = new AtomicInteger(0);
	
	protected final ByteBuffer imageBuffer = ByteBuffer.allocate(256 * 1024);
	
	public MjpegBroadcastChannel(WsDataSource source, int id, String name, int width, int height) {
		super(source, id, name);
		this.subscribers = ConcurrentHashMap.newKeySet();
		metadata.put("name", this.name);
		metadata.put("video.format", "MJPEG");
		metadata.put("video.width", Integer.toString(width));
		metadata.put("video.height", Integer.toString(height));
	}
	
	@Override
	public DataChannelMediaType getType() {
		return DataChannelMediaType.VIDEO;
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
	public void run() {
		System.out.println("Starting broadcast channel @ port " + this.getId());
		DataPacket imagePacket = StreamFramePacket.wrapImage(imageBuffer);
		while (!Thread.interrupted()) {
			if (imageStatus.compareAndSet(STATUS_FILLED, STATUS_READING)) {
				backlog.incrementAndGet();
				synchronized (imageBuffer) {
					this.broadcastPacket(imagePacket).whenComplete((r,e)->backlog.decrementAndGet());
				}
				imageStatus.set(STATUS_EMPTY);
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		System.err.println("Bye!");
	}
	
	public void offerFrame(VideoFrame frame) {
		if (backlog.get() > 10) {
			//Backlog is too big; drop frame
			System.out.println("Dropping frame (backlog = " + backlog.get() + ")");
			return;
		}
		if (imageStatus.compareAndSet(STATUS_EMPTY, STATUS_WRITING)) {
			synchronized (imageBuffer) {
				imageBuffer.clear();
				try {
					imageBuffer.put(frame.getBuffer());
				} catch (BufferOverflowException e) {
					System.err.println("Oversized frame: " + frame.getBuffer().remaining());
					throw e;
				}
				imageBuffer.flip();
			}
			imageStatus.set(STATUS_FILLED);
		}
	}

	@Override
	protected void onRecievePacket(DataPacket packet, DataChannelClient client) {
		// TODO Auto-generated method stub
		
	}
	
}
