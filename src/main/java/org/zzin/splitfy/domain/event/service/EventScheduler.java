package org.zzin.splitfy.domain.event.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@NullMarked
public class EventScheduler {

  private final EventService eventService;

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void scheduleEventProcessing() {
    try {
      eventService.processScheduledEventQueue(LocalDateTime.now());
    } catch (Exception e) {
      log.error("EventScheduler Unexpected error occurred-{}", e.getMessage());
    }
  }
}
