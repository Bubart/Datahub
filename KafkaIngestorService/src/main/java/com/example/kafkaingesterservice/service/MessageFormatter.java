package com.example.kafkaingesterservice.service;

import com.example.kafkaingesterservice.entity.Message;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageFormatter {

    private final XmlMapper xmlMapper;

    public MessageFormatter(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    public String transform(String xml) throws Exception {
        Message msg = xmlMapper.readValue(xml, Message.class);

        return xmlMapper.writeValueAsString(msg);
    }

}
