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

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import com.sshtools.sequins.Terminal;
import com.sshtools.sequins.TerminalFactory;

public class LinuxTerminalFactory implements TerminalFactory {

	@Override
	public Terminal create() {
		return new LinuxTerminal();
	}

	@Override
	public boolean isAvailable() {
		try {
			return new ProcessBuilder("stty", "-g").redirectError(Redirect.DISCARD).redirectOutput(Redirect.DISCARD).redirectInput(Redirect.INHERIT)
					.start().waitFor() == 0;
		} catch (IOException | InterruptedException e) {
			return false;
		}

	}

	@Override
	public int getWeight() {
		return Integer.MAX_VALUE;
	}

}
