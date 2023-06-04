package com.sshtools.sequins;

import java.util.Random;

import org.junit.Test;

import com.sshtools.sequins.Sequence.Color;

public class TestTerminal {

	@Test
	public void testHelloWorld() {
		Terminal.create().messageln("Hello World");
		
		// TODO assert
	}
	
	@Test
	public void testFormatter() {
		var terminal = Terminal.create();
		var rnd = new Random();
		terminal.messageln("Hello World, its {0} degrees outside today, with a {1}% chance of rain.", rnd.nextInt(-10, 50), rnd.nextInt(0, 100));
		
		// TODO assert
	}
	
	@Test
	public void testErrors() {
		var terminal = Terminal.create();
		try {
			throw new Exception("Bang!");
		}
		catch(Exception e) {
			terminal.errorln("Something has gone terribly wrong");
			terminal.error(e);
		}
		// TODO assert
	}
	
	@Test
	public void testPrompt() {

		var terminal = Terminal.create();
		var name = terminal.prompt("what is your name?");
		var age = terminal.prompt("Hello {0}, what is you age?", name);
		if(terminal.yesNo("Are you sure {0}, age {1}.", name, age)) {
			var pw = terminal.password("Ok then {0}, what is your password?", name);
			terminal.messageln("Thanks, storing password {0} for later shenannigans", new String(pw));
		}
		// TODO assert
	}
	
	@Test
	public void testSequence() {

		var terminal = Terminal.create();
		var seq = terminal.createSequence();
		var encoded = seq.str("With a ").boldOn().str("sequence").boldOff().
			str(" you can create a string of formatted and styled text, ").
			str("using ").bg(Color.RED).str("background").defaultBg().
			str(" and ").fg(Color.GREEN).str("foreground").defaultFg().
			str(" colours and text styles such as ").
			italic(true).str("italic").italic(false).ch(' ').
			boldOn().str("bold").boldOff().str(" and ").
			underlineOn().str("underline").underlineOff().ch('.').toString();
		
		var wrt = terminal.getWriter();
		wrt.write(encoded);
		wrt.flush();
		
	}

}
