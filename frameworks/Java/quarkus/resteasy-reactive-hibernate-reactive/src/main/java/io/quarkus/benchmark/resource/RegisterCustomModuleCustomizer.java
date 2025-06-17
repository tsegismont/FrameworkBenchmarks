package io.quarkus.benchmark.resource;

import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.RecyclerPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.netty.util.concurrent.FastThreadLocal;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        mapper.registerModule(new CustomModule());
        mapper.getFactory().setRecyclerPool(new FastThreadLocalPool());
    }

    private static class CustomModule extends SimpleModule {
        public CustomModule() {
            addSerializer(Message.class, new MessageSerializer());
        }
    }

    private static class FastThreadLocalPool extends RecyclerPool.ThreadLocalPoolBase<BufferRecycler> {

        static final FastThreadLocal<BufferRecycler> fastThreadLocal = new FastThreadLocal<>() {
            @Override
            protected BufferRecycler initialValue() {
                return new BufferRecycler();
            }
        };

        @Override
        public BufferRecycler acquirePooled() {
            return fastThreadLocal.get();
        }
    }
}