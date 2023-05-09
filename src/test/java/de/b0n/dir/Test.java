package de.b0n.dir;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

public class Test {
	protected void assertListContainsLineEndingWith(List<String> lines, String ending) {
		for (String line : lines) {
			if (line.endsWith(ending)) {
				return;
			}
		}
		fail("No line ends with " + ending);
	}
}
