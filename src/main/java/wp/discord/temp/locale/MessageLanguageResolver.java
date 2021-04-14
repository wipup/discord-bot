package wp.discord.temp.locale;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import wp.discord.temp.core.ThreadContextAware;

@Slf4j
public class MessageLanguageResolver implements MessageBundle, ThreadContextAware {

	private static final String DEFAULT_LANG = "en";

	private Map<String, MessageBundle> messageBundles;

	public MessageLanguageResolver(Map<String, MessageBundle> messageBundles) {
		this.messageBundles = messageBundles;
	}

	public MessageBundle getDefaultMessageBundle() {
		return messageBundles.get(DEFAULT_LANG);
	}

	public MessageBundle getMessageBundle(String langCode) {
		String code = StringUtils.lowerCase(langCode);
		MessageBundle bundle = messageBundles.get(code);

		if (bundle == null) {
			bundle = getDefaultMessageBundle();
		}
		return bundle;
	}

	@Override
	public String getMessage(String key, Object... objs) {
		String currentLang = getCurrentContext().getLanguage();
		return getLocalizedMessage(currentLang, key, objs);
	}

	public String getLocalizedMessage(String lang, String key, Object... objs) {
		String msg = getMessageBundle(lang).getMessage(key, objs);
		if (msg == null) {
			msg = getDefaultMessageBundle().getMessage(key, objs);
		}
		if (msg == null) {
			log.debug("msg not found for lang={}, key={}, param={}", lang, key, objs);
			return key;
		}
		return msg;
	}

	@Override
	public boolean matchesKey(String key, String messagePattern) {
		for (MessageBundle bundle : messageBundles.values()) {
			if (bundle.matchesKey(key, messagePattern)) {
				getCurrentContext().setLanguage(bundle.getLanguageCode());
				return true;
			}
		}

		return false;
	}

	@Override
	public String getLanguageCode() {
		return DEFAULT_LANG;
	}

}
