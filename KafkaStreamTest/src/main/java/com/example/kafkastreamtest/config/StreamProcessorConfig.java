package com.example.kafkastreamtest.config;

import com.example.kafkastreamtest.entity.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableKafkaStreams
public class StreamProcessorConfig {
    private static final Logger log = LoggerFactory.getLogger(StreamProcessorConfig.class);

    @Value(value="${kafka.topic.outgoing1}")
    private String outgoingTopic1;

    @Value(value="${kafka.topic.outgoing2}")
    private String outgoingTopic2;

    @Value(value="${kafka.topic.incomingTopics}")
    private List<String> incomingTopics;

    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder, ObjectMapper objectMapper) {
        XmlMapper xmlMapper = new XmlMapper();
        log.info("Incoming Topics: {}", incomingTopics);
        KStream<String, String> stream = builder.stream(incomingTopics);

//        stream
//            .peek((key, value) -> log.info("Incoming message: {}", value))
//            .mapValues(value -> "processed: " + value)
//            .peek((key, value) -> log.info("Outgoing message: {}", value))
//            .to("test-output-topic");

        stream
                .peek((key, value) -> log.info("Incoming message: {}", value)).mapValues(value -> {
                    try {
                        if (value.startsWith("<")) {
                            return objectMapper.writeValueAsString(xmlMapper.readValue(value, Message.class));
                        } else {
                            return value;
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .peek((key, value) -> log.info("Transformed: {}", value))
                .split()
                .branch((key, value) -> "A".equals(objectMapper.readValue(value, Message.class).getFormat()),
                        Branched.withConsumer((ks) -> ks.to(outgoingTopic1))
                )
                .branch((key, value) -> "B".equals(objectMapper.readValue(value, Message.class).getFormat()),
                        Branched.withConsumer((ks) -> ks.to(outgoingTopic2))
                )
                .defaultBranch(Branched.withConsumer(ks -> ks.to(outgoingTopic2)));

        return stream;
    }
}
