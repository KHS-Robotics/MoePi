package com.moe365.moepi.net.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.moe365.moepi.net.channel.DataChannel;
import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.channel.UnsubscriptionReason;
import com.moe365.moepi.net.impl.ResponseHandlerManager.ResponseHandler;
import com.moe365.moepi.net.impl.WsDataSource.WsClient;
import com.moe365.moepi.net.packet.DataPacket;
import com.moe365.moepi.net.packet.MutableDataPacket;
import com.moe365.moepi.net.packet.MutableWrappingDataPacket;

/**
 * An implementation of {@link com.moe365.moepi.net.channel.DataChannel DataChannel}
 * for the WebSocket protocol.
 * @author mailmindlin
 */
public abstract class AbstractWsDataChannel implements DataChannel {
	protected final Map<String, String> metadata;
	protected WsDataSource source;
	protected int id;
	protected String name;
	protected Set<WsClient> subscribers;
	
	public AbstractWsDataChannel() {
		this(new HashMap<>());
	}
	
	public AbstractWsDataChannel(WsDataSource source, int id, String name) {
		this();
		this.source = source;
		this.id = id;
		this.name = name;
		this.metadata.put("name", name);
	}
	
	public AbstractWsDataChannel(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	public void setMetadata(String key, String value) {
		this.metadata.put(key, value);
		System.out.println(metadata);
	}
	
	protected final void setId(int id) {
		this.id = id;
	}

	@Override
	public Set<DataChannelClient> getSubscribers() {
		return Collections.unmodifiableSet(subscribers);
	}

	/**
	 * Prepare a packet to be send by ensuring that the <var>id</var> and
	 * <var>channelId</var> fields are correct.
	 * 
	 * @param packet
	 * @return
	 */
	protected MutableDataPacket preparePacket(DataPacket packet) {
		MutableDataPacket mutablePacket;
		if (packet instanceof MutableDataPacket)
			mutablePacket = (MutableDataPacket) packet;
		else
			mutablePacket = new MutableWrappingDataPacket(packet);
		
		mutablePacket.setId(source.lastPacketId++);
		mutablePacket.setChannelId(this.getId());
		return mutablePacket;
	}

	@Override
	public CompletableFuture<Void> broadcastPacket(DataPacket packet) {
		if (this.subscribers.isEmpty())
			return CompletableFuture.completedFuture(null);
		
		ArrayList<CompletableFuture<Void>> result = new ArrayList<>(subscribers.size());
		
		DataPacket prepared;
		try {
			prepared = preparePacket(packet);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		// TODO fix for clients added/removed while iterating
		for (WsClient client : subscribers)
			result.add(client.write(prepared));
		return CompletableFuture.allOf(result.toArray(new CompletableFuture[result.size()]));
	}

	@Override
	public CompletableFuture<Void> sendPacket(DataPacket packet, DataChannelClient target) {
		return ((WsClient)target).write(preparePacket(packet));
	}

	@Override
	public CompletableFuture<Void> broadcastPacketExpectResponse(DataPacket packet,
			BiConsumer<DataPacket, DataChannelClient> responseHandler, Duration timeout) {
		
		//Build packet only once
		DataPacket prepared = preparePacket(packet);
		
		List<CompletableFuture<? extends DataPacket>> results = this.getSubscribers()
			.parallelStream()
			.map(subscriber -> this.sendPacketExpectResponse(prepared, subscriber, timeout))
			.collect(Collectors.toList());
		
		/*
		 * TODO *when* exactly should the result of this method be completed?
		 * After writing to all the endpoints? After recieving the first response?
		 * After all the responses?
		 * Should it fail if timed out before any of the endpoints responded?
		 */
		return CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]));
	}

	@Override
	@SuppressWarnings("unchecked")
	public CompletableFuture<? extends DataPacket> sendPacketExpectResponse(DataPacket packet, DataChannelClient target,
			Duration timeout) {
		DataPacket prepared = preparePacket(packet);
		final CompletableFuture<DataPacket> result = new CompletableFuture<>();
		this.source.responseHandlerManager
				.addHandler(ResponseHandler.with(packet.getId(), Instant.now().plus(timeout), true,
						result::complete, () -> result.completeExceptionally(new TimeoutException())));
		((WsClient)target).write(prepared);
		return result;
	}
	
	//TODO rename
	public void unsubscribe(DataChannelClient subscriber) {
		// TODO Auto-generated method stub

	}

	
	protected boolean onSubscription(DataChannelClient client) {
		this.subscribers.add((WsClient)client);
		return true;
	}
	protected abstract void onRecievePacket(DataPacket packet, DataChannelClient client);
	protected void onUnsubscription(DataChannelClient client, UnsubscriptionReason reason) {
		this.subscribers.remove(client);
	}
	
	protected boolean isSubscriber(DataChannelClient client) {
		return this.subscribers.contains(client);
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String> getMetadata() {
		return this.metadata;
	}
}
