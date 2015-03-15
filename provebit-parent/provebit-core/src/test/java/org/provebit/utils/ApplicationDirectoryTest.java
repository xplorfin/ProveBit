package org.provebit.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class ApplicationDirectoryTest {

	@Test
	public void testGetRoot() {
		assertTrue(ApplicationDirectory.INSTANCE.getRoot().exists());
	}

}
