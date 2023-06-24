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

import static java.lang.Thread.sleep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.sshtools.sequins.Progress.Level;

public class TestProgress {

	private static final int DELAY = 1500;
	private Sequins terminal;
	private int test;

	@Before
	public void getTerminal() {
		terminal = Sequins.create();
		printTestNo(test++);
	}

	@Test
	public void testWithDefault1() throws Exception {
		test1(builderForDefault());
	}

	@Test
	public void testWithDefault2() throws Exception {
		test2(builderForDefault());
	}

	@Test
	public void testWithDefault3() throws Exception {
		test3(builderForDefault());
	}

	@Test
	public void testWithDefault4() throws Exception {
		test4(builderForDefault());
	}

	@Test
	public void testWithDefault5() throws Exception {
		test5(builderForDefault());
	}

	@Test
	public void testWithDefault6() throws Exception {
		test6(builderForDefault());
	}

	@Test
	public void testWithDefault7() throws Exception {
		test7(builderForDefault());
	}

	@Test
	public void testWithPercentage1() throws Exception {
		test1(builderForPercentage());
	}

	@Test
	public void testWithPercentage2() throws Exception {
		test2(builderForPercentage());
	}

	@Test
	public void testWithPercentage3() throws Exception {
		test3(builderForPercentage());
	}

	@Test
	public void testWithPercentage4() throws Exception {
		test4(builderForPercentage());
	}

	@Test
	public void testWithPercentage5() throws Exception {
		test5(builderForPercentage());
	}

	@Test
	public void testWithPercentage6() throws Exception {
		test6(builderForPercentage());
	}

	@Test
	public void testWithPercentage7() throws Exception {
		test7(builderForPercentage());
	}

	@Test
	public void testWithPercentageAndIndeterimateAndRootMessage1() throws Exception {
		test1(builderForPercentageAndIndeterimateAndRootMessage());
	}

	@Test
	public void testWithPercentageAndIndeterimateAndRootMessage2() throws Exception {
		test2(builderForPercentageAndIndeterimateAndRootMessage());
	}

	@Test
	public void testWithPercentageAndIndeterimateAndRootMessage3() throws Exception {
		test3(builderForPercentageAndIndeterimateAndRootMessage());
	}

	@Test
	public void testWithPercentageAndIndeterimateAndRootMessage4() throws Exception {
		test4(builderForPercentageAndIndeterimateAndRootMessage());
	}

	@Test
	public void testWithPercentageAndIndeterimateAndRootMessage5() throws Exception {
		test5(builderForPercentageAndIndeterimateAndRootMessage());
	}

	@Test
	public void testWithPercentageAndIndeterimateAndRootMessage6() throws Exception {
		test6(builderForPercentageAndIndeterimateAndRootMessage());
	}

	@Test
	public void testWithPercentageAndIndeterimateAndRootMessage7() throws Exception {
		test7(builderForPercentageAndIndeterimateAndRootMessage());
	}

	@Test
	public void testWithIndeterminate1() throws Exception {
		test1(builderForIndeterminate());
	}

	@Test
	public void testWithIndeterminate2() throws Exception {
		test2(builderForIndeterminate());
	}

	@Test
	public void testWithIndeterminate3() throws Exception {
		test3(builderForIndeterminate());
	}

	@Test
	public void testWithIndeterminate4() throws Exception {
		test4(builderForIndeterminate());
	}

	@Test
	public void testWithIndeterminate5() throws Exception {
		test5(builderForIndeterminate());
	}

	@Test
	public void testWithIndeterminate6() throws Exception {
		test6(builderForIndeterminate());
	}

	@Test
	public void testWithIndeterminate7() throws Exception {
		test7(builderForIndeterminate());
	}

	@Test
	public void testWithPercentageAndIndeterimate1() throws Exception {
		test1(builderForPercentageAndIndeterimate());
	}

	@Test
	public void testWithPercentageAndIndeterimate2() throws Exception {
		test2(builderForPercentageAndIndeterimate());
	}

	@Test
	public void testWithPercentageAndIndeterimate3() throws Exception {
		test3(builderForPercentageAndIndeterimate());
	}

	@Test
	public void testWithPercentageAndIndeterimate4() throws Exception {
		test4(builderForPercentageAndIndeterimate());
	}

	@Test
	public void testWithPercentageAndIndeterimate5() throws Exception {
		test5(builderForPercentageAndIndeterimate());
	}

	@Test
	public void testWithPercentageAndIndeterimate6() throws Exception {
		test6(builderForPercentageAndIndeterimate());
	}

	@Test
	public void testWithPercentageAndIndeterimate7() throws Exception {
		test7(builderForPercentageAndIndeterimate());
	}

	private void test1(ProgressBuilder bldr) throws IOException, InterruptedException {
		try (var progress = bldr.build()) {
			progress.message(Level.NORMAL, "First row");
			for (int i = 0; i <= 10; i++) {
				progress.progressed(i * 10);
				sleep(DELAY);
			}
		}
	}

	private void test2(ProgressBuilder bldr) throws IOException, InterruptedException {
		try (var progress = bldr.build()) {
			for (int i = 0; i <= 10; i++) {
				progress.progressed(Optional.of(i * 10), Optional.of("Count {0}"), i);
				sleep(DELAY);
			}
		}
	}

	private void test3(ProgressBuilder bldr) throws IOException, InterruptedException {
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

	private void test4(ProgressBuilder bldr) throws IOException, InterruptedException {
		try (var progress = bldr.build()) {
			progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
			sleep(DELAY);
			progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
			sleep(1000);
			for (int i = 0; i <= 10; i++) {
				progress.progressed("Count {0}", i);
				sleep(DELAY);
			}
		}

	}

	private void test5(ProgressBuilder bldr) throws IOException, InterruptedException {
		try (var progress = bldr.build()) {
			progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
			sleep(DELAY);
			progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
			sleep(1000);
			for (int i = 0; i <= 10; i++) {
				progress.progressed(Optional.of(i * 10), Optional.of("Count {0}"), i);
				sleep(DELAY);
			}
		}

	}

	private void test6(ProgressBuilder bldr) throws IOException, InterruptedException {
		try (var progress = bldr.build()) {
			progress.message(Level.NORMAL, "Parallel Jobs");
			sleep(DELAY);
			var l = new ArrayList<Progress>();
			for (int i = 0; i <= 10; i++) {
				var prg = progress.newJob();
				l.add(prg);
				prg.progressed(0, "Thread " + i);
				sleep(DELAY);
			}
			for (int j = 0; j < 100; j++) {
				for (int i = 0; i <= 10; i++) {
					var prg = l.get(i);
					prg.progressed(j, "Thread " + i);
					sleep(DELAY / 100);
				}
			}
			while (!l.isEmpty()) {
				var p = l.remove(0);
				p.close();
			}
		}

	}

	private void test7(ProgressBuilder bldr) throws IOException, InterruptedException {
		try (var progress = bldr.build()) {
			progress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
			sleep(DELAY);
			progress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
			sleep(1000);
			try (var innerProgress = progress.newJob("Inner Progress {0}", "1")) {
				innerProgress.message(Level.NORMAL, "Line 1. {0}", "Arg1");
				sleep(DELAY);
				innerProgress.message(Level.NORMAL, "Line 2. {0}", "Arg2");
				sleep(1000);
				for (int i = 0; i <= 10; i++) {
					innerProgress.progressed(Optional.of(i * 10), Optional.of("Count {0}"), i);
					sleep(DELAY);
				}
			}
		}
	}

	private ProgressBuilder builderForDefault() {
		terminal.messageln("------ Default -----");
		return terminal.progressBuilder();
	}

	private ProgressBuilder builderForPercentage() {
		terminal.messageln("------ With %age -----");
		var bldr = terminal.progressBuilder();
		bldr.withPercentageText();
		return bldr;
	}

	private ProgressBuilder builderForIndeterminate() {
		terminal.messageln("------ With indeterminate -----");
		var bldr = terminal.progressBuilder();
		bldr.withHideCursor();
		bldr.withIndeterminate();
		return bldr;
	}

	private ProgressBuilder builderForPercentageAndIndeterimate() {
		terminal.messageln("------ With %age and indeterminate -----");
		var bldr = terminal.progressBuilder();
		bldr.withIndeterminate();
		bldr.withHideCursor();
		bldr.withPercentageText();
		return bldr;
	}

	private ProgressBuilder builderForPercentageAndIndeterimateAndRootMessage() {
		terminal.messageln("------ With %age, indeterminate and root message -----");
		var bldr = terminal.progressBuilder("With a root {0}", "message");
		bldr.withIndeterminate();
		bldr.withHideCursor();
		bldr.withPercentageText();
		return bldr;
	}

	private int printTestNo(int test) {
		terminal.messageln("");
		terminal.messageln(terminal.createSequence().italicOn().str("Test {0}").italicOff().toString(), test++);
		terminal.messageln("");
		return test;
	}

}
