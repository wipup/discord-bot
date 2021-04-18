package wp.discord.bot.task.get;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.RoleEnforcer;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class GetLogTask {

	private static final String LOG_PATH = "../logs"; // must match logback's configuration
	private static final int MAX_LOG_FILE_LIST_COUNT = 20;

	@Autowired
	private RoleEnforcer roleEnforcer;

	public void getLogs(BotAction action) throws Exception {
		roleEnforcer.allowOnlyAdminOrHigher(action);

		String name = StringUtils.defaultIfBlank(action.getFirstEntitiesParam(CmdEntity.NAME), "*");
		Path logDir = getLogBaseDir();
		log.debug("reading log directory: {}", logDir);

		AntPathMatcher antMatcher = new AntPathMatcher();

		try (Stream<Path> paths = Files.list(logDir)) {
			List<Path> logFiles = paths.filter(Files::isRegularFile) //
					.filter((p) -> antMatcher.match(name, p.getFileName().toString())) //
					.map((p) -> p.toAbsolutePath()) //
					.collect(Collectors.toList());

			if (logFiles.isEmpty()) {
				Reply r = Reply.of().literal("Not found log files matching pattern: ").code(name);
				action.getEventMessageChannel().sendMessage(r.build()).queue();
				return;

			} else if (logFiles.size() == 1) {
				sendLogFiles(action, logFiles.get(0));
				return;
			} else {
				listLogFileNames(action, logFiles);
				return;
			}
		}
	}

	public void listLogFileNames(BotAction action, List<Path> logFiles) throws Exception {
		Reply reply = Reply.of().literal("Found ").code(String.valueOf(logFiles.size())).literal(" log files").newline();
		int i = 0;
		for (i = 0; i < logFiles.size() && i < MAX_LOG_FILE_LIST_COUNT; i++) {
			Path found = logFiles.get(i);
			String fileName = found.getFileName().toString();
			reply.literal(String.format("%2d.) ", i + 1)).code(fileName).newline();
		}

		if (i < logFiles.size()) {
			reply.literal("To see more, please use more specific pattern").newline();
		}
		reply.literal("To download log file, type: ").code("bot get log name <file-name>");

		action.getEventMessageChannel().sendMessage(reply.build()).queue();
	}

	public void sendLogFiles(BotAction action, Path logFile) throws Exception {
		log.info("sending log file: {}", logFile);
		
		String fileName = logFile.getFileName().toString();
		InputStream is = Files.newInputStream(logFile);
		action.getEventMessageChannel().sendFile(is, fileName).queue((m) -> {
			log.info("success send file: {}", logFile);
			SafeUtil.suppress(() -> is.close());
		}, (ex) -> {
			log.error("failed send file: {} ", logFile);
			SafeUtil.suppress(() -> is.close());
		});
	}

	public Path getLogBaseDir() {
		return Paths.get(LOG_PATH).toAbsolutePath();
	}

}
