package wp.discord.bot.core.bot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CommandTokenizer {

	public static List<List<String>> tokenizeMultiLines(String command) {
		List<List<String>> cmdLines = new ArrayList<>();

		for (String line : command.split("\n")) {
			List<String> tokens = tokenize(line);
			cmdLines.add(tokens);
		}

		return cmdLines;
	}

	public static List<String> tokenize(String command) {
		List<String> cmdFragments = new ArrayList<>();

		StringBuilder sb = new StringBuilder();

		boolean literal = false;
		char[] array = command.trim().toCharArray();
		for (int i = 0; i < array.length; i++) {

			char c = array[i];
			if (c == '"') {
				literal = !literal;
				continue;
			}

			if (literal) {
				if (c == '\\') {
					sb.append(array[++i]);
				} else {
					sb.append(c);
				}
			} else if (Character.isWhitespace(c)) { // is white
				String token = sb.toString().trim();
				if (StringUtils.isNotEmpty(token)) {
					cmdFragments.add(token);
				}
				sb.setLength(0);
			} else {
				sb.append(c);
			}

		}

		String token = sb.toString().trim();
		if (StringUtils.isNotEmpty(token)) {
			cmdFragments.add(token);
		}

		return cmdFragments;
	}

}
