package com.example.kafkaadapterservice.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer;
import org.springframework.messaging.MessageChannel;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TcpConfig {
    private static final Logger log = LoggerFactory.getLogger(TcpConfig.class);

    private final TcpPartnerProperties properties;

    public TcpConfig(TcpPartnerProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void debugProperties() {
        log.info("=== TCP Partner Properties ===");
        properties.getPartners().forEach((name, config) -> {
            log.info("Partner: '{}' -> host={}, port={}, topic='{}'",
                    name, config.getHost(), config.getPort(), config.getTopic());
        });
        log.info("==============================");
    }

    @Bean("ConnectionProperties")
    public Map<String, TcpNetClientConnectionFactory> connectionFactories() {
        Map<String, TcpNetClientConnectionFactory> factories = new HashMap<>();

        properties.getPartners().forEach((name, config) -> {
           TcpNetClientConnectionFactory factory =
                   new TcpNetClientConnectionFactory(config.getHost(), config.getPort());

           factory.setSingleUse(false);
           factory.setConnectTimeout(properties.getConnection().getTimeout());
           factory.setSerializer(new ByteArrayRawSerializer());
           factory.setDeserializer(new ByteArrayRawSerializer());
           factory.setComponentName(name + "ConnectionFactory");
           factories.put(name, factory);

            factory.start();

           log.info("Created TCP connection factory for {} at {}:{}", name, config.getHost(), config.getPort());
        });

        return factories;
    }

    @Bean("tcpChannels")
    public Map<String, MessageChannel> tcpChannels() {
        Map<String, MessageChannel> channels = new HashMap<>();

        properties.getPartners().forEach((name, config) -> {
            DirectChannel channel = new DirectChannel();
            channel.setComponentName(name + "TcpChannel");
            channels.put(name, channel);
            log.info("Created TCP channel for {}", name);
        });

        return channels;
    }

    @Bean("tcpHandlers")
    public Map<String, TcpSendingMessageHandler> tcpHandlers() {

        Map<String, TcpNetClientConnectionFactory> connectionFactories = connectionFactories();
        Map<String, MessageChannel> tcpChannels = tcpChannels();

        Map<String, TcpSendingMessageHandler> handlers = new HashMap<>();

        properties.getPartners().forEach((name, config) -> {
            TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
            handler.setConnectionFactory(connectionFactories.get(name));
            handler.setLoggingEnabled(true);
            handler.setComponentName(name + "TcpHandler");

            handler.start();

            ((DirectChannel) tcpChannels.get(name)).subscribe(handler);

            handlers.put(name, handler);
            log.info("Created TCP handler for {}", name);
        });

        return handlers;
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down TCP connection factories...");
    }
}
