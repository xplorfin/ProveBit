package org.provebit.utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ApplicationDirectory implements DirectoryAccessor {
	INSTANCE;
	
	private File applicationDirectory;

	private ApplicationDirectory() {
		final Logger ilog = LoggerFactory.getLogger(ApplicationDirectory.class);
		String workingDirectory;
		String OS = (System.getProperty("os.name")).toUpperCase();

		// get system's default application data folder
		if (OS.contains("WIN")) {
			// Windows compatibility check
			workingDirectory = System.getenv("AppData");
			workingDirectory += "\\..\\Local\\ProveBit";
		} else {
			// Linux
			workingDirectory = System.getProperty("user.home");
			if (OS.contains("MAC")) {
				// OS X compatibility check
				workingDirectory += "/Library/Application Support";
			}
			workingDirectory += "/.provebit";
		}

		// create ProveBit application folder if it does not exist
		applicationDirectory = new File(workingDirectory);
		if (!applicationDirectory.exists()) {
			if (!applicationDirectory.mkdir()) {
				//log.error("Error creating the application folder");
				ilog.error("Wat");
			}
		}
		//ilog.error("asdf");
	}

	public File getRoot() {
		return applicationDirectory;
	}
}
