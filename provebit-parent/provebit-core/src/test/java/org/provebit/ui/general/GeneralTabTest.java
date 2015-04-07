package org.provebit.ui.general;

import org.junit.*;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;

public class GeneralTabTest extends UISpecTestCase {
	private Panel panel;
	
	@Before
	public void setUp() {
		panel = new Panel(new GeneralView(new GeneralModel()));
	}
	
	@Test
	public void testTable() {
		assertTrue(panel.getTable().getHeader().contentEquals(new String[] {
				"File", "Status"
		}));
	}
}
