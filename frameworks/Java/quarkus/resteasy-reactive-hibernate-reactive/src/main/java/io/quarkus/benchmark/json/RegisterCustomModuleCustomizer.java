package io.quarkus.benchmark.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.benchmark.model.World;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(World.class, new WorldSerializer(World.class));
        mapper.registerModule(module);
    }
}