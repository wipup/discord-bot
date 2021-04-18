package wp.discord.bot.util;

import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaitUtil {

	// ---------- blocking
	public static void doWait(String reason, Supplier<Boolean> condition) {
		log.debug("do waiting until: {}", reason);

		final long sleepTime = 50; // ms
		long accumulatedWaitTime = 0;

		boolean sucess = condition.get();
		while (!sucess) {
			try {
				Thread.sleep(sleepTime);
				accumulatedWaitTime += sleepTime;

				if (accumulatedWaitTime >= 15000) {
					log.error("wait time timeout");
					break;
				}
			} catch (Exception e) {
				log.warn("wait sleep interupted", e);
				break;
			}
			sucess = condition.get();
		}

		log.trace("end waiting: {}", reason);
	}

}
