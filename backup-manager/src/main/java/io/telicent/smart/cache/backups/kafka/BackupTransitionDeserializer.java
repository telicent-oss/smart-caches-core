package io.telicent.smart.cache.backups.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.telicent.smart.cache.sources.kafka.serializers.AbstractJacksonDeserializer;

/**
 * A Kafka deserializer for {@link BackupTransition} instances
 */
public class BackupTransitionDeserializer extends AbstractJacksonDeserializer<BackupTransition> {
    /**
     * Creates a new deserializer
     */
    public BackupTransitionDeserializer() {
        super(new ObjectMapper().registerModule(new JavaTimeModule()), BackupTransition.class);
    }
}
