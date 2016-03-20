package com.moe365.mopi;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UDP server to broadcast data at the RIO.
 * <p>
 * All packets start with a 32 bit unsigned integer sequence number,
 * which will always increase between consecutive packets. Format of
 * the UDP packets:
 * <pre>
 *  0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          Sequence Number                      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |           Status code         |                ACK            |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * <dl>
 * <dt>Sequence Number: 32 bits</dt>
 * <dd>The packet number, always increasing. If packet A is received with
 * 	a sequence number of 5, then all future packets with sequence numbers under
 * 	5 may be discarded. This may be a timestamp</dd>
 * <dt>Status code: 16 bits</dt>
 * <dd>One of the following:
 * <ol start=0>
 * <li>NOP</li>
 * <li>HELLO_WORLD</li>
 * <li>ERROR</li>
 * <li>NONE_FOUND</li>
 * <li>ONE_FOUMD</li>
 * <li>TWO_FOUND</li>
 * <li>GOODBYE</li>
 * </ol>
 * All other status codes are reserved for future use.
 * </dd>
 * <dt>Flag: 8 bits</dt>
 * <dd>Like a secondary status code, it is used for
 * 		stuff like QOS. If unused, set to 0.
 * 	<table>
 * 		<thead>
 * 			<tr>
 * 				<th>#</th>
 * 				<th>Name</th>
 * 				<th>Description</th>
 * 			</tr>
 *		</thead>
 *		<tbody>
 *			<tr>
 *				<td>0</td>
 *				<td>Ping</td>
 *				<td>Sends a ping request. For latency measurement</td>
 *			</tr>
 *			<tr>
 *				<td>1</td>
 * 			<li>PING</li>
 * 			<li>PONG</li>
 * 			<li>ARE_YOU_STILL_THERE</li>
 * 			<li>YES_I_AM</li>
 * 		</tbpdy>
 * 	</table>
 * </dd>
 * </dl>
 * </p>
 * 			
 * @author mailmindlin
 */
public class RoboRioClient implements Closeable, Runnable {
	public static final int RIO_PORT = 5801;
	public static final int BUFFER_SIZE = 24;
	public static final String RIO_ADDRESS = "roboRIO-365-FRC.local";
	
	public static final short STATUS_NOP = 0;
	public static final short STATUS_NONE_FOUND = 1;
	public static final short STATUS_ONE_FOUND = 2;
	public static final short STATUS_TWO_FOUND = 3;
	public static final short STATUS_ERROR = 4;
	public static final short STATUS_HELLO_WORLD = 5;
	public static final short STATUS_GOODBYE = 6;
	public static final short STATUS_PING = 7;
	public static final short STATUS_PONG = 8;
	public static final short STATUS_ARE_YOU_THERE = 9;
	public static final short STATUS_YES_I_AM = 10;
	public static final short STATUS_REQUEST_CONFIG = 11;
	public static final short STATUS_CONFIG = 12;
	
	/**
	 * 8 bit packet
	 */
	protected DatagramPacket packet_8;
	/**
	 * 16 bit packet
	 */
	protected DatagramPacket packet_16;
	/**
	 * 24 bit packet
	 */
	protected DatagramPacket packet_24;
	/**
	 * UDP socket
	 */
	protected DatagramSocket socket;
	/**
	 * RoboRIO's address
	 */
	protected final SocketAddress address;
	/**
	 * My port
	 */
	protected final int port;
	/**
	 * Buffer backing packets
	 */
	protected final ByteBuffer buffer;
	/**
	 * Packet number
	 */
	protected AtomicInteger packetNum = new AtomicInteger(0);
	/**
	 * Create a 
	 * @throws SocketException
	 */
	public RoboRioClient() throws SocketException {
		this(RIO_PORT, BUFFER_SIZE, new InetSocketAddress(RIO_ADDRESS, RIO_PORT));
	}
	public RoboRioClient(int port, int buffSize, SocketAddress addr) throws SocketException {
		this.port = port;
		this.address = addr;
		this.buffer = ByteBuffer.allocate(buffSize);
		this.socket = new DatagramSocket(port);
		socket.setTrafficClass(0x10);//Low delay
		this.packet_8 = new DatagramPacket(buffer.array(), 0, 8, address);
		this.packet_16 = new DatagramPacket(buffer.array(), 0, 16, address);
		this.packet_24 = new DatagramPacket(buffer.array(), 0, 24, address);
	}
	public void build(short status, short ack) {
		buffer.position(0);
		buffer.putInt(packetNum.getAndIncrement());
		buffer.putShort(status);
		buffer.putShort(ack);
	}
	public void write(short status) throws IOException {
		build(status, (short) 0);
		socket.send(packet_8);
	}
	public void write(short status, short ack) throws IOException {
		build(status, ack);
		socket.send(packet_8);
	}
	public void writeNoneFound() throws IOException {
		write(STATUS_NONE_FOUND);
	}
	public void writeOneFound(double pos) throws IOException {
		build(STATUS_ONE_FOUND, (short) 0);
		buffer.putDouble(pos);
		socket.send(packet_16);
	}
	public void writeTwoFound(double pos1, double pos2) throws IOException {
		build(STATUS_TWO_FOUND, (short) 0);
		buffer.putDouble(pos1);
		buffer.putDouble(pos2);
		socket.send(packet_24);
	}
	public void writeError(long errorCode) throws IOException {
		build(STATUS_ERROR, (short)0);
		buffer.putLong(errorCode);
		socket.send(packet_16);
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {

		}
	}
}
