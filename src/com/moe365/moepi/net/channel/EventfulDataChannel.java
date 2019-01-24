package com.moe365.moepi.net.channel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.moe365.moepi.net.packet.DataPacket;

public interface EventfulDataChannel {

	/**
	 * Register a handler function for when a new client subscribes to this
	 * channel.
	 * 
	 * @param handler
	 *            Subscription handler
	 */
	void addSubscriptionHandler(Consumer<DataChannelClient> handler);

	/**
	 * Deregister a subscription handler function.
	 * 
	 * @param handler
	 *            Previously registered handler method
	 * @return Whether the handler was removed. If false, then the handler was
	 *         never registered with this channel previously; if true, the
	 *         handler was removed successfully. All other possible states
	 *         should be expressed by throwing an exception
	 */
	boolean removeSubscriptionHandler(Consumer<DataChannelClient> handler);

	/**
	 * Register a handler for when a client unsubscribes.
	 * 
	 * @param handler
	 *            Handler to register
	 */
	void addUnsubscriptionHandler(BiConsumer<DataChannelClient, UnsubscriptionReason> handler);

	/**
	 * Deregister a handler for when a client unsubscribes
	 * 
	 * @param handler
	 *            Handler to deregister
	 * @return
	 */
	boolean removeUnsubscriptionHandler(BiConsumer<DataChannelClient, UnsubscriptionReason> handler);

	/**
	 * Register a handler to be triggered when a packet sent with this channel's
	 * id is received.
	 * 
	 * @param handler
	 *            Handler to register
	 */
	void addRecievePacketHandler(BiConsumer<DataPacket, DataChannelClient> handler);

	boolean removeRecievePacketHandler(BiConsumer<DataPacket, DataChannelClient> handler);

	/**
	 * Forcibly remove a client from this channel's subscriber list. Doing so
	 * immediately cancels all packets being sent to the client, possibly
	 * excluding broadcast operations in progress at the time of the method
	 * call. The client should be notified of this.
	 * 
	 * TODO whether an invocation of this method invokes unsubscription handlers
	 * is undefined
	 * 
	 * @param subscriber
	 */
	void unsubscribe(DataChannelClient subscriber);
}
