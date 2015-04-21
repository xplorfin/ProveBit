package org.provebit.proof;
import static org.junit.Assert.*;

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
	        assertTrue((serial[0] == 1));
	        assertTrue((serial[33] == 0));
	        assertTrue((serial[66] == 0));
	   
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
	
	@Test
	public void testLargeBlock(){
		try {
			byte[]  blockID = Hex.decodeHex("0000000000000000025ff38dcf41c802689454007a298ee6b9f66dec9f863089".toCharArray());
	        byte[] transactionID = Hex.decodeHex("a0f2f6a18a209bb5750a9345a51f6a29c49ac3c53d15306d4950892ebabfe561".toCharArray());
	        /*
	         * TODO: validate this is the path
	         * ["09053a177806bf34e6624f89fb386fd793b6537470fe3d11cf12b38601b7bcda", "right"],
			 * ["a45916cd41fa66b4fd466b55f535527804ca5a9f971a417b9be0dd15b801fe46", "left"],
			 * ["ad75aede7ecb36bb774baa5d1ffb14540665f545ae85bf6d99471148701a4d21", "left"],
			 * ["bfe57aa9d5b99539411e5af113cc0acedeb5f2c4835676bce994799123e3b52d", "left"],
			 * ["7ebf5cc401583364f727ad68e41d8b6f1b5dbc1643024c554bbfa39f71ed18d3", "right"],
			 * ["a7b199af38bda1a3fd569c1ce9d71a9c2b8d370df61ef1c56099f463be192f5c", "left"],
			 * ["8a7df191c685a07c8a76539c70fc6f6f035398bca15decad5ea2969911d50e62", "left"],
			 * ["ddac47209f0d0d684db734629e44f5c7ce2b0db0ff2c4e5bc6f621463ed94a7e", "right"],
			 * ["fc745d58336a5bdb49b9e31f5453f365ae823e39cafb967f9f222eb406576b2a", "right"],
			 * ["a9d2df97ae08169cb43cc020aa46b43524d9aa2fc038324e42d5933c8f4c6b32", "right"],
			 * ["1f07ead695e19862ae1c5aa15baf0186b50d4bdc9c134fb0ba881c2034152f98", "left"],
	         */
	        TransactionMerkleSerializer tm = new TransactionMerkleSerializer();
	        byte[] serial = tm.SerializedPathUpMerkle(transactionID, blockID);
	        assertTrue(serial.length > 0);
	        assertNotNull(serial);
	        System.out.println(Hex.encodeHex(serial));
	   
		} catch (DecoderException e) {
			fail();
		}
	}

}
