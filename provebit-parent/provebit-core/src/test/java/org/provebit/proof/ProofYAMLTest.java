package org.provebit.proof;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	
	@Test
	public void testYAMLParse() throws ParseException, DecoderException, IOException{
		// Timestamp iTime, Timestamp pTime, byte[] path, byte[] transID, byte[] root, byte[] bID, byte[] fileHash
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
		Proof testP = new Proof(iTime, pTime, path, transID, root, bID, fileHash );
		testP.writeProofToFile(writeFile);
		
		File write = new File(writeFile);
		File check = new File(checkFile);
		String writeStr = FileUtils.readFileToString(write);
		String checkStr = FileUtils.readFileToString(check);

		assertTrue(checkStr.equals(writeStr));
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

}
