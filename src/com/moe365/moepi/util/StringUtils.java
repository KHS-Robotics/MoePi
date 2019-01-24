package com.moe365.moepi.util;

public class StringUtils {
	public static final char[] hexChars = "0123456789ABCDEF".toCharArray();
	
	public static String toHexString(final byte[] buf, final int offset, final int length, final int width) {
		StringBuilder sb = new StringBuilder(length * 2 + length / width + 1);
		for (int i = 0; i < length; i++) {
			int b = buf[offset + i] & 0xFF;
			sb.append(hexChars[b >>> 4])
				.append(hexChars[b & 0x0F])
				.append(' ');
			if (i % width == width - 1)
				sb.append('\n');
		}
		return sb.toString();
	}
}
