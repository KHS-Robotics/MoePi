package com.moe365.moepi.net.impl;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.channel.DataChannelDirection;
import com.moe365.moepi.net.channel.DataChannelMediaType;
import com.moe365.moepi.net.channel.EventfulDataChannel;
import com.moe365.moepi.net.channel.UnsubscriptionReason;
import com.moe365.moepi.net.packet.DataPacket;

public class EventfulWsDataChannel extends AbstractWsDataChannel implements EventfulDataChannel {

	
	protected Set<Consumer<DataChannelClient>> subscriptionHandlers;
	protected Set<BiConsumer<DataChannelClient, UnsubscriptionReason>> unsubscriptionHandlers;
	protected Set<BiConsumer<DataPacket, DataChannelClient>> packetRecievedHandlers;

	@Override
	public void addSubscriptionHandler(Consumer<DataChannelClient> handler) {
		this.subscriptionHandlers.add(handler);
	}

	@Override
	public boolean removeSubscriptionHandler(Consumer<DataChannelClient> handler) {
		return this.subscriptionHandlers.remove(handler);
	}

	@Override
	public void addUnsubscriptionHandler(BiConsumer<DataChannelClient, UnsubscriptionReason> handler) {
		this.unsubscriptionHandlers.add(handler);
	}

	@Override
	public boolean removeUnsubscriptionHandler(BiConsumer<DataChannelClient, UnsubscriptionReason> handler) {
		return this.unsubscriptionHandlers.remove(handler);
	}

	@Override
	public void addRecievePacketHandler(BiConsumer<DataPacket, DataChannelClient> handler) {
		this.packetRecievedHandlers.add(handler);
	}

	@Override
	public boolean removeRecievePacketHandler(BiConsumer<DataPacket, DataChannelClient> handler) {
		return this.packetRecievedHandlers.remove(handler);
	}

	@Override
	protected boolean onSubscription(DataChannelClient client) {
		//TODO change threading?
		for (Consumer<DataChannelClient> handler : this.subscriptionHandlers)
			handler.accept(client);
		return true;
	}
	
	@Override
	protected void onRecievePacket(DataPacket packet, DataChannelClient client) {
		for (BiConsumer<DataPacket, DataChannelClient> handler : this.packetRecievedHandlers)
			handler.accept(packet, client);
	}

	@Override
	protected void onUnsubscription(DataChannelClient client, UnsubscriptionReason reason) {
		for (BiConsumer<DataChannelClient, UnsubscriptionReason> handler : this.unsubscriptionHandlers)
			handler.accept(client, reason);
	}
	
	@Override
	public DataPacket parseNext(ByteBuffer buf, int typeCode) {
		return null;
	}

	@Override
	public DataChannelMediaType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataChannelDirection getDirection() {
		// TODO Auto-generated method stub
		return null;
	}

}
