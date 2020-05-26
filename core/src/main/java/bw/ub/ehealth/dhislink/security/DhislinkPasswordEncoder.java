package bw.ub.ehealth.dhislink.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DhislinkPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence stringToEncrypt) {
		
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-512");
            //Add password bytes to digest
            md.update(stringToEncrypt.toString().getBytes());
            //Get the hash's bytes 
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		
		return this.encode(rawPassword).equals(encodedPassword);
	}

}
