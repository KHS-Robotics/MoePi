package com.moe365.moepi.net.channel;

import java.util.List;

/**
 * A registry for channels and such that interfaces directly with the client.
 * Handles the META DataChannel internally.
 * 
 * The point of the DataSource is to provide multiplexing capabilities for
 * multiple resources.
 * 
 * @author mailmindlin
 */
public interface DataSource {
	/**
	 * Get a list of the currently available channels. Immutable list.
	 * 
	 * @return available channels
	 */
	List<DataChannel> getAvailableChannels();

	/*
	 * Deprecated because these methods will probably be removed in the future,
	 * but I'm not sure yet as to how to replace them
	 */
	@Deprecated
	void registerChannel(DataChannel channel);

	@Deprecated
	void unregisterChannel(DataChannel channel);
}
