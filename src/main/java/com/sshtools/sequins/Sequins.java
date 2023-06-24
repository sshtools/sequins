package com.sshtools.sequins;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.jline.terminal.Terminal;

import com.sshtools.sequins.impl.JLineTerminal;

public interface Sequins extends com.sshtools.sequins.Terminal {

	static Sequins create() {
		try {
			return new JLineTerminal();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	static Sequins create(Terminal terminal) {
		return new JLineTerminal(terminal);
	}

	Terminal terminal();
}
