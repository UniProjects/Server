package org.popov.belezirev.server.security;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {
	private static final String UTF8_DEFAULT_ENCODING = "UTF8";
	private static final int BEGIN_INDEX = 1;
	private static final int END_INDEX = 3;
	private MessageDigest messageDigest;

	public PasswordHasher() {
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}

	public String MD5Hash(String password) {
		messageDigest.reset();
		messageDigest.update(password.getBytes(Charset.forName(UTF8_DEFAULT_ENCODING)));
		final byte[] passwordBytes = messageDigest.digest();
		StringBuffer stringBuffer = new StringBuffer();
		for (byte passwordByte : passwordBytes) {
			stringBuffer.append(Integer.toHexString(passwordByte & 0xFF | 0x100).substring(BEGIN_INDEX, END_INDEX));
		}
		return stringBuffer.toString().toUpperCase();
	}
}