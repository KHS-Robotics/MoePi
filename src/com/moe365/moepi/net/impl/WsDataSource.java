package com.moe365.moepi.net.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.moe365.moepi.net.channel.DataChannel;
import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.channel.DataChannelDirection;
import com.moe365.moepi.net.channel.DataChannelMediaType;
import com.moe365.moepi.net.channel.DataSource;
import com.moe365.moepi.net.channel.UnsubscriptionReason;
import com.moe365.moepi.net.exception.DataPacketException;
import com.moe365.moepi.net.exception.ErrorCode;
import com.moe365.moepi.net.packet.AckPacket;
import com.moe365.moepi.net.packet.ChannelClosePacket;
import com.moe365.moepi.net.packet.ChannelEnumerationPacket;
import com.moe365.moepi.net.packet.ChannelEnumerationRequestPacket;
import com.moe365.moepi.net.packet.ChannelMetadataPacket;
import com.moe365.moepi.net.packet.ChannelMetadataRequestPacket;
import com.moe365.moepi.net.packet.ChannelSubscribePacket;
import com.moe365.moepi.net.packet.ChannelUnsubscribePacket;
import com.moe365.moepi.net.packet.ClientHelloPacket;
import com.moe365.moepi.net.packet.DataPacket;
import com.moe365.moepi.net.packet.ErrorPacket;
import com.moe365.moepi.net.packet.ErrorPacket.MutableErrorPacket;
import com.moe365.moepi.net.packet.MutableDataPacket;
import com.moe365.moepi.net.packet.MutableWrappingDataPacket;
import com.moe365.moepi.net.packet.PacketTypeCode;
import com.moe365.moepi.net.packet.PropertyEnumerationPacket;
import com.moe365.moepi.net.packet.PropertyEnumerationRequestPacket;
import com.moe365.moepi.net.packet.PropertyValuesPacket;
import com.moe365.moepi.net.packet.PropertyValuesRequestPacket;
import com.moe365.moepi.net.packet.ServerHelloPacket;
import com.moe365.moepi.util.SparseArray;
import com.moe365.moepi.util.StringUtils;

public class WsDataSource extends WebSocketServlet implements DataSource {
	private static final long serialVersionUID = -902434272219432543L;
	public static final int SERVER_VERSION = 0;
	/**
	 * Last id for a packet sent from the server.
	 */
	protected volatile int lastPacketId = 0;
	protected ResponseHandlerManager responseHandlerManager = new ResponseHandlerManager();
	protected ConcurrentHashMap<Integer, AbstractWsDataChannel> channels = new ConcurrentHashMap<Integer, AbstractWsDataChannel>();
	protected SparseArray<Function<ByteBuffer, DataPacket>> packetBuilders = new SparseArray<>();
	protected ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
		volatile int threadId = 0;

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("DataSource-" + threadId++);
			return t;
		}
	});
	public WsDataSource() {
		this.channels.put(0, new MetaChannel());
		executor.submit(responseHandlerManager);
		//Register constructors
		packetBuilders.put(PacketTypeCode.SERVER_HELLO, ServerHelloPacket::new);
		packetBuilders.put(PacketTypeCode.CLIENT_HELLO, ClientHelloPacket::new);
		packetBuilders.put(PacketTypeCode.ERROR, ErrorPacket.MutableErrorPacket::new);
		packetBuilders.put(PacketTypeCode.ACK, AckPacket::new);
		packetBuilders.put(PacketTypeCode.CHANNEL_ENUMERATION_REQUEST, ChannelEnumerationRequestPacket::new);
		packetBuilders.put(PacketTypeCode.CHANNEL_ENUMERATION, ChannelEnumerationPacket::new);
		packetBuilders.put(PacketTypeCode.CHANNEL_SUBSCRIBE, ChannelSubscribePacket::new);
		packetBuilders.put(PacketTypeCode.CHANNEL_UNSUBSCRIBE, ChannelUnsubscribePacket::new);
		packetBuilders.put(PacketTypeCode.CHANNEL_CLOSE, ChannelClosePacket::new);
		packetBuilders.put(PacketTypeCode.CHANNEL_METADATA_REQUEST, ChannelMetadataRequestPacket::new);
		packetBuilders.put(PacketTypeCode.CHANNEL_METADATA, null);
		packetBuilders.put(PacketTypeCode.PROPERTY_ENUMERATION_REQUEST, PropertyEnumerationRequestPacket::new);
		packetBuilders.put(PacketTypeCode.PROPERTY_ENUMERATION, PropertyEnumerationPacket::new);
		packetBuilders.put(PacketTypeCode.PROPERTY_VALUES_REQUEST, PropertyValuesRequestPacket::new);
		packetBuilders.put(PacketTypeCode.PROPERTY_VALUES, PropertyValuesPacket::new);
	}

	@Override
	public List<DataChannel> getAvailableChannels() {
		List<DataChannel> result = new ArrayList<>(channels.size());
		for (int i = 0; i < channels.size(); i++)
			result.add(channels.get(i));
		return result;
	}

	@Override
	public void registerChannel(DataChannel channel) {
		channels.put(channel.getId(), (AbstractWsDataChannel) channel);
		if (channel instanceof Runnable)//TODO remove
			executor.submit((Runnable)channel);
	}

	@Override
	public void unregisterChannel(DataChannel channel) {
		//TODO check if valid op
		channels.remove(channel.getId());
	}
	
	class MetaChannel extends AbstractWsDataChannel {
		@Override
		protected boolean onSubscription(DataChannelClient client) {
			//All clients are implicitly subscribed
			return false;
		}

		@Override
		protected void onRecievePacket(DataPacket packet, DataChannelClient client) {
			System.out.println("Handling packet " + packet + ", type " + packet.getTypeCode());
			switch (packet.getTypeCode()) {
				case PacketTypeCode.CHANNEL_ENUMERATION_REQUEST: {
					System.out.println("Channel enumeration request from " + client);
					AbstractWsDataChannel[] ch = WsDataSource.this.channels.values().toArray(new AbstractWsDataChannel[channels.size()]);
					Objects.nonNull(ch);
					ChannelEnumerationPacket response = new ChannelEnumerationPacket(ch);
					response.setAckId(packet.getId())
						.setId(lastPacketId++)
						.setChannelId(0);
					client.write(response);
					break;
				}
				case PacketTypeCode.CHANNEL_METADATA_REQUEST: {
					int cID = ((ChannelMetadataRequestPacket)packet).getTargetChannelId();
					System.out.println("Channel metadata request from " + client + " for channel ID " + cID);
					AbstractWsDataChannel channel = WsDataSource.this.channels.get(cID);
					MutableDataPacket response;
					if (channel == null) {
						response = new MutableErrorPacket(ErrorCode.INVALID_CHANNEL, "Unknown channel " + cID);
					} else {
						response = new ChannelMetadataPacket(channel.getMetadata());
					}
					client.write(response
							.setId(lastPacketId++)
							.setAckId(packet.getId())
							.setChannelId(0));
					break;
				}
				case PacketTypeCode.CHANNEL_SUBSCRIBE: {
					System.out.println("Channel subscription from " + client);
					ChannelSubscribePacket subscribePacket = (ChannelSubscribePacket) packet;
					for (int id : subscribePacket.getChannelIds()) {
						System.out.println("\tSubscribing to channel #" + id);
						AbstractWsDataChannel channel = channels.get(id);
						//TODO figure out how to make atomic
						if (channel == null || !channel.onSubscription(client)) {
							System.err.println("Writing error");
							client.write(new MutableErrorPacket(ErrorCode.INVALID_CHANNEL, "Cannot subscribe to channel " + (channel == null ? "null" : channel.getId()))
								.setId(lastPacketId++)
								.setChannelId(0)
								.setAckId(packet.getId()));
							return;
						}
					}
					client.write(new AckPacket(packet.getId())
						.setId(lastPacketId++)
						.setChannelId(0));
					break;
				}
				case PacketTypeCode.CHANNEL_UNSUBSCRIBE: {
					ChannelUnsubscribePacket unsubscribePacket = (ChannelUnsubscribePacket) packet;
					int[] ids = unsubscribePacket.getChannelIds();
					UnsubscriptionReason[] reasons = unsubscribePacket.getReasons();
					for (int i = 0; i < ids.length; i++) {
						AbstractWsDataChannel channel = channels.get(ids[i]);
						if (channel == null)
							//TODO handle INVALID_CHANNEL
							continue;
						channel.onUnsubscription(client, reasons[i]);
					}
					break;
				}
				case PacketTypeCode.CHANNEL_ENUMERATION:
				case PacketTypeCode.CHANNEL_METADATA:
				default:
					client.write(new MutableErrorPacket(ErrorCode.ILLEGAL_PACKET_TYPE, "Illegal packet type " + packet.getTypeCode())
							.setAckId(packet.getId())
							.setChannelId(0)
							.setId(lastPacketId++));
					break;
			}
		}
		
		@Override
		protected boolean isSubscriber(DataChannelClient client) {
			//Every client is implicitly a subscriber of channel 0
			return true;
		}

		@Override
		protected void onUnsubscription(DataChannelClient client, UnsubscriptionReason reason) {
			// TODO Auto-generated method stub
		}

		@Override
		public DataPacket parseNext(ByteBuffer buf, int typeCode) {
			Function<ByteBuffer, DataPacket> builder = WsDataSource.this.packetBuilders.get(typeCode);
			if (builder == null)
				throw new DataPacketException(ErrorCode.UNKNOWN_PACKET_TYPE);
			return builder.apply(buf);
		}

		@Override
		public DataChannelMediaType getType() {
			return DataChannelMediaType.META;
		}

		@Override
		public DataChannelDirection getDirection() {
			return DataChannelDirection.BOTH;
		}
	}

	public class WsClient implements WebSocketListener, DataChannelClient {
		Session session;
		protected Map<String, Object> properties;
		
		public void sessionPut(String key, Object value) {
			if (properties == null)
				this.properties = new ConcurrentHashMap<>();
			properties.put(key, value);
		}
		
		@SuppressWarnings("unchecked")
		public <T> T sessionGet(String key) {
			if (properties == null)
				return null;
			return (T) properties.get(key);
		}

		@Override
		public CompletableFuture<Void> write(DataPacket packet) {
			ByteBuffer buf = packet.writeTo(ByteBuffer.allocate(packet.getLength()));
			buf.flip();
			return write(buf);
		}
		
		public CompletableFuture<Void> write(ByteBuffer data) {
			CompletableFuture<Void> result = new CompletableFuture<>();
			if (data.hasArray()) {
//				System.out.println("Writing: " + StringUtils.toHexString(data.array(), data.position(), data.limit(), 16));
			}
			try {
				session.getRemote().sendBytes(data, new WriteCallback() {
	
					@Override
					public void writeFailed(Throwable t) {
						result.completeExceptionally(t);
					}
	
					@Override
					public void writeSuccess() {
						result.complete(null);
					}
	
				});
			} catch (Exception e) {
				e.printStackTrace();
				result.completeExceptionally(e);
			}
			return result;
		}

		@Override
		public void onWebSocketClose(int statusCode, String reason) {
			try {
				System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
				WsDataSource.this.channels.forEachValue(10, channel->{
					if (channel.isSubscriber(this))
						channel.onUnsubscription(this, UnsubscriptionReason.NETWORK_DISCONNECT);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onWebSocketConnect(Session session) {
			System.out.println("Connected: " + session.getRemoteAddress());
			this.session = session;
		}

		@Override
		public void onWebSocketError(Throwable t) {
			System.out.println("Error: " + t.getMessage());
		}

		@Override
		public void onWebSocketBinary(byte[] arr, int offset, int length) {
			System.out.println(StringUtils.toHexString(arr, offset, length, 16));
			//Read packet
			ByteBuffer buf = ByteBuffer.wrap(arr, offset, length);
			//check packet size, to make sure that we can read the header fields
			if (length < DataPacket.HEADER_LENGTH) {
				System.err.println("Recieved packet w/ length=" + length);
				return;
			}
			
			//Get builder

			MutableWrappingDataPacket tmpPacket = new MutableWrappingDataPacket(buf);
			int channelId = tmpPacket.getChannelId();
			AbstractWsDataChannel channel = WsDataSource.this.channels.get(channelId);
			if (channel == null) {
				System.err.println("Unknown channel ID " + channelId);
				write(new MutableErrorPacket(ErrorCode.INVALID_CHANNEL, "Unknown channel ID #" + channelId)
						.setId(lastPacketId++)
						.setAckId(tmpPacket.getId())
						.setChannelId(tmpPacket.getChannelId()));
				return;	
			}
			//Build packet
			DataPacket packet;
			try {
				packet = channel.parseNext(buf, tmpPacket.getTypeCode());
			} catch (DataPacketException e0) {
				e0.printStackTrace();
				write(new MutableErrorPacket(e0.getCode(), e0.getMessage())
						.setId(lastPacketId++)
						.setAckId(tmpPacket.getId())
						.setChannelId(tmpPacket.getChannelId()));
				return;
			} catch (Exception e) {
				//TODO handle
				e.printStackTrace();
				return;
			}
			
			//Respond to ACK listener(s)
			if (packet.getAckId() != 0)
				WsDataSource.this.responseHandlerManager.handle(this, packet);
			try {
				channel.onRecievePacket(packet, this);
			} catch (Exception e0) {
				e0.printStackTrace();
				write(new MutableErrorPacket(ErrorCode.INTERNAL_ERROR, e0.getMessage())
						.setAckId(buf.getInt(offset + DataPacket.CHANNEL_ID_OFFSET))
						.setId(lastPacketId++)
						.setChannelId(channelId))
					//Yes, really
					.exceptionally(e->{new RuntimeException("Error while writing error packet for internal error", e).printStackTrace();return null;});
			}
		}

		@Override
		public void onWebSocketText(String data) {
			// We aren't really interested in text messages
		}
	}

	@Override
	public void configure(WebSocketServletFactory factory) {
		//Wouldn't have figured this out if not for github.com/czyzby/reinvent/blob/master/websocket/src/com/github/czyzby/reinvent/websocket/WebSocketServer.java
		factory.setCreator(new WebSocketCreator() {
			@Override
			public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
				response.setAcceptedSubProtocol("v" + WsDataSource.SERVER_VERSION + ".moews");
				System.out.println("Generating client for " + request.getRequestPath() + ", protocol = " + request.getProtocolVersion() + ", " + request.getSubProtocols());
				return new WsClient();
			}
		});
	}
}
