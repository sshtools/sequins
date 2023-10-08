package com.sshtools.sequins;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.jline.terminal.Terminal;

import com.sshtools.sequins.Bitmap.ImageDisplayMethod;
import com.sshtools.sequins.impl.JLineSequins;

public interface Sequins extends Prompter, DrawContext, Closeable {
	
	Optional<ImageDisplayMethod> imageDisplayMethod();

	static Sequins create() {
		try {
			return new JLineSequins();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	static Sequins create(Terminal terminal) {
		return new JLineSequins(terminal);
	}

	Terminal terminal();
	
	static boolean isYes(String str, boolean defaultIfNull) {
		return (str == null) ? defaultIfNull : str.toLowerCase().startsWith("y");
	}
	
	default boolean yesNo() {
		return isYes(prompt(createSequence().ch('[').boldOn().ch('Y').boldOff().str("]es,[N]o: ").toString()), true);
	}

	default boolean yesNo(String fmt, Object... args) {
		return yesNo(PromptContext.empty(), fmt, args);
	}

	default boolean noYes(String fmt, Object... args) {
		return noYes(PromptContext.empty(), fmt, args);
	}

	default String prompt(String fmt, Object... args) {
		return prompt(PromptContext.empty(), fmt, args);
	}

	default char[] password(String fmt, Object... args) {
		return password(PromptContext.empty(), fmt, args);
	}

	default boolean yesNo(PromptContext context, String fmt, Object... args) {
		return isYes(prompt(context, 
						createSequence().msg(fmt, args).str(" [").boldOn().ch('Y').boldOff().str("]es,[N]o: ").toString()), true);
	}
	
	default boolean noYes() {
		return isYes(prompt(createSequence().str("[Y]es,[").boldOn().ch('N').boldOff().str("]o: ").toString()), false);
	}

	default boolean noYes(PromptContext context, String fmt, Object... args) {
		return isYes(prompt(context,
						createSequence().msg(fmt, args).str(" [Y]es,[").boldOn().ch('N').boldOff().str("]o: ").toString()), false);
	}

	String prompt();

	String prompt(PromptContext context, String fmt, Object... args);

	char[] password();

	char[] password(PromptContext context, String fmt, Object... args);

	PrintWriter getWriter();

	PrintWriter getErrorWriter();

	Sequence createSequence();

	ProgressBuilder progressBuilder();
	
	default Sequins print(String text) {
		var wrt = getWriter();
		wrt.print(text);
		wrt.flush();
		return this;
	}
	
	default Sequins newline() {
		var wrt = getWriter();
		wrt.println();
		wrt.flush();
		return this;
	}
	
	default Sequins message(String message, Object... args) {
		var seq = createSequence();
		seq.msg(message, args);
		var wrt = getWriter();
		wrt.print(seq);
		wrt.flush();
		return this;
	}
	
	default Sequins messageln(String message, Object... args) {
		var seq = createSequence();
		seq.msg(message, args);
		var wrt = getWriter();
		wrt.println(seq);
		wrt.flush();
		return this;
	}

	default Sequins error(Throwable exception) {
		return error(null, exception);
	}

	default Sequins error(String message, Throwable exception, Object... args) {
		return error(false, message, exception, args);
	}
	
	default Sequins error(boolean showTrace, String message, Throwable exception, Object... args) {
		if(message != null) {
			errorln(message, args);
		}
		if(exception != null) {
			var seq = createSequence();
			seq.exception(exception, showTrace);
			var wrt = getErrorWriter();
			wrt.print(seq.toString());
			wrt.flush();
		}
		return this;
	}
	
	default Sequins error(String message, Object... args) {
		var seq = createSequence();
		seq.msg(message, args);
		var wrt = getErrorWriter();
		wrt.print(seq);
		wrt.flush();
		return this;
	}
	
	default Sequins errorln(String message, Object... args) {
		var seq = createSequence();
		seq.msg(message, args);
		var wrt = getErrorWriter();
		wrt.println(seq);
		return this;
	}

	default ProgressBuilder progressBuilder(String title, Object... args) {
		return progressBuilder().withMessage(title, args);
	}
	
	BitmapBuilder createBitmap();
	
	Sequins viewport(int height);
	
	void close();

	void clear();
}
