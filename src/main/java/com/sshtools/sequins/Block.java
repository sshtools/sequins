package com.sshtools.sequins;

public class Block {


	public Block(Terminal terminal, int rows) {
		var w = terminal.getWriter();
		var seq = terminal.createSequence();
		for(int i = 0 ; i < rows ; i++) {
			if(i > 0)
				seq.nl().cr();
			seq.eraseLine();
		}
		w.print(seq.toString());
		w.flush();
	}
}
