package wp.discord.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@Configuration
public class ObjectMapperConfig {

	public static final String YML_MAPPER = "ymlMapper";
	public static final String JSON_MAPPER = "jsonMapper";

	@Bean(YML_MAPPER)
	@Primary
	public ObjectMapper ymlMapper() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.findAndRegisterModules();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		return mapper;
	}

	@Bean(JSON_MAPPER)
	public ObjectMapper jsonMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper;
	}
}
