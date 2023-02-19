package com.sshtools.sequins;

public interface Prompter {
	static boolean isYes(String str, boolean defaultIfNull) {
		return (str == null) ? defaultIfNull : str.toLowerCase().startsWith("y");
	}
	
	boolean yesNo();

	boolean yesNo(String fmt, Object... args);

	boolean noYes();

	boolean noYes(String fmt, Object... args);

	String prompt();

	String prompt(String fmt, Object... args);

	char[] password();

	char[] password(String fmt, Object... args);
}
