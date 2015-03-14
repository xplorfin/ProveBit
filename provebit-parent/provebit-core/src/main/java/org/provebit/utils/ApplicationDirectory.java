package org.provebit.utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationDirectory {
	private static final Logger log = LoggerFactory.getLogger(ApplicationDirectory.class);
	private static File applicationDirectory;
	
	static {
		String workingDirectory;
		String OS = (System.getProperty("os.name")).toUpperCase();
		
		// get system's default application data folder
		if (OS.contains("WIN")) {
			workingDirectory = System.getenv("AppData");
			// TODO check windows works
			workingDirectory += "/Local/ProveBit";
		}
		else {
			// linux 
		    workingDirectory = System.getProperty("user.home");
		    workingDirectory += "/.provebit";
		    // for mac
		    if (OS.contains("MAC")) {
			    workingDirectory += "/Library/Application Support";
			    // TODO check mac works
				workingDirectory += "/.provebit";
		    }
		}
		
		// create ProveBit application folder if it does not exist
		applicationDirectory = new File(workingDirectory);
		if (!applicationDirectory.exists()) {
			if (!applicationDirectory.mkdir()) {
				log.error("Error creating the application folder");
			}
		}	
	}
	
	public static File getRoot() {
		return applicationDirectory;
	}
}
