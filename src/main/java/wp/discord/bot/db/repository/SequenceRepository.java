package wp.discord.bot.db.repository;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.core.persist.AbstractFileBasedRepository;
import wp.discord.bot.db.entity.ScheduledAction;
import wp.discord.bot.db.repository.SequenceRepository.SequenceEntity;
import wp.discord.bot.util.SafeUtil;

@Repository
@Slf4j
public class SequenceRepository extends AbstractFileBasedRepository<SequenceEntity> {

	private Map<String, AtomicInteger> sequenceMap = new HashMap<>();

	private AtomicInteger getSeq(String name) {
		AtomicInteger seq = sequenceMap.get(name);
		if (seq == null) {
			seq = new AtomicInteger(0);
			sequenceMap.put(name, seq);
		}
		return seq;
	}

	public BigInteger nextValSeq(String name) throws Exception {
		BigInteger result = null;
		try {
			result = BigInteger.valueOf(getSeq(name).incrementAndGet());
			return result;
		} finally {
			asyncPersist(new SequenceEntity(name, result));
		}
	}

	public BigInteger nextValSeqScheduleAction() throws Exception {
		return nextValSeq(ScheduledAction.class.getSimpleName());
	}

	@Override
	public Class<SequenceEntity> getEntityClass() {
		return SequenceEntity.class;
	}

	@Override
	public void doReload(SequenceEntity entity) throws Exception {
		log.info("Reload Sequence: {}", entity);
		SafeUtil.suppress(() -> {
			AtomicInteger seq = getSeq(entity.getName());
			seq.set(entity.value.intValue());

			log.info("Sequence: {}: value: {}", entity.getName(), seq.get());
		});
	}

	@Override
	protected String getFileName(SequenceEntity entity) {
		return "SEQ_" + entity.name;
	}

	@Data
	@AllArgsConstructor
	public static class SequenceEntity {
		private String name;
		private BigInteger value;
	}

}
