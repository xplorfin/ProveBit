package org.provebit.daemon;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class DaemonTests {
    static String DAEMONDIR = "/tests/org/provebit/daemon/testDaemonDir";
    static String daemonDirPath;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        daemonDirPath = new java.io.File( "." ).getCanonicalPath() + DAEMONDIR;
    }

    @Test
    public void testLaunchDaemon() throws InterruptedException {
        fail("Not yet implemented");
    }
    
    @Test
    public void testLaunchNotDaemon() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testDetectFileAdd() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testDetectFileDelete() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testDetectFileChange() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testDetectDirectoryAdd() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testDetectDirectoryDelete() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testDetectDirectoryChange() {
        fail("Not yet implemented");
    }

}
