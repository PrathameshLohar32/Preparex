package com.preparex.preparex_backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration for the contest engine.
 */
@Configuration
public class ContestKafkaConfig {

    public static final String TOPIC_CONTEST_SUBMISSIONS = "contest-submissions";
    public static final String TOPIC_CONTEST_ENDED = "contest-ended";
    public static final String TOPIC_CONTEST_REMINDERS = "contest-reminders";
    public static final String TOPIC_BADGE_EVENTS = "badge-events";

    @Bean
    public NewTopic contestSubmissionsTopic() {
        return TopicBuilder.name(TOPIC_CONTEST_SUBMISSIONS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic contestEndedTopic() {
        return TopicBuilder.name(TOPIC_CONTEST_ENDED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic contestRemindersTopic() {
        return TopicBuilder.name(TOPIC_CONTEST_REMINDERS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic badgeEventsTopic() {
        return TopicBuilder.name(TOPIC_BADGE_EVENTS).partitions(1).replicas(1).build();
    }
}
