package utils;

import model.Block;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockChainUtils {

	public static String calculateHash(String data) {
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] rawHash = digest.digest(data.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer(); 
			for (int i = 0; i < rawHash.length; i++) {
				String hex = Integer.toHexString(0xff & rawHash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}
		
			
	public static boolean validateBlock(Block block) {
		String dataToEncode = block.getPreviousHash() + Long.toString(block.getFirstTimestamp()) +
				Integer.toString(block.getNonce()) + block.getTransactions();
		String checkHash = calculateHash(dataToEncode);
		return (checkHash.equals(block.getHash()));
	}
	
}
