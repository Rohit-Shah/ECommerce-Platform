package com.platform.order.internal.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.OutboxStatus;
import com.platform.common.events.OrderCreatedEvent;
import com.platform.order.internal.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper mapper;

    @Scheduled(fixedDelayString = "${outbox.poll.interval:1000}")
    public void pollAndPublish(){
        List<OutboxEvent> top20PendingEvents = outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if(top20PendingEvents.isEmpty())  return;
        log.debug("Found {} pending orders to process",top20PendingEvents.size());
        for(OutboxEvent event : top20PendingEvents){
            processEvent(event);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void processEvent(OutboxEvent event){
        try{
            Object domainEvent = deserializeEvent(event.getEventType(),event.getPayload());
            eventPublisher.publishEvent(domainEvent);
            event.markAsPublished();
            outboxEventRepository.save(event);
            log.info("Successfully published outbox event [id={}, type={}]",event.getId(),event.getEventType());
        }catch (JsonProcessingException e){
            log.error("Failed to process outbox event [id={}, type={}]",event.getId(),event.getEventType());
            event.markAsFailed();
            outboxEventRepository.save(event);
        }catch (Exception e){
            log.error("Error occurred while processing event", e);
        }
    }

    private Object deserializeEvent(String eventType,String eventPayload) throws JsonProcessingException {
        if(OrderCreatedEvent.class.getSimpleName().equals(eventType)){
            return mapper.readValue(eventPayload,OrderCreatedEvent.class);
        }
        throw new IllegalArgumentException("Unknown event type");
    }

}
