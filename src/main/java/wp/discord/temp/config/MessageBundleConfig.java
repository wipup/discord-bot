package wp.discord.temp.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import wp.discord.bot.config.ObjectMapperConfig;
import wp.discord.temp.locale.MessageBundle;
import wp.discord.temp.locale.MessageLanguageResolver;
import wp.discord.temp.locale.RandomizedMessageBundle;

@Configuration
@Slf4j
public class MessageBundleConfig {

	@Autowired
	@Qualifier(ObjectMapperConfig.YML_MAPPER)
	private ObjectMapper ymlMapper;

	@Autowired
	private ResourceLoader resourceLoader;

	@Bean
	public MessageLanguageResolver messageLanguageResolver() throws Exception {
		Map<String, MessageBundle> localBundles = loadAllLocalBundles();
		MessageLanguageResolver resolver = new MessageLanguageResolver(localBundles);
		return resolver;
	}

	private Map<String, MessageBundle> loadAllLocalBundles() throws Exception {
		Map<String, MessageBundle> languagePacks = new HashMap<>();

		Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("i18n/*.yml");
		for (Resource resource : resources) {
			log.info("Loading bundle: {}", resource.getFile());
			MessageBundle bundle = newMessageBundle(resource);
			languagePacks.put(bundle.getLanguageCode(), bundle);
		}

		return languagePacks;
	}

	private MessageBundle newMessageBundle(Resource resource) throws Exception {
		String langCode = getLanguageCode(resource.getFilename());
		Map<String, Object> msgBundle = readMessageBundle(resource);
		log.debug("[{}] Message Bundle: {}", langCode, msgBundle);

		return new RandomizedMessageBundle(langCode, msgBundle);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> readMessageBundle(Resource resource) throws Exception {
		try (InputStream in = resource.getInputStream()) {
			return flattenBundleKey(ymlMapper.readValue(in, Map.class));
		}
	}

	private Map<String, Object> flattenBundleKey(Map<String, Object> bundle) {
		Map<String, Object> newBundle = new HashMap<>();
		flattenDepthFirst(newBundle, bundle, "");

		log.debug("bundle: {}", newBundle);
		return newBundle;
	}

	@SuppressWarnings("unchecked")
	private void flattenDepthFirst(Map<String, Object> masterBundle, Map<String, Object> map, String parentKey) {
		if (MapUtils.isEmpty(map)) {
			return;
		}

		for (String key : map.keySet()) {
			String newParentKey = key;
			if (StringUtils.isNotEmpty(parentKey)) {
				newParentKey = parentKey + "." + key;
			}

			Object value = map.get(key);
			if (value instanceof Map<?, ?>) {
				Map<String, Object> m = (Map<String, Object>) value;
				flattenDepthFirst(masterBundle, m, newParentKey);

			} else {
				masterBundle.put(newParentKey, value);
			}
		}
	}

	private String getLanguageCode(String fileName) {
		int dotIndex = fileName.indexOf(".");
		if (dotIndex < 0) {
			return fileName.toLowerCase();
		}

		return fileName.substring(0, dotIndex).toLowerCase();
	}
}
