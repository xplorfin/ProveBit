package org.provebit.ui.main;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.provebit.ui.RunGUI;
import org.uispec4j.Button;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class MainViewTest extends UISpecTestCase {
	private Window window;
	
	public void setUp() throws Exception {
		super.setUp();
//		UISpec4J.setWindowInterceptionTimeLimit(1000000);
		setAdapter(new MainClassAdapter(RunGUI.class));
		window = getMainWindow();
	}
	
//	@Test
//	public void testGetAboutUs() {
//		window.getMenuBar().getMenu("About").getSubMenu("About Us").contentEquals("About Us");
//	}
	
	public void testAboutUs() {	
		WindowInterceptor
		.init(window.getMenuBar().getMenu("About").getSubMenu("About Us").triggerClick())
		.process(new WindowHandler("Test About Us Dialog") {
			public Trigger process(Window window) throws Exception {
				assertEquals("About Us", window.getTitle());
				return window.getButton("OK").triggerClick();
			}
		})
		.run();
	}
}
