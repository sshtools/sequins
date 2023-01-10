/**
 * Copyright © 2023 JAdaptive Limited (support@jadaptive.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sshtools.sequins;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.CharBuffer;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class Sequence {
	
	final static Sequence EMPTY = new Sequence(true);
	
	public enum Color {
		BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE, BRIGHT_BLACK, BRIGHT_RED,
		BRIGHT_GREEN, BRIGHT_YELLOW, BRIGHT_BLUE, BRIGHT_MAGENTA, BRIGHT_CYNA, BRIGHT_WHITE
	}
	
	public enum BoxChar {
		BOX_TOP_LEFT,
		BOX_TOP,
		BOX_TOP_MIDDLE,
		BOX_TOP_RIGHT,
		BOX_MIDDLE_LEFT,
		BOX_MIDDLE,
		BOX_MIDDLE_MIDDLE,
		BOX_MIDDLE_RIGHT,
		BOX_BOTTOM_LEFT,
		BOX_BOTTOM,
		BOX_BOTTOM_MIDDLE,
		BOX_BOTTOM_RIGHT,
		BOX_LEFT,
		BOX_RIGHT ,
		BOX_CENTER;
		
	}

	public final static char NL = 0x0a;
	public final static char CR = 0x0d;
	public final static char VT = 0x0b;
	public final static char TAB = '\t';
	public final static char NUL = (char) 0;
	public final static char ESC = 0x1b;
	public final static char BEL = 0x07;
	public final static char HTS = 136;
	public final static char IND = 0x84;
	public final static char NEL = 0x85;
	public final static char RI = 0x8d;
	public final static char ENQ = 0x05;
	public final static char FF = 0x0c;
	public final static char XON = 0x11;
	public final static char XOFF = 0x13;

	public final static char CSI = 0x9b;
	public final static char DCS = 0x90;
	public static final char SS2 = 0x8e;
	public static final char SS3 = 0x8f;
	public static final char ST = 0x9c;
	public static final char OSC = 0x9d;
	public static final char SOS = 0x98;
	public static final char PM = 0x9e;
	public static final char APC = 0x9f;
	public static final char SO = 0x0e;
	public static final char SI = 0x0f;

	private PrintWriter writer;
	private StringWriter buffer;
	private int textLength;
	private int maxTextLength = Integer.MAX_VALUE;
	private final boolean readOnly;
	private boolean textAdvance = true;

	Sequence(boolean readOnly) {
		this.readOnly = readOnly;
		if(!readOnly) {
			buffer = new StringWriter();
			this.writer = new PrintWriter(buffer, true);
		}
	}

	public Sequence() {
		this(false);
	}
	
	public Sequence noTextAdvance(Callable<Sequence> callable) {
		var was = textAdvance;
		textAdvance = false;
		try {
			return callable.call();
		} catch(RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new IllegalStateException("Failed.", e);
		}
		finally {
			textAdvance = was;
		}
	}
	
	public int maxTextLength() {
		return maxTextLength;
	}

	public Sequence maxTextLength(int maxTextLength) {
		this.maxTextLength = maxTextLength;
		return this;
	}

	public int textLength() {
		return textLength;
	}

	public Sequence newSeq() {
		return new Sequence();
	}

	public final Sequence seq(Sequence seq) {
		if(textAdvance) {
			textLength += seq.textLength;
		}
		noTextAdvance(() -> str(1, seq.toString()));
		return this;
	}

	public final Sequence esc() {
		return noTextAdvance(() -> ch(ESC));
	}

	public final Sequence sep() {
		return noTextAdvance(() -> ch(';'));
	}

	public final Sequence csi() {
		return noTextAdvance(() -> ch(ESC).ch('['));
	}

	public final Sequence apc() {
		return noTextAdvance(() -> ch(ESC).ch('_'));
	}

	public final Sequence b(byte character) {
		return b(1, character);
	}

	public final Sequence b(int repeat, byte character) {

		if(readOnly)
			throw new IllegalStateException("Read only.");
		
		if(textAdvance) {
			if(textLength + repeat > maxTextLength) {
				repeat = maxTextLength - textLength;
			}
			textLength += repeat;
		}
		
		for(int i = 0 ; i < repeat; i++)
			writer.print((char) (character & 0xff));
		return this;
		
	}

	public final Sequence ch(char character) {
		return ch(1, character);
	}

	public final Sequence ch(int repeat, char character) {

		if(readOnly)
			throw new IllegalStateException("Read only.");
		
		if(textAdvance) {
			if(textLength + repeat > maxTextLength) {
				repeat = maxTextLength - textLength;
			}
			textLength += repeat;
		}
		for(int i = 0 ; i < repeat; i++)
			writer.print(character);

		return this;
	}

	public final Sequence cp(int codepoint) {
		return cp(1, codepoint);
	}

	public final Sequence cp(int repeat, int codepoint) {
		return str(repeat, CharBuffer.wrap(Character.toChars(codepoint)));
	}

	public final Sequence num(int number) {
		return num(1, number);
	}

	public final Sequence num(int repeat, int number) {
		return str(repeat, String.valueOf(number));
	}

	public final Sequence str(char[] string) {
		return str(1, string);
	}

	public final Sequence str(int repeat, char[] string) {
		return str(repeat, CharBuffer.wrap(string));
	}

	public final  Sequence str(Object string) {
		return str(1, string);
	}

	public final Sequence str(int repeat, Object string) {
		
		if(readOnly)
			throw new IllegalStateException("Read only.");
		
		var str = String.valueOf(string);
		for(int i = 0 ; i < repeat; i++) {
			var len = str.length();
			try {
				if(textAdvance && textLength + len > maxTextLength) {
					len = maxTextLength - textLength;
					str = str.substring(0, len);
					writer.print(str);
					break;
				}
				else
					writer.print(str);
			}
			finally {
				if(textAdvance)
					textLength += len;
				
			}
		}

		
		return this;
	}
	
	public final Sequence span(char[] string, int width) {
		return span((Object)CharBuffer.wrap(string), width);
	}

	public final Sequence span(Object string, int width) {
		var str = String.valueOf(string);
		if(str.length() > width) {
			var el = Math.min(str.length(), 3);
			noTextAdvance(() -> str(1, str.substring(0, width - el) + "...".substring(0, el)));
		}
		else if(str.length() < width) {
			noTextAdvance(() -> str(1, string));
			return noTextAdvance(() -> ch(width - str.length(), ' '));
		}
		textLength += width;
		return this;
	}

	public final Sequence st() {
		return noTextAdvance(() -> ch(ESC).ch('\\'));
	}

	public final  Sequence cr() {
		return cr(1);
	}

	public final Sequence cr(int repeat) {
		return noTextAdvance(() -> ch(repeat, CR));
	}
	
	public Sequence box(BoxChar ch) {
		switch(ch) {
		case BOX_TOP:
		case BOX_BOTTOM:
		case BOX_MIDDLE:
			return ch('-');
		case BOX_TOP_LEFT:
		case BOX_TOP_MIDDLE:
		case BOX_TOP_RIGHT:
		case BOX_MIDDLE_LEFT:
		case BOX_MIDDLE_MIDDLE:
		case BOX_MIDDLE_RIGHT:
		case BOX_BOTTOM_LEFT:
		case BOX_BOTTOM_MIDDLE:
		case BOX_BOTTOM_RIGHT:
			return ch('+');
		case BOX_LEFT:
		case BOX_RIGHT:
		case BOX_CENTER:
			return ch('|');
		default:
			return ch('*');
		}
	}

	public Sequence gr(boolean gr) {
		return this;
	}

	public Sequence off() {
		return this;
	}

	public final Sequence grOn() {
		return gr(true);
	}

	public final Sequence grOff() {
		return gr(false);
	}

	public Sequence bold(boolean bold) {
		return this;
	}

	public final Sequence boldOn() {
		return bold(true);
	}

	public final Sequence boldOff() {
		return bold(false);
	}

	public Sequence italic(boolean italic) {
		return this;
	}

	public final Sequence italicOn() {
		return italic(true);
	}

	public final Sequence italicOff() {
		return italic(false);
	}

	public Sequence underline(boolean underline) {
		return this;
	}

	public final Sequence underlineOn() {
		return underline(true);
	}

	public final Sequence underlineOff() {
		return underline(false);
	}

	public Sequence strikeout(boolean strikeout) {
		return this;
	}

	public final Sequence strikeoutOn() {
		return strikeout(true);
	}

	public final Sequence strikeoutOff() {
		return strikeout(false);
	}

	public Sequence inverse(boolean inverseinverse) {
		return this;
	}

	public final Sequence inverseOn() {
		return inverse(true);
	}

	public final Sequence inverseOff() {
		return inverse(false);
	}

	public Sequence blink(boolean blink) {
		return this;
	}

	public final Sequence blinkOn() {
		return blink(true);
	}

	public final Sequence blinkOff() {
		return blink(false);
	}
	
	public Sequence fg(Color color) {
		return this;
	}
	
	public Sequence defaultFg() {
		return this;
	}
	
	public Sequence bg(Color color) {
		return this;
	}
	
	public Sequence defaultBg() {
		return this;
	}

	public final Sequence tab() {
		return tab(1);
	}

	public final Sequence tab(int repeat) {
		return noTextAdvance(() -> ch(repeat, TAB));
	}

	public final Sequence nl() {
		return nl(1);
	}

	public final Sequence nl(int repeat) {
		return noTextAdvance(() -> ch(repeat, NL));
	}

	public final Sequence yesNo(boolean yes) {
		return str(yes ? "Yes" : "No");
	}

	public final Sequence nul() {
		return nul(1);
	}

	public final Sequence nul(int repeat) {
		return noTextAdvance(() -> ch(repeat, NUL));
	}

	public final Sequence msg(String pattern, Object... args) {
		return msg(1, pattern, args);
	}

	public final Sequence msg(int repeat, String pattern, Object... args) {
		if(textAdvance)
			textLength += MessageFormat.format(pattern, args).length();
		pattern = pattern.replace("{", newSeq().boldOn().toString() + "{");
		pattern = pattern.replace("}", "}" + newSeq().boldOff().toString());
		var fPattern = pattern;
		noTextAdvance(() -> str(repeat, MessageFormat.format(fPattern, args)));
		return this;
	}

	public final Sequence fmt(String pattern, Object... args) {
		return fmt(1, pattern, args);
	}

	public final Sequence fmt(int repeat, String pattern, Object... args) {
		if(textAdvance)
			textLength += String.format(pattern, args).length();
		var p = Pattern.compile("(\\%[0-9\\-\\.]*[a-z]+)");
		var m = p.matcher(pattern);
		if(m.find()) {
			pattern = m.replaceAll((r) -> newSeq().boldOn().str(r.group(0)).boldOff().toString());
		}
		var fPattern = pattern;
		noTextAdvance(() -> str(repeat, String.format(fPattern, args)));
		return this;
	}

	@Override
	public String toString() {
		return buffer == null ? "" : buffer.toString();
	}
	
	public Sequence size(long bytes) {
		var size = bytes;
		var unit = "";
		if (size > 9999) {
			size = size / 1024;
			unit = "KiB";
			if (size > 9999) {
				size = size / 1024;
				unit = "MiB";
				if (size > 9999) {
					size = size / 1024;
					unit = "GiB";
					if (size > 9999) {
						size = size / 1024;
						unit = "TiB";
						if (size > 9999) {
							size = size / 1024;
							unit = "PiB";
						}
					}
				}
			}
		}
		str(String.valueOf(size));
		italic(true);
		ch(' ');
		str(unit);
		italic(false);
		return this;
	}

	public static String repeat(String s, int times) {
		StringBuilder bui = new StringBuilder();
		for (int i = 0; i < times; i++) {
			bui.append(s);
		}
		return bui.toString();
	}



	public boolean emptyText() {
		return textLength == 0;
	}
	
	public boolean readOnly() {
		return readOnly;
	}

	public static Sequence empty() {
		return EMPTY;
	}
	
}
