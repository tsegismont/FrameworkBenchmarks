package io.quarkus.benchmark.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        mapper.registerModule(new CustomModule());
    }

    private static class CustomModule extends SimpleModule {
        public CustomModule() {
            addSerializer(Message.class, new MessageSerializer());
        }
    }
}