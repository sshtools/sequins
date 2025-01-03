package com.sshtools.sequins;

import static java.lang.Thread.sleep;

import com.sshtools.sequins.Progress.Level;

public class AAAAA {

	private static final long DELAY = 3000;

	public static void main(String[] args) throws Exception {
		var terminal = Sequins.create();
		var bldr = terminal.progressBuilder();
		bldr.withIndeterminate();
		bldr.withHideCursor();
		bldr.withPercentageText();
		
		try (var progress = bldr.build()) {
			progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
			sleep(DELAY);
			progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
			sleep(1000);
			for (int i = 0; i <= 10; i++) {
				progress.progressed(i * 10);
				sleep(DELAY);
			}
		}
	}
}
