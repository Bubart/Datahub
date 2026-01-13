package com.example.kafkaadapterservice.service;

import com.example.kafkaadapterservice.config.TcpPartnerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Service
public class TcpHealthService {

    private static final Logger log = LoggerFactory.getLogger(TcpHealthService.class);
    private final TcpPartnerProperties properties;

    public TcpHealthService(TcpPartnerProperties properties) {
        this.properties = properties;
    }

    public boolean isAvailable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port),
                    properties.getConnection().getTimeout());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean waitForAvailability(String host, int port) {
        int maxAttempts = properties.getConnection().getRetryConfig().getMaxAttempts();
        long delay = properties.getConnection().getRetryConfig().getDelay();

        for (int i = 1; i <= maxAttempts; i++) {
            if (isAvailable(host, port)) {
                return true;
            }

            log.warn("Retry {}/{} for {}:{}", i, maxAttempts, host, port);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }
}
