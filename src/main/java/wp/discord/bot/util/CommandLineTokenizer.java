package wp.discord.bot.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class CommandLineTokenizer {

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

		List<String> previous = SafeUtil.get(() -> allLines.get(allLines.size() - 1));
		if (hasEscapeNewLine(previous)) {
			previous.remove(previous.size() - 1);
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
		char encloseLiteral = '"';
		
		char[] array = command.trim().toCharArray();
		for (int i = 0; i < array.length; i++) {

			char c = array[i];
			
			if (!literal) {
				if (c == '"' || c == '\'') {
					literal = true;
					encloseLiteral = c;
					continue;
				}
			}

			if (literal) {
				if (c == '\\') {
					if (i + 1 >= array.length) {
						// do nothing
					} else {
						char nextValue = array[++i];
						if (nextValue == 'n') { // \n
							sb.append('\n');
						} else if (nextValue == 't') {
							sb.append('\t');
						} else {
							sb.append(nextValue);
						}
					}
				} else {
					if (c == encloseLiteral) {
						literal = false;
						continue;
					}
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
