package com.sshtools.sequins;

import org.junit.Test;

public class TestViewport {

	@Test
	public void testViewportAtTopOfScreen() throws Exception {
		try(var term = Sequins.create()) {
			term.clear();
			term.messageln("This line is not part of the viewport");
			term.messageln("And this line is not part of the viewport");
			try(var vp = term.viewport(10)) {
				for(int i = 1 ; i <= 15; i++) {
					term.messageln("Line {0}", i);
				}
				Thread.sleep(10000);
				term.clear();
			}
			term.messageln("And this third line is not part of the viewport");
		}
	}
}
