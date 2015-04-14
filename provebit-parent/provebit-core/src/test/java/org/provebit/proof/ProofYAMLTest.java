package org.provebit.proof;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date jan1 = dateFormat.parse("01/01/2007"); 
		Date jan2 = dateFormat.parse("02/01/2007"); 
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
		System.out.println(checkStr);

		assertTrue(checkStr.equals(writeStr));
	}

}
