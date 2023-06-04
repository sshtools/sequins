# Sequins

A library to help make line based console output in Java applications a bit prettier.

This is used in several [JADAPTIVE](https://jadaptive.com) applications such as a [Push SFTP](https://github.com/sshtools/push-sftp).

## Get Sequins

It is available in Maven Central. 

```xml
<dependency>
   <groupId>com.sshtools</groupId>
   <artifactId>sequins</artifactId>
   <version>0.0.2</version>
</dependency>
```

## Features

 * Nested progress output with animated spinners
 * Simple prompting (passwords, yes/no, text)
 * Style text with colors, bold, italic, underline and more
 * Draw tables with box drawing characters
 * Query terminal size
 * Basic fallback output (e.g. unsupported platforms, when no tty is availabe such as Eclipse IDE)
 * Zero runtime dependencies

## Anti-features

 * Will not support full screen features

## TODO

 * Unbuffered input support (like jline)
 * Better Windows support (cmd.exe, cygwin and terminal)
 * Tests

## Examples

### HelloWorld

```java
	Terminal.create().messageln("Hello World");
```

### HelloWorld Formatting

The formatter arguments will be bolded.

```java

		var terminal = Terminal.create();
		var rnd = new Random();
		terminal.messageln("Hello World, its {0} degrees outside today, with a {1}% chance of rain.", rnd.nextInt(-10, 50), rnd.nextInt(0, 100));
```

### Errors

Will output on `System.err` instead of `System.out`.

```java
var terminal = Terminal.create();
try {
	throw new Exception("Bang!");
}
catch(Exception e) {
	terminal.errorln("Something has gone terribly wrong");
	terminal.error(e);
}
```

### Prompting

Prompt for text, passwords, confirmation.

```java
var terminal = Terminal.create();
var name = terminal.prompt("what is your name?");
var age = terminal.prompt("Hello {0}, what is you age?", name);
if(terminal.yesNo("Are you sure {0}, age {1}.", name, age)) {
	var pw = terminal.password("Ok then {0}, what is your password?", name);
	terminal.messageln("Thanks, storing password {0} for later shenannigans", new String(pw));
}
```

### Sequences

With a sequence you can create a string of formatted and styled text, using background and foreground colours and text styles such as italic, bold, and underline.

```java
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
```

### Progress

Intended for long running tasks, where output might be line by line, or animated progress. Progress can be nested too (with the output of each nested level being indented further).

```java
var terminal = Terminal.create();
bldr = terminal.progressBuilder();
bldr.withIndeterminate();
bldr.withHideCursor();
bldr.withPercentageText();
try(var progress = bldr.build()) {
	progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
	sleep(DELAY);
	progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
	sleep(1000);
	for(int i = 0 ; i <= 10; i++) {
		progress.progressMessage("Count {0}", i);
		sleep(3000);
	}
}
```
