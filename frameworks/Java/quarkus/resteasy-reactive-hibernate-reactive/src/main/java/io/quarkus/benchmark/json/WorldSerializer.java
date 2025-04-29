package io.quarkus.benchmark.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.quarkus.benchmark.model.World;

import java.io.IOException;

public class WorldSerializer extends StdSerializer<World> {

    public WorldSerializer(Class<World> t) {
        super(t);
    }

    @Override
    public void serialize(World value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", value.getId());
        gen.writeNumberField("randomNumber", value.getRandomNumber());
        gen.writeEndObject();
    }
}