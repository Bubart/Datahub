package com.example.kafkaingesterservice.ingester.interfaces.api;

import com.example.kafkaingesterservice.service.MessageFormatter;
import com.example.kafkaingesterservice.service.ProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class IngestController {
    private static final Logger log = LoggerFactory.getLogger(IngestController.class);

    private final MessageFormatter messageFormatter;
    private final ProducerService producerService;

    @Value("${ingester.kafka.topic}")
    private String topic;

    public IngestController(MessageFormatter messageFormatter, ProducerService producerService) {
        this.messageFormatter = messageFormatter;
        this.producerService = producerService;
    }

    @GetMapping
    public void testGet() {
        log.info("This is from the GET call!");
    }

    @PostMapping(
        value = "",
        consumes = "application/xml"
    )
    public ResponseEntity<String> ingest(@RequestBody String xml) throws Exception {
        String transformed = messageFormatter.transform(xml);
        producerService.send(topic, transformed);

        return ResponseEntity.ok("Message formatted + ingested");
    }
}
