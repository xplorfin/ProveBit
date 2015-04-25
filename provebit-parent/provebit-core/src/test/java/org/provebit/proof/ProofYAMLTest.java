package org.provebit.proof;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ProofYAMLTest {
	
	private final String writeFile = "src/test/resources/org/provebit/proof/yaml_test_file.txt";
	private final String checkFile = "src/test/resources/org/provebit/proof/yaml_correct_file.txt";
	private final String partialCheckFile = "src/test/resources/org/provebit/proof/yaml_partial_correct_file.txt";
	
	@Test
	public void testYAMLDump() throws ParseException, DecoderException, IOException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Date jan1 = dateFormat.parse("2007-01-01 10:11:12.123"); 
		Date jan2 = dateFormat.parse("2007-01-02 10:11:15.111"); 
		Timestamp iTime = new Timestamp(jan1.getTime());
		Timestamp pTime = new Timestamp(jan2.getTime());

		
		byte [] path = Hex.decodeHex("00000000".toCharArray());
		byte [] transID = Hex.decodeHex("11111111".toCharArray());
		byte [] root = Hex.decodeHex("22222222".toCharArray());
		byte [] bID = Hex.decodeHex("33333333".toCharArray());
		byte [] fileHash = Hex.decodeHex("44444444".toCharArray());
		Proof testP = new Proof("testFile.txt", iTime, pTime, path, transID, root, bID, fileHash );
		testP.writeProofToFile(writeFile);
		
		File write = new File(writeFile);
		String writeStr = FileUtils.readFileToString(write);

		assertTrue(writeStr.contains("Transaction Path: '00000000'"));
		assertTrue(writeStr.contains("Proven Time: '2007-01-02 10:11:15.111'"));
		assertTrue(writeStr.contains("Block ID: '33333333'"));
	}
	
	@Test
	public void testYAMLLoad() throws FileNotFoundException, DecoderException, ParseException{
		File genFile = new File(checkFile);
		Proof testP = new Proof(genFile);
		byte [] bID = Hex.decodeHex("33333333".toCharArray());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Date jan1 = dateFormat.parse("2007-01-01 10:11:12.123");
		Timestamp iTime = new Timestamp(jan1.getTime());
		assertTrue(Arrays.equals(bID, testP.getBlockId()));
		assertTrue(testP.getIdealTime().equals(iTime));
	}
	
	@Test
	public void testPartialProofDump() throws ParseException, DecoderException, IOException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Date jan1 = dateFormat.parse("2007-01-01 10:11:12.123"); 
		Timestamp iTime = new Timestamp(jan1.getTime());

		byte [] transID = Hex.decodeHex("11111111".toCharArray());
		byte [] root = Hex.decodeHex("22222222".toCharArray());
		byte [] fileHash = Hex.decodeHex("44444444".toCharArray());
		
		Proof testP = new Proof("testFile.txt", iTime, transID, root, fileHash);
		testP.writeProofToFile(writeFile);
		
		File write = new File(writeFile);
		String writeStr = FileUtils.readFileToString(write);

		assertFalse(writeStr.contains("Transaction Path: '00000000'"));
		assertFalse(writeStr.contains("Proven Time: '2007-01-02 10:11:15.111'"));
		assertFalse(writeStr.contains("Block ID: '33333333'"));
		assertTrue(writeStr.contains("Transaction ID: '11111111'"));
	}
	
	@Test
	public void testPartialProofLoad() throws FileNotFoundException, DecoderException, ParseException{
		File genFile = new File(partialCheckFile);
		Proof testP = new Proof(genFile);
		byte [] fileHash = Hex.decodeHex("44444444".toCharArray());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Date jan1 = dateFormat.parse("2007-01-01 10:11:12.123");
		Timestamp iTime = new Timestamp(jan1.getTime());
		
		assertFalse(testP.isFullProof());
		assertTrue(Arrays.equals(fileHash, testP.getFileHash()));
		assertTrue(testP.getIdealTime().equals(iTime));
	}
	
	@Test
	public void testCompletingProof() throws FileNotFoundException, DecoderException, ParseException, UnsupportedEncodingException{
		File genFile = new File(partialCheckFile);
		Proof testP = new Proof(genFile);
		
		assertFalse(testP.isFullProof());
		
		byte [] path = Hex.decodeHex("00000000".toCharArray());
		byte [] bID = Hex.decodeHex("33333333".toCharArray());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Date jan2 = dateFormat.parse("2007-01-02 10:11:15.111"); 
		Timestamp pTime = new Timestamp(jan2.getTime());
		
		testP.completeProof(pTime, path, bID);
		assertTrue(testP.isFullProof());
		assertTrue(Arrays.equals(testP.getBlockId(),bID));

	}

}
