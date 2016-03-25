/******************************************************************************************************************
* File:MessageEncryptor.java
* Project: Assignment A3
* Internal Methods:
*	String encrypt(String Data)
*   String decrypt(String encryptedData)
*   Boolean isGranted(Message msg)
*
******************************************************************************************************************/

/**
 *
 * @author Tang Lekhaka
 */
package SecurityPackage;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import MessagePackage.Message;

public class MessageEncryptor {
	private static final String algorithm = "AES";
    private static final String secretKey = SecurityConstants.secretKey;

    private static Key generateKey() throws Exception {
        byte[] keyValue = secretKey.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        keyValue = sha.digest(keyValue);
        keyValue = Arrays.copyOf(keyValue, 16);  
        Key key = new SecretKeySpec(keyValue, algorithm);
        return key;
    }

    public static String encrypt(String Data) throws Exception {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(algorithm);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(Data.getBytes());
            String encryptedValue = DatatypeConverter.printBase64Binary(encVal);
            return encryptedValue;
    }

    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(algorithm);
        c.init(Cipher.DECRYPT_MODE, key);       
        byte[] decordedValue = DatatypeConverter.parseBase64Binary(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
    
    public static Boolean isGranted(Message msg) throws Exception{
    	String token = decrypt(msg.GetEncryptedToken());
    	
    	if (msg.GetMessageId() == Integer.parseInt(token))
    		return true;
    	
    	return false;
    }
	
	// For testing purpose
    public static void main(String[] args) {
		// TODO Auto-generated method stub
		Message msg = new Message(2);
		msg.SetEncryptedToken("5l8l8q6OJMLP8YKSCQy1Tw==");
		
		try {
			if (isGranted(msg))
			{
				System.out.println("Granted!");
			}
			else
			{
				System.out.println("Blocked!");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Blocked!");
			//e.printStackTrace();
		}
	}
}
