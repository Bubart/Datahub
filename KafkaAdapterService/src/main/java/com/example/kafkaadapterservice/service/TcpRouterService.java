package com.example.kafkaadapterservice.service;

import com.example.kafkaadapterservice.config.TcpPartnerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TcpRouterService {

    private static final Logger log = LoggerFactory.getLogger(TcpRouterService.class);

    private final TcpPartnerProperties properties;
    private final Map<String, MessageChannel> tcpChannels;
    private final TcpHealthService healthService;

    public TcpRouterService(
            TcpPartnerProperties properties,
            @Qualifier("tcpChannels") Map<String, MessageChannel> tcpChannels,
            TcpHealthService healthService
    ) {
        this.properties = properties;
        this.tcpChannels = tcpChannels;
        this.healthService = healthService;
    }

    public String getPartnerByTopic(String topic) {
        // First, try to find exact match
        String partner = properties.getPartners().entrySet().stream()
                .filter(e -> e.getValue().getTopic().equals(topic))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        // If not found and default exists, use default
        if (partner == null && properties.hasDefaultPartner()) {
            log.info("No specific partner for topic '{}', using default", topic);
            return properties.getDefaultPartnerKey();
        }

        return partner;
    }

    public MessageChannel getChannel(String partnerName) {
        return tcpChannels.get(partnerName);
    }

    public MessageChannel getDefaultChannel() {
        if (properties.hasDefaultPartner()) {
            return tcpChannels.get(properties.getDefaultPartnerKey());
        }
        return null;
    }

    public TcpPartnerProperties.PartnerConfig getPartnerConfig(String partnerName) {
        return properties.getPartners().get(partnerName);
    }

    public boolean send(String partnerName, byte[] payload) {
        if (trySend(partnerName, payload)) {
            return true;
        }

        if (!partnerName.equals(properties.getDefaultPartnerKey()) && properties.hasDefaultPartner()) {
            log.warn("Primary partner {} failed, trying default partner", partnerName);
            return trySend(properties.getDefaultPartnerKey(), payload);
        }

        return false;
    }

    public boolean sendToDefault(byte[] payload) {
        if (!properties.hasDefaultPartner()) {
            log.error("No default partner configured");
            return false;
        }
        return trySend(properties.getDefaultPartnerKey(), payload);
    }

    public boolean trySend(String partnerName, byte[] payload) {
        TcpPartnerProperties.PartnerConfig config = getPartnerConfig(partnerName);

        if (config == null) {
            log.error("Unknown partner: {}", partnerName);
            return false;
        }

        if (!healthService.waitForAvailability(config.getHost(), config.getPort())) {
            log.error("Partner {} not available at the moment at {}:{}",
                    partnerName, config.getHost(), config.getHost());
        }

        MessageChannel channel = getChannel(partnerName);
        if (channel == null) {
            log.error("No channel found for partner: {}", partnerName);
        }

        try {
            channel.send(MessageBuilder.withPayload(payload).build());
            log.info("Message sent to: {}", partnerName);
            return true;
        } catch (Exception e) {
            log.error("Failed to send to {}:{}", partnerName, e.getMessage());
            return false;
        }
    }
}
