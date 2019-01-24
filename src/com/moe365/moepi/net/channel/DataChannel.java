package com.moe365.moepi.net.channel;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.moe365.moepi.net.packet.DataPacket;

/**
 * A specific resource that is available from a DataSource.
 * 
 * @author mailmindlin
 */
public interface DataChannel {
	/**
	 * Get the id of this channel
	 * 
	 * @return channel id
	 */
	int getId();

	/**
	 * Get the name of this channel. Channel names can be 0-255 bytes when UTF-8
	 * encoded.
	 * 
	 * @return channel name
	 */
	String getName();

	/**
	 * Get the media type of this channel.
	 * 
	 * @return channel media type
	 */
	DataChannelMediaType getType();

	/**
	 * Get the data flow direction of this channel. The direction is more of a
	 * guideline, of where unique data is generated. For example, a property
	 * access channel that sets server properties would be marked as
	 * {@link DataChannelDirection#SERVER_TO_CLIENT SERVER_TO_CLIENT}, if it is
	 * intended to be mainly used to access properties, while it would be marked
	 * as {@link DataChannelDirection#CLIENT_TO_SERVER CLIENT_TO_SERVER} if it
	 * is intended to be mainly used to set properties.
	 * 
	 * @return direction of DataChannel
	 */
	DataChannelDirection getDirection();

	/**
	 * Get the current list of subscribers to this channel
	 * 
	 * @return subscribers to this channel
	 */
	Set<DataChannelClient> getSubscribers();

	/**
	 * Broadcast a packet to all connected clients, without expecting a return
	 * packet.
	 * 
	 * @param packet
	 *            Packet to broadcast
	 * @return future of when the packet has been written to all clients
	 */
	CompletableFuture<Void> broadcastPacket(DataPacket packet);
	// TODO add selective broadcast methods (e.g., broadcastPacketIf; needs
	// better name)

	/**
	 * Send a packet to a specific client, without expecting a return packet.
	 * 
	 * @param packet
	 *            Packet to send
	 * @param target
	 *            Client to send packet to
	 * @return future of when the packet has been written
	 */
	CompletableFuture<Void> sendPacket(DataPacket packet, DataChannelClient target);

	/**
	 * Broadcast a packet to all connected clients, waiting for <code>ACK</code>
	 * packets in response.
	 * 
	 * @param packet
	 *            Packet to broadcast
	 * @param responseHandler
	 *            Handler for responses
	 * @param timeout
	 *            Timeout for response handler to remain active TODO decide when
	 *            the future should actually complete (maybe standardize for
	 *            compat. with
	 *            {@link #sendPacketExpectResponse(DataPacket, DataChannelClient, Duration)})
	 * @return future of when the packet has been written
	 */
	CompletableFuture<Void> broadcastPacketExpectResponse(DataPacket packet, BiConsumer<DataPacket, DataChannelClient> responseHandler, Duration timeout);

	/**
	 * Send a packet, expecting a response. Returns success after the first
	 * acknowledgement packet, ignoring all subsequent ones with the same
	 * <code>ACK_ID</code> field.
	 * 
	 * @param packet
	 *            Packet to send
	 * @param target
	 *            Client to send packet to
	 * @param timeout
	 *            Timeout for the first
	 * @return future for the response packet
	 */
	<T extends DataPacket> CompletableFuture<T> sendPacketExpectResponse(DataPacket packet, DataChannelClient target, Duration timeout);
	
	DataPacket parseNext(ByteBuffer buf, int typeCode);
	
	Map<String, String> getMetadata();

	/**
	 * Whether this channel is currently open.
	 * 
	 * @return if it's open
	 */
	boolean isOpen();

	/**
	 * Close this channel. If this channel is already closed, no action should
	 * occur, and no exception should be thrown. All clients should be notified
	 * of their disconnection via <code>CHANNEL_CLOSE</code> packets, and it
	 * should be detrgistered from all DataSources.
	 */
	void close();
}
