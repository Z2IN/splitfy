package org.zzin.splitfy.domain.event.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.domain.event.entity.WaitingQueue;
import org.zzin.splitfy.domain.event.repository.EventQueryRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

  private final EventQueryRepository eventQueryRepository;

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void tick() {
    LocalDateTime now = LocalDateTime.now();

    // 1. 만료된 대기열 정리
    eventQueryRepository.deleteExpiredQueues(now);

    // 2. 진행중인 이벤트 확인
    Long eventId = eventQueryRepository.findOpenedEventId(now);
    if (eventId == null) {
      return;
    }

    // 3. 이벤트에 이미 턴 보유자 있으면 종료
    if (eventQueryRepository.existsActiveTurn(eventId, now)) {
      return;
    }

    // 3. 턴이 비어 있으면 다음 1명 활성화
    WaitingQueue next = eventQueryRepository.findNextActivatableQueue(eventId);
    if (next != null) {
      next.startTurn(now);
    }
  }
}
