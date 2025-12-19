package com.example.kafkaadapterservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer;
import org.springframework.messaging.MessageChannel;

@Configuration
public class TcpConfig {

    @Bean
    public TcpNetClientConnectionFactory clientConnectionFactory() {
        TcpNetClientConnectionFactory factory =
            new TcpNetClientConnectionFactory("localhost", 5001);

        factory.setSingleUse(false);
        factory.setSerializer(new ByteArrayRawSerializer());
        factory.setDeserializer(new ByteArrayRawSerializer());

        return factory;
    }

    @Bean(name = "tcpOut")
    public MessageChannel tcpOut() {
        return new DirectChannel();
    }

//    @Bean
//    @ServiceActivator(inputChannel = "tcpOut")
//    public TcpOutboundGateway tcpOutboundGateway(TcpNetClientConnectionFactory connectionFactory) {
//        TcpOutboundGateway gateway = new TcpOutboundGateway();
//        gateway.setConnectionFactory(connectionFactory);
//        gateway.setRequiresReply(false); // send-only mode
//        return gateway;
//    }

    @Bean
    @ServiceActivator(inputChannel = "tcpOut")
    public TcpSendingMessageHandler tcpSender(TcpNetClientConnectionFactory connectionFactory) {

        TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
        handler.setConnectionFactory(connectionFactory);

        // optional:
        handler.setLoggingEnabled(true);

        return handler;
    }
}
