package com.preparex.preparex_backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration for profile stats consumers.
 * Ensures submission-saved and sprint-ended topics exist for consumers.
 */
@Configuration
public class ProfileKafkaConfig {

    public static final String TOPIC_SUBMISSION_SAVED = "submission-saved";
    public static final String TOPIC_SPRINT_ENDED = "sprint-ended";

    @Bean
    public NewTopic submissionSavedTopic() {
        return TopicBuilder.name(TOPIC_SUBMISSION_SAVED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic sprintEndedTopic() {
        return TopicBuilder.name(TOPIC_SPRINT_ENDED).partitions(1).replicas(1).build();
    }
}
