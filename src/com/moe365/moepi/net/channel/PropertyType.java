package com.moe365.moepi.net.channel;

/**
 * An enumeration of the available property types. Types are sent as a byte, by
 * converting them via {@link #ordinal()}
 * 
 * @author mailmindlin
 */
public enum PropertyType {
	BUTTON,
	/**
	 * A boolean property. Allowed values are <samp>true</samp> and
	 * <samp>false</samp>.
	 */
	BOOLEAN,
	/**
	 * An integer property. Use for types of <var>byte</var>, <var>short</var>,
	 * <var>int</var>, and <var>long</var> TODO add range restriction
	 */
	INTEGER,
	/**
	 * A floating point property. Note that this doesn't necessarily mean that
	 * it's of the type <var>float</var>, just that it has a non-fixed decimal.
	 */
	FLOAT,
	/**
	 * A variable-length string property. Allowed lengths are <code>0</code> -
	 * <code>INT_MAX</code>. UTF-8 encoded.
	 */
	STRING;
}
