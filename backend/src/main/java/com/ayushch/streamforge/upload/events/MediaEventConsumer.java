package com.ayushch.streamforge.upload.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MediaEventConsumer {
    
    @KafkaListener(topics = "media-uploads", groupId = "streamforge-group")
    public void consume(MediaUploaderEvent event) {
        log.info("[kafka-topic]: worker received file]");
        //TODO: write the consume logic, make thumbnail, save it back to object store
        //with relation to the file, and this relation is taken care by the db
    }
}
