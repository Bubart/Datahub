package com.example.kafkaadapterservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class KafkaAdapterListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaAdapterListener.class);
    private final MessageChannel tcpOut;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();


    public KafkaAdapterListener(@Qualifier("tcpOut") MessageChannel tcpOut) {
        this.tcpOut = tcpOut;
    }

    @KafkaListener(
        topics = {"Partner1Outgoing", "Partner2Outgoing"},
        groupId = "adapter-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) throws JsonProcessingException {
        String message = record.value();
        log.info("Value received from topic: {} ", message);

        JsonNode jsonNode = objectMapper.readTree(message);
        String xml = xmlMapper.writeValueAsString(jsonNode).replace("ObjectNode", "PartnerMessage");

        try {
            byte[] payload = xml.getBytes(StandardCharsets.UTF_8);

            tcpOut.send(MessageBuilder.withPayload(payload).build());
            ack.acknowledge();

            log.info("TCP sent OK");
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

    }
}
