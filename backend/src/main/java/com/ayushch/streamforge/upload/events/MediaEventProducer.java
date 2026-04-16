package com.ayushch.streamforge.upload.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MediaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MediaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMediaUploaderEvent(MediaUploaderEvent event) {
        kafkaTemplate.send("media-uploads", event);
        log.info("[kafka-topic]: message sent to topic. UploadId: ", event.fileId());
    }
}
