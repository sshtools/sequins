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
package com.sshtools.sequins.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.StringTokenizer;

import com.sshtools.sequins.Constraint;
import com.sshtools.sequins.ProgressBuilder;
import com.sshtools.sequins.Sequence;

public class LinuxTerminal extends FallbackTerminal {

	private static final long WIDTH_REFRESH_TIME = 1000;

	private long lastWidth = -1;
	private Constraint constraint = Constraint.of(80, 24);

	@Override
	protected DumbConsoleProgress createProgress(ProgressBuilder builder) {
		if(builder.hideCursor()) {
			System.out.println("Hiding cursor");
			var writer = getWriter();
			var shutdownHook = new Thread(() -> {
				writer.print(createSequence().csi().ch('?').num(25).ch('h').toString());
				writer.flush();
				System.out.println("Turned cursor back on");
			});
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			writer.print(createSequence().csi().ch('?').num(25).ch('l').toString());
			return doCreateProgress(builder, shutdownHook);
		}
		else
			return doCreateProgress(builder, null);
	}

	private DumbConsoleProgress doCreateProgress(ProgressBuilder builder, Thread shutdownHook) {
		var writer = getWriter();
		return new LinuxTerminalProgress(this, builder.indeterminate(), builder.spinnerStartDelay(), builder.percentageText(), 
				builder.message(), builder.args()) {
			@Override
			protected void onClosed() {
				if(builder.hideCursor()) {
					writer.print(createSequence().csi().ch('?').num(25).ch('h').toString());
					writer.flush();
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
				}
			}
		};
	}

	@Override
	public Constraint constraint() {
		if (lastWidth < System.currentTimeMillis() - WIDTH_REFRESH_TIME) {
			lastWidth = System.currentTimeMillis();
			var width = -1;
			var height = -1;
			try {
				width = Integer.parseInt(System.getenv("COLUMNS"));
			} catch (Exception e3) {
			}
			try {
				height = Integer.parseInt(System.getenv("LINES"));
			} catch (Exception e3) {
			}

			if (width == -1 || height == -1) {
				try {
					/* This is the only method that works with sudo */
					var p = new ProcessBuilder("tput", "cols", "lines").redirectError(Redirect.INHERIT)
							.redirectInput(Redirect.INHERIT).start();
					try (var in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
						var l = Integer.parseInt(in.readLine());
						if (width == -1)
							width = l;
						l = Integer.parseInt(in.readLine());
						if (height == -1) {
							height = l;
						}
					}
				} catch (Exception e) {
				}
			}

			if (width == -1 || height == -1) {
				try {
					var p = new ProcessBuilder("stty", "-a").redirectError(Redirect.INHERIT)
							.redirectInput(Redirect.INHERIT).start();
					try (var in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
						var st = new StringTokenizer(in.readLine());
						while (st.hasMoreTokens()) {
							var tkn = st.nextToken();
							if (tkn.equals("columns") && width == -1) {
								width = Integer.parseInt(st.nextToken());
							}
							if (tkn.equals("rows") && height == -1) {
								height = Integer.parseInt(st.nextToken());
							}
						}
					}
				} catch (Exception e2) {
				}
			}

			if (width == -1)
				width = 80;

			if (height == -1)
				height = 24;

			constraint = Constraint.of(width, height);
		}
		return constraint;

	}

	@Override
	public Sequence createSequence() {
		return new Sequence() {

			@Override
			public Sequence newSeq() {
				return createSequence();
			}

			@Override
			public Sequence shade(int repeat, Shade ch) {
				switch (ch) {
				case DARK_SHADE:
					return ch(repeat, '▓');
				case MEDIUM_SHADE:
					return ch(repeat, '▒');
				default:
					return ch(repeat, '░');
				}
			}

			@Override
			public Sequence box(BoxChar ch) {
				switch (ch) {
				case BOX_TOP_LEFT:
					return cp(0x6c);
				case BOX_TOP:
					return cp(0x71);
				case BOX_TOP_MIDDLE:
					return cp(0x77);
				case BOX_TOP_RIGHT:
					return cp(0x6b);
				case BOX_MIDDLE_LEFT:
					return cp(0x74);
				case BOX_MIDDLE:
					return cp(0x71);
				case BOX_MIDDLE_MIDDLE:
					return cp(0x6e);
				case BOX_MIDDLE_RIGHT:
					return cp(0x75);
				case BOX_BOTTOM_LEFT:
					return cp(0x6d);
				case BOX_BOTTOM:
					return cp(0x71);
				case BOX_BOTTOM_MIDDLE:
					return cp(0x76);
				case BOX_BOTTOM_RIGHT:
					return cp(0x6a);
				case BOX_LEFT:
					return cp(0x78);
				case BOX_RIGHT:
					return cp(0x78);
				case BOX_CENTER:
					return cp(0x78);
				default:
					return ch('*');
				}
			}

			@Override
			public Sequence gr(boolean gr) {
				return noTextAdvance(() -> esc().ch('(').ch(gr ? '0' : 'B'));
			}

			@Override
			public Sequence bold(boolean bold) {
				return noTextAdvance(() -> csi().num(bold ? 1 : 22).ch('m'));
			}

			@Override
			public Sequence italic(boolean italic) {
				return noTextAdvance(() -> csi().num(italic ? 3 : 23).ch('m'));
			}

			@Override
			public Sequence underline(boolean underline) {
				return noTextAdvance(() -> csi().num(underline ? 4 : 24).ch('m'));
			}

			@Override
			public Sequence strikeout(boolean strikeout) {
				return noTextAdvance(() -> csi().num(strikeout ? 9 : 29).ch('m'));
			}

			@Override
			public Sequence blink(boolean blink) {
				return noTextAdvance(() -> csi().num(blink ? 5 : 25).ch('m'));
			}

			@Override
			public Sequence inverse(boolean inverse) {
				return noTextAdvance(() -> csi().num(inverse ? 7 : 27).ch('m'));
			}

			@Override
			public Sequence off() {
				return noTextAdvance(() -> csi().num(0).ch('m'));
			}

			@Override
			public Sequence fg(Color color) {
				var ord = color.ordinal();
				if (ord < 8)
					return noTextAdvance(() -> csi().num(ord + 30).ch('m'));
				else
					return noTextAdvance(() -> csi().num(ord + 82).ch('m'));
			}

			@Override
			public Sequence defaultFg() {
				return noTextAdvance(() -> csi().num(39).ch('m'));
			}

			@Override
			public Sequence bg(Color color) {
				var ord = color.ordinal();
				if (ord < 8)
					return noTextAdvance(() -> csi().num(ord + 40).ch('m'));
				else
					return noTextAdvance(() -> csi().num(ord + 92).ch('m'));
			}

			@Override
			public Sequence defaultBg() {
				return noTextAdvance(() -> csi().num(49).ch('m'));
			}

			@Override
			public Sequence eraseLine() {
				return csi().num(2).ch('K');
			}

			@Override
			public Sequence cub(int repeat) {
				return noTextAdvance(() -> csi().num(repeat).ch('D'));
			}
		};
	}

}
