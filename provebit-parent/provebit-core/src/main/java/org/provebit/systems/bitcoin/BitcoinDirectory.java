package org.provebit.systems.bitcoin;

import java.io.File;

import org.provebit.utils.ApplicationDirectory;
import org.provebit.utils.DirectoryAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BitcoinDirectory implements DirectoryAccessor {
	INSTANCE;
		
	private File bitcoinDirectory;
	
	private BitcoinDirectory() {
		final Logger ilog = LoggerFactory.getLogger(ApplicationDirectory.class);
		File approot = ApplicationDirectory.INSTANCE.getRoot();
		bitcoinDirectory = new File(approot, "bitcoin");
		if (!bitcoinDirectory.exists()) {
			bitcoinDirectory.mkdir();
			ilog.error("Could not create directory: " + bitcoinDirectory);
		}
	}
	
	public File getRoot() {
		return bitcoinDirectory;
	}

}
