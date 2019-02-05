package com.moe365.moepi.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.moe365.moepi.client.packets.HelloRioPacket;
import com.moe365.moepi.client.packets.RioPacket;

public class StaticRioClient extends AbstractRioClient {
	
	protected final int serverPort;
	
	protected final int rioPort;
	
	protected final DatagramChannel channel;
	/**
	 * RoboRIO's address.
	 */
	protected final SocketAddress address;
	
	public StaticRioClient(SocketAddress addr) throws SocketException, IOException {
		this(RioClient.SERVER_PORT, addr);
	}
	
	/**
	 * Create a client that WILL NOT resolve the passed address via mDNS
	 * 
	 * @param serverPort
	 */
	public StaticRioClient(int serverPort, SocketAddress addr) throws SocketException, IOException {
		this.serverPort = serverPort;
		this.address = addr;
		
		if (addr instanceof InetSocketAddress) {
			// TODO finish
			InetSocketAddress iAddr = (InetSocketAddress) addr;
			this.rioPort = iAddr.getPort();
		} else {
			this.rioPort = -1;
		}

		System.out.println("Connecting to RIO: " + serverPort + " => " + addr);
		
		this.channel = DatagramChannel.open()
				.setOption(StandardSocketOptions.SO_REUSEADDR, true)
				.bind(new InetSocketAddress(serverPort));
		
		// Send hello packet
		{
			RioPacket helloPacket = new HelloRioPacket();
			this.broadcast(helloPacket);
		}
	}
	
	@Override
	protected void send(ByteBuffer buffer) throws IOException {
		this.channel.send(buffer, this.address);
	}
	
	@Override
	public void close() throws IOException {
		this.channel.close();
	}
}