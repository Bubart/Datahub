package com.example.kafkastreamtest.config;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class StreamProcessorConfig {
    private static final Logger log = LoggerFactory.getLogger(StreamProcessorConfig.class);

    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream("test-input-topic");

        stream
            .peek((key, value) -> log.info("Incoming message: {}", value))
            .mapValues(value -> "processed: " + value)
            .peek((key, value) -> log.info("Outgoing message: {}", value))
            .to("test-output-topic");

        return stream;
    }
}
