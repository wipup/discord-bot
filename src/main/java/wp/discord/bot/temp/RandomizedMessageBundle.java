//package wp.discord.bot.temp;
//
//import java.text.MessageFormat;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//import org.apache.commons.collections4.IterableUtils;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class RandomizedMessageBundle implements MessageBundle {
//
//	private final String languageCode;
//	private Random random;
//	private Map<String, Object> message;
//
//	public RandomizedMessageBundle(String languageCode, Map<String, Object> message) {
//		this.languageCode = languageCode.toLowerCase();
//		this.random = new Random();
//		this.message = message;
//	}
//
//	@Override
//	public String getMessage(String key, Object... objs) {
//		Object template = message.get(key);
//		if (template == null) {
//			return null;
//		}
//
//		String result = formatMessage(template, objs);
//		log.trace("getMessage({}, {}) = {} -> {}", key, objs, template, result);
//
//		return result;
//	}
//
//	protected String formatMessage(Object pattern, Object... objects) {
//		if (pattern == null) {
//			pattern = "";
//		}
//		if (pattern instanceof Collection<?>) {
//			Collection<?> templateList = (Collection<?>) pattern;
//			Object randomElement = getRandomElement(templateList);
//			return formatMessage(randomElement, objects);
//
//		}
//		return MessageFormat.format(pattern.toString(), objects);
//	}
//
//	protected Object getRandomElement(Collection<?> collection) {
//		int index = random.nextInt(collection.size());
//
//		if (collection instanceof List<?>) {
//			List<?> list = (List<?>) collection;
//			return list.get(index);
//		}
//		return IterableUtils.get(collection, index);
//	}
//
//	@Override
//	public boolean matchesKey(String key, String msg) {
//		Object template = message.get(key);
//
//		log.trace("matchesKey({}, {}) = {}", key, msg, template);
//		if (template == null) {
//			return false;
//
//		} else if (template instanceof Collection<?>) {
//			Collection<?> collection = (Collection<?>) template;
//			return collection.stream().anyMatch((t) -> msg.matches(t.toString()));
//		}
//
//		return msg.matches(template.toString());
//	}
//
//	@Override
//	public String getLanguageCode() {
//		return languageCode;
//	}
//
//	public void setRandom(Random random) {
//		this.random = random;
//	}
//}
