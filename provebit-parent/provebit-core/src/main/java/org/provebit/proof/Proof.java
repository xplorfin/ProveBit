package org.provebit.proof;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Shishir Kanodia, Noah Malmed
 * 
 * Note: A proof can be partial or full. 
 * A partial proof does not contain a transaction path, blockID or proof time,
 *  and is meant to represent a proof before it has been pushed onto the blockChain.
 * A full proof has all information and represents a proof after it has been pushed onto the blockchain
 *
 */
public class Proof {

	private String type = "Document Proof";
	private String fileName;
	private final String version = "0.1";
	private Date idealTime;
	private Date provenTime;
	private byte[] transactionPath;
	private byte[] transactionID;
	private byte[] merkleRoot;
	private byte[] blockID;
	private byte[] fileHash;
	
	private boolean fullProof;
	
	/**
	 * Full Proof constructor: Takes parameters for all values
	 * @param iTime
	 * @param pTime
	 * @param path
	 * @param transID
	 * @param root
	 * @param bID
	 * @param fileHash
	 */
	public Proof(String fileName, Date iTime, Date pTime, byte[] path, byte[] transID, byte[] root, byte[] bID, byte[] fileHash){
		this.fileName = fileName;
		fullProof = true;
		idealTime = iTime;
		provenTime = pTime;
		transactionPath = path;
		transactionID = transID;
		merkleRoot = root;
		blockID = bID;
		this.fileHash = fileHash;
	}
	
	/**
	 * Partial proof constructor: Takes parameters for only partial proof values
	 * @param iTime
	 * @param transID
	 * @param merkleRoot
	 * @param fileHash
	 */
	public Proof(String fileName, Date iTime, byte[] transID, byte[] merkleRoot, byte[] fileHash){
		this.fileName = fileName;
		fullProof = false;
		idealTime = iTime;
		transactionID = transID;
		this.merkleRoot = merkleRoot;
		this.fileHash= fileHash;
	}
	
	/**
	 * File constructor - Constructs a proof (full or partial) from a yaml file
	 * @param yamlFile
	 * @throws FileNotFoundException
	 * @throws DecoderException
	 */
	public Proof(File yamlFile) throws FileNotFoundException, DecoderException{
		InputStream yamlInput = new FileInputStream(yamlFile);
		Yaml parser = new Yaml();		
		Object parsedYaml = parser.load(yamlInput);
		if(parsedYaml instanceof Map<?, ?>){
			// Because we are the ones who put the data there we can expect it be in this form
			@SuppressWarnings("unchecked")
			Map<String,String> yamlMap = (Map<String,String>)parsedYaml;
			// If Proven Time is defined then it is a full proof
			fullProof = yamlMap.containsKey("Proven Time");
			parseMap(yamlMap);
		}
	}
	
	/**
	 * Helper function to parse a map pulled from a yaml file
	 * @param yamlMap
	 * @throws DecoderException
	 */
	private void parseMap(Map<String,String> yamlMap) throws DecoderException{
		if(fullProof){
			provenTime = Timestamp.valueOf(yamlMap.get("Proven Time"));
			transactionPath = Hex.decodeHex(yamlMap.get("Transaction Path").toCharArray());
			blockID = Hex.decodeHex(yamlMap.get("Block ID").toCharArray());
		}
		fileName = yamlMap.get("File Name");
		idealTime = Timestamp.valueOf(yamlMap.get("Ideal Time"));		
		transactionID = Hex.decodeHex(yamlMap.get("Transaction ID").toCharArray());
		merkleRoot = Hex.decodeHex(yamlMap.get("Merkle Root").toCharArray());		
		fileHash = Hex.decodeHex(yamlMap.get("File Hash").toCharArray());
}
	
	/**
	 * 
	 * @param provenTime
	 * @param transactionPath
	 * @param blockID
	 */
	public void completeProof(Timestamp provenTime, byte[] transactionPath, byte[] blockID){
		fullProof = true;
		this.provenTime = provenTime;
		this.transactionPath = transactionPath;
		this.blockID = blockID;
	}
	
	public boolean isFullProof(){
		return fullProof;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	/**
	 * @return the transaction_path
	 */
	public byte[] getTransaction_path() {
		return transactionPath;
	}

	/**
	 * @return the transactionID
	 */
	public byte[] getTransactionID() {
		return transactionID;
	}

	/**
	 * @return the merkleroot
	 */
	public byte[] getMerkleroot() {
		return merkleRoot;
	}

	/**
	 * @return the blockId
	 */
	public byte[] getBlockId() {
		return blockID;
	}

	/**
	 * @return the fileHash
	 */
	public byte[] getFileHash() {
		return fileHash;
	}
	
	public Date getIdealTime() {
		return idealTime;
	}


	public Date getProvenTime() {
		return provenTime;
	}

	/**
	 * Function to write a proof (full or partial) to a file
	 * @param file
	 */
	public void writeProofToFile(String file){

		Map<String, String> mapRep = new HashMap<String, String>();
		if (fullProof){
			mapRep.put("Proven Time", provenTime.toString());
			mapRep.put("Transaction Path", Hex.encodeHexString(transactionPath));
			mapRep.put("Block ID", Hex.encodeHexString(blockID));
		}
		mapRep.put("File Name", fileName);
		mapRep.put("Type", type);
		mapRep.put("Version", version);
		mapRep.put("Ideal Time", idealTime.toString());		
		mapRep.put("Transaction ID", Hex.encodeHexString(transactionID));
		mapRep.put("Merkle Root", Hex.encodeHexString(merkleRoot));		
		mapRep.put("File Hash", Hex.encodeHexString(fileHash));
		

		DumperOptions options = new DumperOptions();
		options.setPrettyFlow(true);
		
	    Yaml yaml = new Yaml(options);
	    FileWriter fw = null;
		try {
			fw = new FileWriter(new File(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    yaml.dump(mapRep, fw);
	}

}
