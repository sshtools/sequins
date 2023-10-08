package com.sshtools.sequins;

public interface Bitmap extends Twidget {
	
	public enum ImageDisplayMethod {
		KITTY, SIXEL
	}
	
	public enum ImageSourceFormat {
		RGB, RGBA, PNG, GIF, JPEG, BMP
	}

}
