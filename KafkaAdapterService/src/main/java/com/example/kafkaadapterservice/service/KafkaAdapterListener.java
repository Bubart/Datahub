package com.example.kafkaadapterservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Service
public class KafkaAdapterListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaAdapterListener.class);

    private final TcpRouterService routerService;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    public KafkaAdapterListener(TcpRouterService routerService, ObjectMapper objectMapper) {
        this.routerService = routerService;
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();
    }

    @KafkaListener(
            topics = {"Partner1Outgoing", "Partner2Outgoing", "PartnerAllOutgoing"},
            groupId = "adapter-service"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String topic = record.topic();
        String json = record.value();

        log.info("Received from topic {}: {}", topic, json);

        String partnerName = routerService.getPartnerByTopic(topic);
        if (partnerName == null) {
            log.error("No partner configured for topic: {}", topic);
            ack.acknowledge();
            return;
        }

        try {
            JsonNode node = objectMapper.readTree(json);
            String xml = xmlMapper.writeValueAsString(node)
                    .replace("ObjectNode", "PartnerMessage");
            byte[] payload = xml.getBytes(StandardCharsets.UTF_8);

            if (routerService.send(partnerName, payload)) {
                ack.acknowledge();
            } else {
                log.warn("Message not acknowledged - will be redelivered");
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to process message: {}", e.getMessage());
            ack.acknowledge();
        }
    }
}
