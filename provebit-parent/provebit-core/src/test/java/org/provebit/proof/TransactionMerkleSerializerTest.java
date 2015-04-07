package org.provebit.proof;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class TransactionMerkleSerializerTest {
	
	//using block 351043
	@Test
	public void testSerializer(){
		try {
			byte[]  blockID = Hex.decodeHex("000000000000307b75c9b213f61b2a0c429a34b41b628daae9774cb9b5ff1059".toCharArray());
	        byte[] transactionID = Hex.decodeHex("b6b3ff7b4d004a788c751f3f8fc881f96c7b647ae06eb9a720bddc924e6f9147".toCharArray());
	        
	        TransactionMerkleSerializer tm = new TransactionMerkleSerializer();
	        byte[] serial = tm.SerializedPathUpMerkle(transactionID, blockID);
	        assertTrue(null != serial && serial.length > 0);
	    	
	        // Check that the oath up the tree is correct
	        assertTrue((serial[0] == 0));
	        assertTrue((serial[33] == 1));
	        assertTrue((serial[66] == 1));
	   
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testConnectToWrongBlock(){
		try {
			byte[]  blockID = Hex.decodeHex("00000000000030775c9b2103f61b2a0c429a34b41b628daae9774cb9b5ff1059".toCharArray());
	        byte[] transactionID = Hex.decodeHex("b6b3ff7b4d004a788c751f3f8fc881f96c7b647ae06eb9a720bddc924e6f9147".toCharArray());
	        
	        TransactionMerkleSerializer tm = new TransactionMerkleSerializer();
	        byte[] serial = tm.SerializedPathUpMerkle(transactionID, blockID);
	        
	        // Shouldn't recognize transaction
	        assertTrue(serial == null);
	   
		} catch (DecoderException e) {
			fail();
		}
	}
	
	@Test
	public void testConnectToWrongTransaction(){
		try {
			byte[]  blockID = Hex.decodeHex("000000000000307b75c9b213f61b2a0c429a34b41b628daae9774cb9b5ff1059".toCharArray());
	        byte[] transactionID = Hex.decodeHex("b6b55f7b4d004a788c751f3f8fc881f96c7b647ae06eb9a720bddc924e6f9147".toCharArray());
	        
	        TransactionMerkleSerializer tm = new TransactionMerkleSerializer();
	        byte[] serial = tm.SerializedPathUpMerkle(transactionID, blockID);
	        
	        // Shouldn't recognize transaction
	        assertTrue(serial == null);
	   
		} catch (DecoderException e) {
			fail();
		}
	}

}
