package org.zzin.splitfy.domain.event.service;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.dto.CommonCursor;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.event.dto.EventCursor;
import org.zzin.splitfy.domain.event.dto.EventSummaryDTO;
import org.zzin.splitfy.domain.event.dto.request.CreateEventRequest;
import org.zzin.splitfy.domain.event.dto.request.SelectEventNumberRequest;
import org.zzin.splitfy.domain.event.dto.response.CreateEventResponse;
import org.zzin.splitfy.domain.event.dto.response.EventNumberListResponse;
import org.zzin.splitfy.domain.event.dto.response.EventNumberResponse;
import org.zzin.splitfy.domain.event.dto.response.EventResponse;
import org.zzin.splitfy.domain.event.dto.response.EventRewardResponse;
import org.zzin.splitfy.domain.event.dto.response.GetEventsByResponse;
import org.zzin.splitfy.domain.event.dto.response.JoinQueueResponse;
import org.zzin.splitfy.domain.event.dto.response.QueuePositionResponse;
import org.zzin.splitfy.domain.event.entity.Event;
import org.zzin.splitfy.domain.event.entity.EventEntry;
import org.zzin.splitfy.domain.event.entity.EventNumber;
import org.zzin.splitfy.domain.event.entity.WaitingQueue;
import org.zzin.splitfy.domain.event.enums.EventStatus;
import org.zzin.splitfy.domain.event.exception.EventErrorCode;
import org.zzin.splitfy.domain.event.mapper.EventMapper;
import org.zzin.splitfy.domain.event.repository.EventEntryRepository;
import org.zzin.splitfy.domain.event.repository.EventNumberRepository;
import org.zzin.splitfy.domain.event.repository.EventQueryRepository;
import org.zzin.splitfy.domain.event.repository.EventRepository;
import org.zzin.splitfy.domain.event.repository.WaitingQueueRepository;
import org.zzin.splitfy.domain.point.service.PointInnerService;

@Service
@NullMarked
@RequiredArgsConstructor
public class EventService {

  private static final long TOTAL_REWARD = 200_000L; // 이벤트 고정 예산
  private static final int MIN_REWARD = 1_000;      // 최소 보장

  private static final int JACKPOT_PERCENT = 30;    // remaining의 30%
  private static final int UNIT = 1_000;            // 1,000원 단위

  private final EventRepository eventRepository;
  private final EventMapper eventMapper;
  private final EventEntryRepository eventEntryRepository;
  private final WaitingQueueRepository waitingQueueRepository;
  private final EventQueryRepository eventQueryRepository;
  private final PointInnerService pointInnerService;
  private final EventNumberRepository eventNumberRepository;

  @Transactional
  public CreateEventResponse createEvent(CreateEventRequest request) {

    int totalStock = request.totalStock(); // 재고 = 슬롯 수

    Event event = Event.builder()
        .title(request.title())
        .description(request.description())
        .totalStock(request.totalStock())
        .status(EventStatus.SCHEDULED)
        .startAt(request.startAt())
        .endAt(request.endAt())
        .build();

    Event saved = eventRepository.save(event);

    //번호판 생성
    int baseTotal = totalStock * MIN_REWARD;
    int remaining = (int) TOTAL_REWARD - baseTotal;

    // 전부 최소 보장으로 채우기
    List<Integer> rewards = new ArrayList<>(totalStock);
    for (int i = 0; i < totalStock; i++) {
      rewards.add(MIN_REWARD);
    }

    // remaining 없으면 그대로
    if (remaining > 0) {

      // 2) 대박 1개 (항상 0번)
      int jackpotDelta = remaining * JACKPOT_PERCENT / 100;
      jackpotDelta = jackpotDelta / UNIT * UNIT; // 1000원 단위 내림

      rewards.set(0, rewards.get(0) + jackpotDelta);
      remaining -= jackpotDelta;

      // 3) 남은 돈 분산 (1번부터)
      int idx = 1 % totalStock;
      while (remaining >= UNIT) {
        rewards.set(idx, rewards.get(idx) + UNIT);
        remaining -= UNIT;
        idx = (idx + 1) % totalStock;
      }

      Collections.shuffle(rewards);
    }

    List<EventNumber> numbers = new ArrayList<>(totalStock);
    for (int number = 1; number <= totalStock; number++) {
      numbers.add(EventNumber.builder()
          .eventId(saved.getId())
          .number(number)
          .reward(rewards.get(number - 1))
          .build());
    }
    eventNumberRepository.saveAll(numbers);
    return new CreateEventResponse(saved.getId());
  }

  @Transactional(readOnly = true)
  public EventResponse getEvent(Long eventId) {

    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new BusinessException(EventErrorCode.EVENT_NOT_FOUND));

    return eventMapper.toResponse(event);
  }

  @Transactional(readOnly = true)
  public EventNumberListResponse getEventNumbers(Long eventId, AuthUser authUser) {
    long userId = authUser.userId();

    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new BusinessException(EventErrorCode.EVENT_NOT_FOUND));

    //이벤트 기간 검증
    event.validateEventPeriod(LocalDateTime.now());

    // 1순위 검증
    WaitingQueue head = eventQueryRepository.findHead(eventId);
    if (head == null || head.getUserId() != userId) {
      throw new BusinessException(EventErrorCode.NOT_YOUR_TURN);
    }

    List<EventNumber> eventNumbers = eventNumberRepository.findByEventIdOrderByNumberAsc(eventId);

    List<EventNumberResponse> items = eventNumbers.stream()
        .map(eventMapper::toResponse)
        .toList();

    return new EventNumberListResponse(eventId, items);
  }

  /**
   * @param eventCursor null이면 첫 페이지를 조회한다.
   */
  @Transactional(readOnly = true)
  public CommonCursor<GetEventsByResponse> getEventsByCursor(@Nullable EventCursor eventCursor,
      int size) {
    List<EventSummaryDTO> events = eventQueryRepository.getEventsByCursor(eventCursor, size);

    boolean hasNext = events.size() > size;
    String nextCursor = null;

    if (hasNext) {
      events = events.subList(0, size);
      EventSummaryDTO lastEvent = events.get(size - 1);
      nextCursor = EventCursor.of(lastEvent.getStatus().priority(), lastEvent.getEventId())
          .encode();
    }

    List<GetEventsByResponse> response = events.stream().map(GetEventsByResponse::fromDto).toList();

    return CommonCursor.of(response, nextCursor, hasNext);
  }

  @Transactional
  public JoinQueueResponse joinQueue(Long eventId, AuthUser authUser) {
    long userId = authUser.userId();
    LocalDateTime now = LocalDateTime.now();

    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new BusinessException(EventErrorCode.EVENT_NOT_FOUND));

    //이벤트 진행중인지 확인
    event.validateEventPeriod(now);

    // 중복 참여 확인 (이미 번호 선택 성공한 사람)
    if (eventEntryRepository.existsByEventIdAndUserId(eventId, userId)) {
      throw new BusinessException(EventErrorCode.ALREADY_PARTICIPATED);
    }

    // 대기열 중복 확인 (이미 대기열인 사람)
    if (waitingQueueRepository.existsByEventIdAndUserId(eventId, userId)) {
      throw new BusinessException(EventErrorCode.ALREADY_IN_QUEUE);
    }

    WaitingQueue queue = WaitingQueue.builder().eventId(eventId).userId(userId).build();

    WaitingQueue saved = waitingQueueRepository.save(queue);

    // 내 앞순번 계산
    long position = eventQueryRepository.countAhead(saved.getEventId(), saved.getJoinAt(),
        saved.getId());

    return new JoinQueueResponse(eventId, position, saved.getJoinAt());
  }

  @Transactional(readOnly = true)
  public QueuePositionResponse getQueuePosition(long eventId, AuthUser authUser) {
    long userId = authUser.userId();

    WaitingQueue queue = waitingQueueRepository.findByEventIdAndUserId(eventId, userId)
        .orElseThrow(() -> new BusinessException(EventErrorCode.NOT_IN_QUEUE));

    long position = eventQueryRepository.countAhead(queue.getEventId(), queue.getJoinAt(),
        queue.getId());

    return new QueuePositionResponse(position);
  }

  @Transactional
  public EventRewardResponse selectEventNumber(@Valid SelectEventNumberRequest request,
      Long eventId, AuthUser authUser) {

    long userId = authUser.userId();
    int number = request.number();
    LocalDateTime now = LocalDateTime.now();

    //이벤트 기간 조회
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new BusinessException(EventErrorCode.EVENT_NOT_FOUND));
    event.validateEventPeriod(now);

    // 1순위 찾기
    WaitingQueue head = eventQueryRepository.findHeadForUpdate(eventId);
    if (head == null || head.getUserId() != userId) {
      throw new BusinessException(EventErrorCode.NOT_YOUR_TURN);
    }

    //이벤트 번호 뽑기 + 상태 변경
    EventNumber slot = eventQueryRepository.findEventNumberForUpdate(eventId, number);
    if (slot == null) {
      throw new BusinessException(EventErrorCode.NUMBER_NOT_FOUND);
    }
    if (slot.isSelected()) {
      throw new BusinessException(EventErrorCode.NUMBER_ALREADY_TAKEN);
    }
    slot.select();

    //이벤트 참여 내역 저장
    eventEntryRepository.save(
        EventEntry.builder().eventId(eventId).userId(userId).reward(slot.getReward()).build());

    //리워드 포인트 지급
    pointInnerService.sendEventRewardPoint(userId, slot.getReward());

    //큐에서 이벤트 참여자 제거
    waitingQueueRepository.delete(head);

    //다음 이벤트 참가자 확인 후 활성화
    WaitingQueue nextHead = eventQueryRepository.findNextHeadForUpdate(eventId, head.getJoinAt(),
        head.getId());
    if (nextHead != null) {
      nextHead.startTurn(now);
      waitingQueueRepository.save(nextHead);
    }

    return new EventRewardResponse(slot.getNumber(), slot.getReward());
  }

  @Transactional
  public void processScheduledEventQueue(LocalDateTime now) {

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
