package wp.discord.bot.task.get;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.unit.DataSize;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.constant.CmdEntity;
import wp.discord.bot.core.RoleEnforcer;
import wp.discord.bot.core.TracingHandler;
import wp.discord.bot.exception.ActionFailException;
import wp.discord.bot.model.BotAction;
import wp.discord.bot.util.Reply;
import wp.discord.bot.util.SafeUtil;

@Component
@Slf4j
public class GetLogTask {

	private static final String LOG_PATH = "../logs"; // must match logback's configuration
	private static final int MAX_LOG_FILE_LIST_COUNT = 10;

	@Autowired
	private TracingHandler tracing;

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
				throw new ActionFailException(r);

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
			reply.literal(String.format("%2d.) ", i + 1)).append(createFileReply(found));
		}

		if (i < logFiles.size()) {
			reply.literal("To see more, please use more specific pattern").newline();
		}
		reply.literal("To download log file, type: ").code("bot get log name <file-name>");
		tracing.queue(action.getEventMessageChannel().sendMessage(reply.build()));
	}

	public Reply createFileReply(Path path) throws Exception {
		String fileName = path.getFileName().toString();
		ZonedDateTime dt = Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault());
		long fileSize = Files.size(path);

		return Reply.of().code(fileName).newline() //
				.literal("\tFile Size: ").code(DataSize.ofBytes(fileSize).toKilobytes()).literal("KB").newline() //
				.literal("\tLast Modified: ").code(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss.SSS ").format(dt)).newline();
	}

	public void sendLogFiles(BotAction action, Path logFile) throws Exception {
		log.info("sending log file: {}", logFile);

		String fileName = logFile.getFileName().toString();
		InputStream is = Files.newInputStream(logFile);

		action.getEventMessageChannel().sendFile(is, fileName).queue(tracing.trace((m) -> {
			log.info("success send file: {}", logFile);
			SafeUtil.suppress(() -> is.close());

		}), tracing.trace((ex) -> {
			log.error("failed send file: {} ", logFile);
			SafeUtil.suppress(() -> is.close());

		}));
	}

	public Path getLogBaseDir() {
		return Paths.get(LOG_PATH).toAbsolutePath();
	}

}
