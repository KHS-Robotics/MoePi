package com.moe365.moepi.net.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.channel.DataChannelDirection;
import com.moe365.moepi.net.channel.DataChannelMediaType;
import com.moe365.moepi.net.impl.WsDataSource.WsClient;
import com.moe365.moepi.net.packet.DataPacket;
import com.moe365.moepi.net.packet.StreamFramePacket;

public class RandomlyBroadcastingChannel extends AbstractWsDataChannel implements Runnable {
	
	public RandomlyBroadcastingChannel(WsDataSource source, int id, String name) {
		super(source, id, name);
		this.subscribers = ConcurrentHashMap.newKeySet();
		metadata.put("video.format", "MJPEG");
		metadata.put("video.width", "100");
		metadata.put("video.height", "100");
	}
	
	@Override
	public DataPacket parseNext(ByteBuffer buf, int typeCode) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	protected boolean onSubscription(DataChannelClient client) {
		this.subscribers.add((WsClient) client);
		return true;
	}

	@Override
	protected void onRecievePacket(DataPacket packet, DataChannelClient client) {
		System.out.println("Starting random channel @ port " + this.getId());
		System.out.println("Recieved packet " + packet);
	}

	@Override
	public void run() {
		final int width = 100;
		final int height = 100;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		
		//Thanks to http://stackoverflow.com/a/10929569/2759984 for about the graphics
		Graphics g = img.getGraphics();
		
		int i = 0;
		while(!Thread.interrupted()) {
			g.setColor(Color.RED);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.BLUE);
			g.setFont(g.getFont().deriveFont(24f));
			g.drawString("" + (i++), 45, 50 + 24/2);
			
			final byte[] imgData;
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				ImageIO.write(img, "JPEG", baos);
				imgData = baos.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			this.broadcastPacket(StreamFramePacket.wrapImage(ByteBuffer.wrap(imgData)));
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public DataChannelMediaType getType() {
		return DataChannelMediaType.VIDEO;
	}
	
	@Override
	public DataChannelDirection getDirection() {
		return DataChannelDirection.SERVER_TO_CLIENT;
	}

}
