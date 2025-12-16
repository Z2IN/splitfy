package org.zzin.splitfy.domain.event.service;

import java.time.LocalDateTime;
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
import org.zzin.splitfy.domain.event.dto.response.CreateEventResponse;
import org.zzin.splitfy.domain.event.dto.response.EventResponse;
import org.zzin.splitfy.domain.event.dto.response.GetEventsByResponse;
import org.zzin.splitfy.domain.event.dto.response.JoinQueueResponse;
import org.zzin.splitfy.domain.event.dto.response.QueuePositionResponse;
import org.zzin.splitfy.domain.event.entity.Event;
import org.zzin.splitfy.domain.event.entity.WaitingQueue;
import org.zzin.splitfy.domain.event.exception.EventErrorCode;
import org.zzin.splitfy.domain.event.mapper.EventMapper;
import org.zzin.splitfy.domain.event.repository.EventEntryRepository;
import org.zzin.splitfy.domain.event.repository.EventQueryRepository;
import org.zzin.splitfy.domain.event.repository.EventRepository;
import org.zzin.splitfy.domain.event.repository.WaitingQueueRepository;

@Service
@NullMarked
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final EventMapper eventMapper;
  private final EventEntryRepository eventEntryRepository;
  private final WaitingQueueRepository waitingQueueRepository;
  private final EventQueryRepository eventQueryRepository;

  @Transactional
  public CreateEventResponse createEvent(CreateEventRequest request) {

    Event event = Event.builder()
        .title(request.title())
        .description(request.description())
        .totalStock(request.totalStock())
        .startAt(request.startAt())
        .endAt(request.endAt())
        .build();

    Event saved = eventRepository.save(event);

    return new CreateEventResponse(saved.getId());
  }

  @Transactional(readOnly = true)
  public EventResponse getEvent(Long eventId) {

    Event event = eventRepository.findById(eventId).orElseThrow(() -> new BusinessException(
        EventErrorCode.EVENT_NOT_FOUND));

    return eventMapper.toResponse(event);
  }

  @Transactional
  public JoinQueueResponse joinQueue(Long eventId, AuthUser authUser) {
    long userId = authUser.userId();

    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new BusinessException(EventErrorCode.EVENT_NOT_FOUND));

    //이벤트 진행중인지 확인
    event.validateEventPeriod(LocalDateTime.now());

    // 중복 참여 확인 (이미 번호 선택 성공한 사람)
    if (eventEntryRepository.existsByEventIdAndUserId(eventId, userId)) {
      throw new BusinessException(EventErrorCode.ALREADY_PARTICIPATED);
    }

    // 대기열 중복 확인 (이미 대기열인 사람)
    if (waitingQueueRepository.existsByEventIdAndUserId(eventId, userId)) {
      throw new BusinessException(EventErrorCode.ALREADY_IN_QUEUE);
    }

    WaitingQueue queue = WaitingQueue.builder()
        .eventId(eventId)
        .userId(userId)
        .build();

    WaitingQueue saved = waitingQueueRepository.save(queue);

    // 내 앞순번 계산
    long position = eventQueryRepository.countAhead(saved.getEventId(), saved.getJoinAt(),
        saved.getId());

    return new JoinQueueResponse(eventId, position, saved.getJoinAt());
  }

  @Transactional(readOnly = true)
  public QueuePositionResponse getQueuePosition(long eventId, AuthUser authUser) {
    long userId = authUser.userId();

    WaitingQueue queue = waitingQueueRepository
        .findByEventIdAndUserId(eventId, userId)
        .orElseThrow(() -> new BusinessException(EventErrorCode.NOT_IN_QUEUE));

    long position = eventQueryRepository.countAhead(
        queue.getEventId(), queue.getJoinAt(), queue.getId()
    );

    return new QueuePositionResponse(position);
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

    List<GetEventsByResponse> response = events.stream()
        .map(GetEventsByResponse::fromDto)
        .toList();

    return CommonCursor.of(response, nextCursor, hasNext);
  }
}
