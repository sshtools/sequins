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

import java.io.IOException;

import org.junit.Test;

public class TestProgress {

	private static final int DELAY = 1500;

	@Test
	public void testWithPercentageAndIndeterimate() throws Exception {
		var terminal = Terminal.create();
		
		int test = 1;

		var bldr = terminal.progressBuilder();
//		terminal.messageln("------ Default -----");
//		doTests(terminal, test, bldr);
//		
//		bldr = terminal.progressBuilder();
//		terminal.messageln("------ With %age -----");
//		bldr.withPercentageText();
//		doTests(terminal, test, bldr);
//
//		bldr = terminal.progressBuilder();
//		terminal.messageln("------ With indeterminate -----");
//		bldr.withHideCursor();
//		bldr.withIndeterminate();
//		doTests(terminal, test, bldr);

		bldr = terminal.progressBuilder();
		terminal.messageln("------ With %aget and indeterminate -----");
		bldr.withIndeterminate();
		bldr.withHideCursor();
		bldr.withPercentageText();
		doTests(terminal, test, bldr);

//		terminal.messageln("------ With %aget and indeterminate and root message -----");
//		bldr = terminal.progressBuilder("With a root {0}", "message");
//		bldr.withIndeterminate();
//		bldr.withHideCursor();
//		bldr.withPercentageText();
//		doTests(terminal, test, bldr);

	}

	private static void doTests(Terminal terminal, int test, ProgressBuilder bldr)
			throws InterruptedException, IOException {
		
//		test = printTestNo(terminal, test);		
//		try(var progress = bldr.build()) {
//			progress.message(Level.NORMAL, "First row");
//			for(int i = 0 ; i <= 10; i++) {
//				progress.progressPercentage(i * 10);
//				sleep(DELAY);
//			}
//		}
//
//		test = printTestNo(terminal, test);		
//		try(var progress = bldr.build()) {
//			for(int i = 0 ; i <= 10; i++) {
//				progress.progressed(i * 10, "Count {0}", i);
//				sleep(DELAY);
//			}
//		}
//
//		test = printTestNo(terminal, test);		
//		try(var progress = bldr.build()) {
//			progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
//			sleep(DELAY);
//			progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
//			sleep(1000);
//			for(int i = 0 ; i <= 10; i++) {
//				progress.progressPercentage(i * 10);
//				sleep(DELAY);
//			}
//		}
//
//		test = printTestNo(terminal, test);		
//		try(var progress = bldr.build()) {
//			progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
//			sleep(DELAY);
//			progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
//			sleep(1000);
//			for(int i = 0 ; i <= 10; i++) {
//				progress.progressMessage("Count {0}", i);
//				sleep(DELAY);
//			}
//		}
//
//		test = printTestNo(terminal, test);		
//		try(var progress = bldr.build()) {
//			progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
//			sleep(DELAY);
//			progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
//			sleep(1000);
//			for(int i = 0 ; i <= 10; i++) {
//				progress.progressed(Optional.of(i * 10), Optional.of("Count {0}"), i);
//				sleep(DELAY);
//			}
//		}
		
//		test = printTestNo(terminal, test);		
//		try(var progress = bldr.build()) {
//			progress.message(Level.NORMAL, "Parallel Jobs");
//			sleep(DELAY);
//			var l = new ArrayList<Progress>();
//			for(int i = 0 ; i <= 10; i++) {
//				var prg = progress.newJob();
//				l.add(prg);
//				prg.progressed(0, "Thread " + i);
//				sleep(DELAY);
//			}
//			for(int j = 0 ; j < 100 ; j++) {
//				for(int i = 0 ; i <= 10; i++) {
//					var prg = l.get(i);
//					prg.progressed(j, "Thread " + i);
//					sleep(DELAY / 100);
//				}
//			}
//			while(!l.isEmpty()) {
//				var p = l.remove(0);
//				p.close();
//			}
//		}
		
//		test = printTestNo(terminal, test);
//		try(var progress = bldr.build()) {
//			progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
//			sleep(DELAY);
//			progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
//			sleep(1000);
//			try(var innerProgress = progress.newJob("Inner Progress {0}", "1")) {
//				innerProgress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
//				sleep(DELAY);
//				innerProgress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
//				sleep(1000);
//				for(int i = 0 ; i <= 10; i++) {
//					innerProgress.progressed(Optional.of(i * 10), Optional.of("Count {0}"), i);
//					sleep(DELAY);
//				}
//			}
//		}

		terminal.messageln(terminal.createSequence().italicOn().str("{0} tests done").italicOff().toString(), test++);
	}

	private static int printTestNo(Terminal terminal, int test) {
		terminal.messageln("");
		terminal.messageln(terminal.createSequence().italicOn().str("Test {0}").italicOff().toString(), test++);
		terminal.messageln("");
		return test;
	}

}
