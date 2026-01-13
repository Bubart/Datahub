package com.example.kafkaadapterservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "tcp")
public class TcpPartnerProperties {

    private static final String DEFAULT_PARTNER_KEY = "default";

    private Map<String, PartnerConfig> partners = new HashMap<>();
    private ConnectionConfig connection = new ConnectionConfig();

    public Map<String, PartnerConfig> getPartners() {
        return partners;
    }

    public void setPartners(Map<String, PartnerConfig> partners) {
        this.partners = partners;
    }

    public ConnectionConfig getConnection() {
        return connection;
    }

    public void setConnection(ConnectionConfig connection) {
        this.connection = connection;
    }

    public PartnerConfig getDefaultPartner() {
        return partners.get(DEFAULT_PARTNER_KEY);
    }

    public boolean hasDefaultPartner() {
        return partners.containsKey(DEFAULT_PARTNER_KEY);
    }

    public String getDefaultPartnerKey() {
        return DEFAULT_PARTNER_KEY;
    }

    public Map<String, PartnerConfig> getNonDefaultPartners() {
        Map<String, PartnerConfig> result = new HashMap<>(partners);
        result.remove(DEFAULT_PARTNER_KEY);
        return result;
    }

    public static class PartnerConfig {
        private String host;
        private int port;
        private String topic;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        @Override
        public String toString() {
            return "PartnerConfig{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", topic='" + topic + '\'' +
                    '}';
        }
    }

    public static class ConnectionConfig {
        private int timeout = 5000;
        private RetryConfig retryConfig = new RetryConfig();

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public RetryConfig getRetryConfig() {
            return retryConfig;
        }

        public void setRetryConfig(RetryConfig retryConfig) {
            this.retryConfig = retryConfig;
        }
    }

    public static class RetryConfig {
        private int maxAttempts = 3;
        private long delay = 2000;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getDelay() {
            return delay;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }
    }

}
