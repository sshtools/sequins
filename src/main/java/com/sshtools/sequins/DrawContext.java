package com.sshtools.sequins;

public interface DrawContext {
	
	Constraint constraint();

	default int getWidth() {
		return constraint().width();
	}
}
