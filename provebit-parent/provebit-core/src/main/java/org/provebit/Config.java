package org.provebit;

import java.io.File;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

public class Config {

	static {
		setDirectory(new File("."));
		setBitcoinNet(MainNetParams.get());
	}
	
	static private File directory;
	static private NetworkParameters params;
	
	public static void setDirectory(File directory) {
		if (!directory.isDirectory())
			throw new RuntimeException("trying to set file as directory");
		Config.directory = directory;
	}
	
	public static File getDirectory() {
		if (directory == null)
			throw new RuntimeException("directory not initialized");
		return directory;
	}
	
	public static void setBitcoinNet(NetworkParameters params) {
		Config.params = params;
	}
	
	public static NetworkParameters getBitcoinNet() {
		return params;
	}
}
