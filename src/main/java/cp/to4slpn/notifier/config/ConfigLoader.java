package cp.to4slpn.notifier.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

public final class ConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .registerModule(new JavaTimeModule());

    public static Config loadConfig(InputStream configStream) throws IOException {
        return MAPPER.readValue(configStream, Config.class);
    }
}