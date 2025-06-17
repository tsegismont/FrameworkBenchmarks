package io.quarkus.benchmark.resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.quarkus.resteasy.reactive.jackson.runtime.mappers.JacksonMapperUtil.SerializationInclude;

import java.io.IOException;

public class MessageSerializer extends StdSerializer<Message> implements ResolvableSerializer {

    private static final SerializedString FIELD_NAME = new SerializedString("message");

    private SerializationInclude serializationInclude;

    public MessageSerializer() {
        super(Message.class);
    }

    @Override
    public void resolve(SerializerProvider provider) {
        serializationInclude = SerializationInclude.decode(Message.class, provider);
    }

    @Override
    public void serialize(Message value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        String message = value.getMessage();
        if (message != null) {
            gen.writeFieldName(FIELD_NAME);
            gen.writeString(message);
        } else if (serializationInclude.shouldSerialize(value)) {
            gen.writeFieldName(FIELD_NAME);
            gen.writeNull();
        }
        gen.writeEndObject();
    }
}
