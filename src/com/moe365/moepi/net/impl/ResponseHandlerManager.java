package com.moe365.moepi.net.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.packet.DataPacket;

public class ResponseHandlerManager implements Runnable {
	/**
	 * Maximum time (in ms) between calls to {@link #cleanup()}.
	 */
	public static final long MAX_CLEANUP_WAIT = 10_000;
	ConcurrentHashMap<Integer, ResponseHandler> handlers = new ConcurrentHashMap<>();
	PriorityBlockingQueue<ResponseHandler> queue = new PriorityBlockingQueue<>(10, (a,b)->(a.getInvalidationTime().compareTo(b.getInvalidationTime())));
	public <T extends DataPacket> CompletableFuture<T> onAcknowledgement(int channel, int id, Duration timeout) {
		//TODO finish
		return null;
	}
	public long cleanup() {
		ResponseHandler handler;
		Instant now = Instant.now();
		while ((handler = queue.poll()) != null) {
			Instant invalidationTime = handler.getInvalidationTime();
			if (invalidationTime.isAfter(now)) {
				queue.offer(handler);
				try {
					return Math.min(Duration.between(now, invalidationTime).toMillis(), MAX_CLEANUP_WAIT);
				} catch (ArithmeticException e) {
					return MAX_CLEANUP_WAIT;
				}
			}
			handlers.remove(handler.getId());
			//TODO possibly move execution to another thread
			handler.onTimeout();
		}
		return MAX_CLEANUP_WAIT;
	}
	public boolean handle(DataChannelClient client, DataPacket packet) {
		ResponseHandler handler = handlers.get(packet.getAckId());
		if (handler != null) {
			if (handler.getInvalidationTime().isAfter(Instant.now())) {
				//Handler is invalid, so remove it.
				handlers.remove(packet.getAckId());
				queue.remove(handler);
				return false;
			}
			handler.onResponse(client, packet);
			if (handler.doRemoveAfterResponse()) {
				handlers.remove(packet.getAckId());
				queue.remove(handler);
			}
			return true;
		}
		return false;
	}
	public void addHandler(ResponseHandler handler) {
		handlers.put(handler.getId(), handler);
		queue.offer(handler);
	}
	/**
	 * Periodically run {@link #cleanup()}
	 */
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				Thread.sleep(cleanup());
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	public static abstract class ResponseHandler {
		public static ResponseHandler with(int id, Instant invalidationTime, boolean removeAfterResponse, Consumer<DataPacket> responseHandler, Runnable onTimeout) {
			return new ResponseHandler(id, invalidationTime, removeAfterResponse) {
				@Override
				public void onTimeout() {
					onTimeout.run();
				}
				@Override
				public void onResponse(DataChannelClient client, DataPacket packet) {
					responseHandler.accept(packet);
				}
			};
		}
		public static ResponseHandler with(int id, Instant invalidationTime, boolean removeAfterResponse, BiConsumer<DataChannelClient, DataPacket> responseHandler, Runnable onTimeout) {
			return new ResponseHandler(id, invalidationTime, removeAfterResponse) {
				@Override
				public void onTimeout() {
					onTimeout.run();
				}
				@Override
				public void onResponse(DataChannelClient client, DataPacket packet) {
					responseHandler.accept(client, packet);
				}
			};
		}
		protected final int id;
		protected final Instant invalidationTime;
		protected final boolean removeAfterResponse;
		public ResponseHandler(int id, Instant invalidationTime, boolean removeAfterResponse) {
			this.id = id;
			this.invalidationTime = invalidationTime;
			this.removeAfterResponse = removeAfterResponse;
		}
		public Instant getInvalidationTime() {
			return invalidationTime;
		}
		public int getId() {
			return id;
		}
		public boolean doRemoveAfterResponse() {
			return this.removeAfterResponse;
		}
		public abstract void onTimeout();
		public abstract void onResponse(DataChannelClient client, DataPacket packet);
	}
}
