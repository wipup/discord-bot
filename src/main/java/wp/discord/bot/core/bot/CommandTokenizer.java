package wp.discord.bot.core.bot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import wp.discord.bot.util.SafeUtil;

public class CommandTokenizer {

	public static List<List<String>> tokenizeMultiLines(String multilines) {
		List<List<String>> allLines = new ArrayList<>();

		for (String line : multilines.split("([\n]|[\r][\n])")) {
			List<String> tokens = tokenize(line);

			// handle newline escape
			List<String> previous = SafeUtil.get(() -> allLines.get(allLines.size() - 1));
			if (hasEscapeNewLine(previous)) {
				previous.remove(previous.size() - 1);
				previous.addAll(tokens);
			} else {
				allLines.add(tokens);
			}
		}

		return allLines;
	}

	private static boolean hasEscapeNewLine(List<String> tokens) {
		if (CollectionUtils.isEmpty(tokens)) {
			return false;
		}
		String lastToken = tokens.get(tokens.size() - 1);
		return "\\".equals(lastToken.trim());
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
