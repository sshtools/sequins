package com.sshtools.sequins;

import org.junit.Test;

public class TestImage {

	@Test
	public void testPngImage() throws Exception {
		try(var term = Sequins.create()) {
			term.clear();
			var bitmapBldr = term.createBitmap();
			var img = bitmapBldr.build("logo.png", TestImage.class);
			img.draw();
		}
	}
}
