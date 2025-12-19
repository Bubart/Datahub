package com.example.kafkaingesterservice.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "PartnerMessage")
public class Message {

    @JacksonXmlProperty(localName = "PartnerId")
    private String partnerId;

    @JacksonXmlProperty(localName = "TransactionId")
    private String transactionId;

    @JacksonXmlProperty(localName = "Amount")
    private double amount;

    @JacksonXmlProperty(localName = "Timestamp")
    private String timestamp;

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
