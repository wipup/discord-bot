package wp.discord.bot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandLineTokenizer_tokenizeTest {

	@Test
	public void shouldEscapeDoubleQuoteInsideSingleQuoteQuotation() {
		String cmd = "bot add schedule name 'schedule\\'s name' message ' message with single quote here \"message\" ' ";
		List<String> cmds = CommandLineTokenizer.tokenize(cmd);

		int index = 0;
		assertEquals("bot", cmds.get(index++));
		assertEquals("add", cmds.get(index++));
		assertEquals("schedule", cmds.get(index++));
		assertEquals("name", cmds.get(index++));
		assertEquals("schedule's name", cmds.get(index++));
		assertEquals("message", cmds.get(index++));
		assertEquals("message with single quote here \"message\"", cmds.get(index++));
		assertTrue(cmds.size() == index);
	}

	@Test
	public void shouldEscapeSingleQuoteInsideDoubleQuoteQuotation() {
		String cmd = "bot add schedule name \"schedule's name\" message \" message with single quote here 'message' \" ";
		List<String> cmds = CommandLineTokenizer.tokenize(cmd);

		int index = 0;
		assertEquals("bot", cmds.get(index++));
		assertEquals("add", cmds.get(index++));
		assertEquals("schedule", cmds.get(index++));
		assertEquals("name", cmds.get(index++));
		assertEquals("schedule's name", cmds.get(index++));
		assertEquals("message", cmds.get(index++));
		assertEquals("message with single quote here 'message'", cmds.get(index++));
		assertTrue(cmds.size() == index);
	}

	@Test
	public void shouldHandleQuotationWithDoubleQuote() {
		String cmd = "bot add schedule name \"my name\" message \" should quote this \" ";
		List<String> cmds = CommandLineTokenizer.tokenize(cmd);

		int index = 0;
		assertEquals("bot", cmds.get(index++));
		assertEquals("add", cmds.get(index++));
		assertEquals("schedule", cmds.get(index++));
		assertEquals("name", cmds.get(index++));
		assertEquals("my name", cmds.get(index++));
		assertEquals("message", cmds.get(index++));
		assertEquals("should quote this", cmds.get(index++));
		assertTrue(cmds.size() == index);
	}

	@Test
	public void shouldHandleQuotationWithSingleQuote() {
		String cmd = "bot add schedule name 'my name' message ' should quote this ' ";
		List<String> cmds = CommandLineTokenizer.tokenize(cmd);

		int index = 0;
		assertEquals("bot", cmds.get(index++));
		assertEquals("add", cmds.get(index++));
		assertEquals("schedule", cmds.get(index++));
		assertEquals("name", cmds.get(index++));
		assertEquals("my name", cmds.get(index++));
		assertEquals("message", cmds.get(index++));
		assertEquals("should quote this", cmds.get(index++));
		assertTrue(cmds.size() == index);
	}

}
