package it.micegroup.openai.plugin.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class EncryptionUtil {

	/**
	 * Encrypts the given value using the given key and algorithm.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String value) throws Exception {
		Key key = new SecretKeySpec(OpenAiConstants.KEY, OpenAiConstants.ALGORITHM);
		Cipher cipher = Cipher.getInstance(OpenAiConstants.ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedValue = cipher.doFinal(value.getBytes());
		return Base64.encodeBase64String(encryptedValue);
	}

	/**
	 * Decrypts the given value using the given key and algorithm.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String value) throws Exception {
		Key key = new SecretKeySpec(OpenAiConstants.KEY, OpenAiConstants.ALGORITHM);
		Cipher cipher = Cipher.getInstance(OpenAiConstants.ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedValue = cipher.doFinal(Base64.decodeBase64(value));
		return new String(decryptedValue);
	}
}
