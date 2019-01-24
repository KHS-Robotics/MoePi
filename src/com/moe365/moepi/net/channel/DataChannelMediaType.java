package com.moe365.moepi.net.channel;

/**
 * The media type of a DataChannel. For pre-negotiation of data protocols. This
 * information is sent to the client in the response to a
 * <code>CHANNEL_ENUMERATION_REQUEST</code> packet.
 * 
 * @author mailmindlin
 *
 */
public enum DataChannelMediaType {
	/**
	 * The META media type is for the meta channel only (id #0). This channel
	 * contians packets pertaining to the DataStream as a whole, such as
	 * SERVER_HELLO and CHANNEL_ENUMERATION_REQUEST
	 */
	META,
	/**
	 * A channel that sends an audio stream. Exact protocol is negotiated with
	 * <code>REQUEST_META</code> packets.
	 */
	AUDIO,
	/**
	 * A channel that sends a video stream. Exact protocol is negotiated with
	 * <code>CHANNEL_META</code> packets.
	 */
	VIDEO,
	/**
	 * Both audio and video data streams.
	 */
	AUDIO_VIDEO,
	/**
	 * A channel that allows for property getting and setting
	 */
	PROPERTY_ACCESS,
	/**
	 * A stream of text objects
	 */
	TEXT_STREAM,
	/**
	 * A stream of objects. It is specified in the <code>CHANNEL_META</code>
	 * negotiation whether it is merely a {@link #TEXT_STREAM} with strucutred
	 * data, or a constantly updating single object. Defaults to JSON for
	 * serialization, unless otherwise specified.
	 */
	OBJECT_STREAM,
	/**
	 * Reserved for future versions.
	 */
	FUTURE;
}