package testing.Others;

import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


import GUI.PasswordDialog;

public class testEncrypt {

	public static void main(String[] args) throws Exception 
	{
		String t = "prueba test";
		//byte[] encryptData = t.getBytes( "UTF-8"  );		
		
		{
			String secretKey = "prueba";
						
	        byte[] salt = new byte[8];
	        SecureRandom sr = new SecureRandom( secretKey.getBytes( "UTF-8" ) );
	        sr.nextBytes( salt );
			SecretKeyFactory factory =
			    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec( secretKey.toCharArray(), salt, 10000, 128);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			byte[] iv = new byte[ 128 / 8 ];
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey, ivspec);

			byte[] encTxt = cipher.doFinal( secretKey.getBytes( "UTF-8" ) );
			
			System.out.println("testEncrypt.main() " + encTxt.length + " -> " + Arrays.toString( encTxt ));
			
			
			
			salt = new byte[8];
			sr = new SecureRandom( secretKey.getBytes( "UTF-8" ) );
	        sr.nextBytes( salt );
			factory =
			    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			spec = new PBEKeySpec( secretKey.toCharArray(), salt, 10000, 128);
			tmp = factory.generateSecret(spec);
			skey = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			
			
			cipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));
			String plaintext = new String(cipher.doFinal( encTxt ), "UTF-8");
			System.out.println(plaintext);
	        
		}

		/*
		PasswordDialog pdg = new PasswordDialog( null, "pruebaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		pdg.setVisible( true );

		System.out.println("testEncrypt.main() " + pdg.getState() );
		System.out.println("testEncrypt.main() " + pdg.getPassword() );
		
		pdg.setVisible( true );
		System.out.println("testEncrypt.main() " + pdg.getState() );
		System.out.println("testEncrypt.main() " + pdg.getPassword() );
		*/
		
	}

}
