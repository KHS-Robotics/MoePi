package com.moe365.moepi.client;

import java.io.Closeable;
import java.io.IOException;

import com.moe365.moepi.client.packets.*;
import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.geom.TargetType;

/**
 * UDP server to broadcast data at the RIO. <strong>Not</strong> thread safe.
 * <p>
 * 
 * <section id="header">
 * <h2>Packet Header</h2>
 * <pre>
 * &nbsp;&nbsp;0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          Sequence Number                      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |           Status code         |              Flag             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * <dl>
 * <dt><strong>Sequence Number</strong></dt>
 * <dd>32-bit packet number, always increasing. If packet A is received with a
 * sequence number of 5, for example, then all future packets with sequence
 * numbers under 5 may be discarded. Because this field is not assured to be
 * consecutive, unix (or other) timestamps may be used, assuming no consecutive
 * packets are sent within the same lowest resolution time unit.
 * </dd>
 * <dt><strong>Status code</strong></dt>
 * <dd>16-bit code indicating the intent of the packet. See <a href="#statusCodes">Status Codes</a>.</dd>
 * <dt><strong>Flag</strong></dt>
 * <dd>8-bit secondary status code, it is used for stuff like QOS. If unused,
 * set to 0. See <a href="#flags">Flags</a>.
 * </dd>
 * </dl>
 * </section>
 * <section id="statusCodes">
 * <h3>Status Codes</h3>
 * A status code may be one of the following:
 * <ul>
 * <li>{@linkplain RioPacket#STATUS_NOP NOP}: 0</li>
 * <li>{@linkplain RioPacket#STATUS_NONE_FOUND NONE_FOUND}: 1</li>
 * <li>{@linkplain RioPacket#STATUS_ONE_FOUND ONE_FOUND}: 2</li>
 * <li>{@linkplain RioPacket#STATUS_TWO_FOUND TWO_FOUND}: 3</li>
 * <li>{@linkplain RioPacket#STATUS_THREE_FOUND THREE_FOUND}: 4</li>
 * <li>{@linkplain RioPacket#STATUS_FOUR_FOUND FOUR_FOUND}: 5</li>
 * <li>{@linkplain RioPacket#STATUS_FIVE_FOUND FIVE_FOUND}: 6</li>
 * <li>{@linkplain RioPacket#STATUS_SIX_FOUND SIX_FOUND}: 7</li>
 * <li>{@linkplain RioPacket#STATUS_ERROR ERROR}: 0x8000</li>
 * <li>{@linkplain RioPacket#STATUS_HELLO_WORLD HELLO_WORLD}: 0x8001</li>
 * <li>{@linkplain RioPacket#STATUS_GOODBYE GOODBYE}: 0x8002</li>
 * </ul>
 * All other status codes are reserved for future use.
 * </section>
 * <section id="flags">
 * <h3>Flags</h3>
 * The flags field is a bitfield.
 * <table style="border:1px solid black;border-collapse:collapse;">
 * <thead>
 * <tr>
 * <th style="text-align:left">Bit</th>
 * <th style="text-align:left">Name</th>
 * <th style="text-align:left">Description</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>0</td>
 * <td>PING</td>
 * <td>Sends a ping request. For latency measurement</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>PONG</td>
 * <td>A response to a ping request</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>ARE_YOU_STILL_THERE</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>YES_I_AM</td>
 * <td></td>
 * </tr>
 * </tbody>
 * </table>
 * </section>
 * @author mailmindlin
 */
public interface RioClient extends Closeable {
	public static final int SERVER_PORT = 5810;
	public static final int RIO_PORT = 5810;
	public static final boolean PREFER_IP6 = true;
	/**
	 * <p>Size of the buffer in the bytes.</p>
	 * header + payload = 1 integer + 1 short + 30 doubles = 4 bytes + 2 bytes + 30*(8 bytes) = 246 bytes
	 */
	public static final int BUFFER_SIZE = 246;
	public static final int RESOLVE_RETRY_TIME = 5_000;
	/**
	 * mDNS address of the RoboRio.
	 */
	public static final String RIO_ADDRESS = "roboRIO-4342-FRC.local";
	
	void broadcast(RioPacket packet) throws IOException;
	
	default void writeNoneFound() throws IOException {
		this.broadcast(new NoneFoundRioPacket());
	}
	
	default void writeOneFound(PreciseRectangle rect) throws IOException {
		this.broadcast(new OneFoundRioPacket(rect));
	}
	
	default void writeOneFound(double left, double top, double width, double height, TargetType type) throws IOException {
		this.broadcast(
			new OneFoundRioPacket(
				left, top, width, height, type
			)
		);
	}
	
	default void writeTwoFound(PreciseRectangle rect1, PreciseRectangle rect2) throws IOException {
		this.broadcast(new TwoFoundRioPacket(rect1, rect2));
	}
	
	default void writeTwoFound(double left1, double top1, double width1, double height1, TargetType type1, double left2, double top2, double width2, double height2, TargetType type2) throws IOException {
		this.broadcast(
			new TwoFoundRioPacket(
				left1, top1, width1, height1, type1, 
				left2, top2, width2, height2, type2
			)
		);
	}

	default void writeThreeFound(PreciseRectangle rect1, PreciseRectangle rect2, PreciseRectangle rect3) throws IOException {
		this.broadcast(new ThreeFoundRioPacket(rect1, rect2, rect3));
	}
	
	default void writeThreeFound(double left1, double top1, double width1, double height1, TargetType type1, 
								double left2, double top2, double width2, double height2, TargetType type2,
								double left3, double top3, double width3, double height3, TargetType type3) throws IOException {
		this.broadcast(
			new ThreeFoundRioPacket(
				left1, top1, width1, height1, type1, 
				left2, top2, width2, height2, type2, 
				left3, top3, width3, height3, type3
			)
		);
	}

	default void writeFourFound(PreciseRectangle rect1, PreciseRectangle rect2, PreciseRectangle rect3, PreciseRectangle rect4) throws IOException {
		this.broadcast(new FourFoundRioPacket(rect1, rect2, rect3, rect4));
	}
	
	default void writeFourFound(double left1, double top1, double width1, double height1, TargetType type1, 
								double left2, double top2, double width2, double height2, TargetType type2,
								double left3, double top3, double width3, double height3, TargetType type3,
								double left4, double top4, double width4, double height4, TargetType type4) throws IOException {
		this.broadcast(
			new FourFoundRioPacket(
				left1, top1, width1, height1, type1, 
				left2, top2, width2, height2, type2, 
				left3, top3, width3, height3, type3,
				left4, top4, width4, height4, type4
			)
		);
	}

	default void writeFiveFound(PreciseRectangle rect1, PreciseRectangle rect2, PreciseRectangle rect3, PreciseRectangle rect4, PreciseRectangle rect5) throws IOException {
		this.broadcast(new FiveFoundRioPacket(rect1, rect2, rect3, rect4, rect5));
	}
	
	default void writeFiveFound(double left1, double top1, double width1, double height1, TargetType type1, 
								double left2, double top2, double width2, double height2, TargetType type2,
								double left3, double top3, double width3, double height3, TargetType type3,
								double left4, double top4, double width4, double height4, TargetType type4,
								double left5, double top5, double width5, double height5, TargetType type5) throws IOException {
		this.broadcast(
			new FiveFoundRioPacket(
				left1, top1, width1, height1, type1, 
				left2, top2, width2, height2, type2, 
				left3, top3, width3, height3, type3,
				left4, top4, width4, height4, type4,
				left5, top5, width5, height5, type5
			)
		);
	}

	default void writeSixFound(PreciseRectangle rect1, PreciseRectangle rect2, PreciseRectangle rect3, PreciseRectangle rect4, PreciseRectangle rect5, PreciseRectangle rect6) throws IOException {
		this.broadcast(new SixFoundRioPacket(rect1, rect2, rect3, rect4, rect5, rect6));
	}
	
	default void writeSixFound(double left1, double top1, double width1, double height1, TargetType type1, 
								double left2, double top2, double width2, double height2, TargetType type2,
								double left3, double top3, double width3, double height3, TargetType type3,
								double left4, double top4, double width4, double height4, TargetType type4,
								double left5, double top5, double width5, double height5, TargetType type5,
								double left6, double top6, double width6, double height6, TargetType type6) throws IOException {
		this.broadcast(
			new SixFoundRioPacket(
				left1, top1, width1, height1, type1, 
				left2, top2, width2, height2, type2, 
				left3, top3, width3, height3, type3,
				left4, top4, width4, height4, type4,
				left5, top5, width5, height5, type5,
				left6, top6, width6, height6, type6
			)
		);
	}
	
	default void writeError(String message) throws IOException {
		broadcast(new ErrorRioPacket(message));
	}
}
