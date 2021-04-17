package wp.discord.bot.db.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.util.SafeUtil;

@Repository
@Slf4j
public class ScheduleRepository {

	public static final AtomicInteger SEQ_ID = new AtomicInteger(0);
	public static final int MAX_SCHEDULED_PER_USER = 20;

	private Map<String, Map<BigInteger, ScheduledAction>> inMemoryRepository = new ConcurrentHashMap<>();

	public BigInteger nextSeqId() {
		return BigInteger.valueOf(SEQ_ID.incrementAndGet());
	}

	public ScheduledAction find(String userId, BigInteger scheduleId) throws Exception {
		return SafeUtil.get(() -> inMemoryRepository.get(userId).get(scheduleId));
	}

	public ScheduledAction find(User user, BigInteger scheduleId) throws Exception {
		return find(user.getId(), scheduleId);
	}

	public List<ScheduledAction> findAll(String userId) throws Exception {
		Map<BigInteger, ScheduledAction> userRepo = inMemoryRepository.get(userId);
		if (userRepo == null) {
			userRepo = new ConcurrentHashMap<>();
			inMemoryRepository.put(userId, userRepo);
		}
		return userRepo.values().stream().sorted().collect(Collectors.toList());
	}

	public void delete(ScheduledAction schedule) {
		delete(schedule.getAuthorId(), schedule.getId());
	}

	public void delete(String userId, BigInteger scheduleId) {
		Map<BigInteger, ScheduledAction> userRepo = inMemoryRepository.get(userId);
		if (userRepo == null) {
			return;
		}		
		userRepo.remove(scheduleId);
	}

	public List<ScheduledAction> findAll(User user) throws Exception {
		return findAll(user.getId());
	}

	public List<ScheduledAction> findAll() throws Exception {
		return inMemoryRepository.values().stream() //
				.map((userActions) -> userActions.values().stream().collect(Collectors.toList())) //
				.flatMap((l) -> l.stream()) //
				.sorted() //
				.collect(Collectors.toList()); //
	}

	public boolean isFull(User user) throws Exception {
		return isFull(user.getId());
	}

	public boolean isFull(String userId) throws Exception {
		return findAll(userId).size() >= MAX_SCHEDULED_PER_USER;
	}

	public void save(ScheduledAction schedule) throws Exception {
		Map<BigInteger, ScheduledAction> userRepo = inMemoryRepository.get(schedule.getAuthorId());
		if (userRepo == null) {
			userRepo = new ConcurrentHashMap<>();
			inMemoryRepository.put(schedule.getAuthorId(), userRepo);
		}

		log.info("save Schedule: {}", schedule);
		userRepo.put(schedule.getId(), schedule);
	}

}
