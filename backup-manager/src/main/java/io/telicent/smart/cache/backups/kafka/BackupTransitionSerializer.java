package io.telicent.smart.cache.backups.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.telicent.smart.cache.sources.kafka.serializers.AbstractJacksonSerializer;

/**
 * A Kafka serializer for {@link BackupTransition} instances
 */
public class BackupTransitionSerializer extends AbstractJacksonSerializer<BackupTransition> {

    /**
     * Creates a new transition serializer
     */
    public BackupTransitionSerializer() {
        super(new ObjectMapper().registerModule(new JavaTimeModule()));
    }
}
