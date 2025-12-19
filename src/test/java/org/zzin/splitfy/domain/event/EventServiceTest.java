package org.zzin.splitfy.domain.event;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.event.dto.request.SelectEventNumberRequest;
import org.zzin.splitfy.domain.event.dto.response.EventResponse;
import org.zzin.splitfy.domain.event.dto.response.EventRewardResponse;
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
import org.zzin.splitfy.domain.event.repository.EventQueryRepository;
import org.zzin.splitfy.domain.event.repository.EventRepository;
import org.zzin.splitfy.domain.event.repository.WaitingQueueRepository;
import org.zzin.splitfy.domain.event.service.EventService;
import org.zzin.splitfy.domain.point.Service.PointInnerService;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private EventMapper eventMapper;

  @Mock
  private EventEntryRepository eventEntryRepository;

  @Mock
  private WaitingQueueRepository waitingQueueRepository;

  @Mock
  private EventQueryRepository eventQueryRepository;

  @Mock
  private PointInnerService pointInnerService;

  @InjectMocks
  private EventService eventService;

  @Test
  void getEvent_존재하는_이벤트조회_성공() {
    // given
    Long eventId = 1L;
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);
    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));

    EventResponse mapped = new EventResponse(
        1L,
        event.getTitle(),
        event.getDescription(),
        event.getStartAt(),
        event.getEndAt(),
        event.getTotalStock(),
        event.getStatus()
    );
    given(eventMapper.toResponse(event)).willReturn(mapped);

    // when
    EventResponse response = eventService.getEvent(eventId);

    // then
    then(eventRepository).should(times(1)).findById(eventId);
    then(eventMapper).should(times(1)).toResponse(event);
    assertThat(response).isEqualTo(mapped);
  }

  @Test
  void getEvent_이벤트가_존재하지않으면_예외발생() {
    // given
    Long eventId = 1L;
    given(eventRepository.findById(eventId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> eventService.getEvent(eventId))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());

    then(eventRepository).should(times(1)).findById(eventId);
  }

  @Test
  void joinQueue_첫번째_참가자_성공() {
    // given
    long eventId = 1L;
    long userId = 100L;
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    WaitingQueue savedQueue = createWaitingQueue(1L, eventId, userId);

    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventEntryRepository.existsByEventIdAndUserId(eventId, userId)).willReturn(false);
    given(waitingQueueRepository.existsByEventIdAndUserId(eventId, userId)).willReturn(false);
    given(waitingQueueRepository.save(any(WaitingQueue.class))).willReturn(savedQueue);
    given(
        eventQueryRepository.countAhead(anyLong(), any(LocalDateTime.class), anyLong())).willReturn(
        0L);

    // when
    JoinQueueResponse response = eventService.joinQueue(eventId, new AuthUser(userId));

    // then
    assertThat(response.eventId()).isEqualTo(eventId);
    assertThat(response.position()).isEqualTo(0L);
    then(waitingQueueRepository).should(times(1)).save(any(WaitingQueue.class));
    then(eventQueryRepository).should(times(1))
        .countAhead(anyLong(), any(LocalDateTime.class), anyLong());
  }

  @Test
  void joinQueue_여러명_참가후_순번_정상_계산() {
    // given
    Long eventId = 1L;
    Long userId = 101L;
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    WaitingQueue savedQueue = createWaitingQueue(5L, eventId, userId);

    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventEntryRepository.existsByEventIdAndUserId(eventId, userId)).willReturn(false);
    given(waitingQueueRepository.existsByEventIdAndUserId(eventId, userId)).willReturn(false);
    given(waitingQueueRepository.save(any(WaitingQueue.class))).willReturn(savedQueue);
    given(
        eventQueryRepository.countAhead(anyLong(), any(LocalDateTime.class), anyLong())).willReturn(
        4L); // 앞에 4명

    // when
    JoinQueueResponse response = eventService.joinQueue(eventId, new AuthUser(userId));

    // then
    assertThat(response.position()).isEqualTo(4L);
  }

  @Test
  void joinQueue_이벤트_존재하지않으면_예외발생() {
    // given
    Long eventId = 999L;
    Long userId = 100L;

    given(eventRepository.findById(eventId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> eventService.joinQueue(eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());

    then(waitingQueueRepository).should(never()).save(any(WaitingQueue.class));
  }

  @Test
  void joinQueue_이벤트_시작전이면_예외발생() {
    // given
    Long eventId = 1L;
    Long userId = 100L;
    LocalDateTime now = LocalDateTime.now();

    Event futureEvent = createEventWithTime("미래 이벤트", now.plusDays(1), now.plusDays(2),
        EventStatus.OPENED, 100L);

    given(eventRepository.findById(eventId)).willReturn(Optional.of(futureEvent));

    // when & then
    assertThatThrownBy(() -> eventService.joinQueue(eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.EVENT_NOT_STARTED.getMessage());

    then(waitingQueueRepository).should(never()).save(any(WaitingQueue.class));
  }

  @Test
  void joinQueue_이벤트_종료후면_예외발생() {
    // given
    Long eventId = 1L;
    Long userId = 100L;
    LocalDateTime now = LocalDateTime.now();

    Event endedEvent = createEventWithTime("종료된 이벤트", now.minusDays(2), now.minusDays(1),
        EventStatus.CLOSED, 100L);

    given(eventRepository.findById(eventId)).willReturn(Optional.of(endedEvent));

    // when & then
    assertThatThrownBy(() -> eventService.joinQueue(eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.EVENT_ENDED.getMessage());

    then(waitingQueueRepository).should(never()).save(any(WaitingQueue.class));
  }

  @Test
  void joinQueue_이미_참여한_사용자면_예외발생() {
    // given
    Long eventId = 1L;
    Long userId = 100L;
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventEntryRepository.existsByEventIdAndUserId(eventId, userId)).willReturn(true); // 이미 참여

    // when & then
    assertThatThrownBy(() -> eventService.joinQueue(eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.ALREADY_PARTICIPATED.getMessage());

    then(waitingQueueRepository).should(never()).save(any(WaitingQueue.class));
  }

  @Test
  void joinQueue_이미_대기열에_있는_사용자면_예외발생() {
    // given
    Long eventId = 1L;
    Long userId = 100L;
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventEntryRepository.existsByEventIdAndUserId(eventId, userId)).willReturn(false);
    given(waitingQueueRepository.existsByEventIdAndUserId(eventId, userId)).willReturn(
        true); // 이미 대기열에 있음

    // when & then
    assertThatThrownBy(() -> eventService.joinQueue(eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.ALREADY_IN_QUEUE.getMessage());

    then(waitingQueueRepository).should(never()).save(any(WaitingQueue.class));
  }


  @Test
  void getQueuePosition_대기열_존재하면_내_앞사람_수_반환() {
    // given
    Long eventId = 10L;
    long userId = 99L;

    AuthUser authUser = mock(AuthUser.class);
    given(authUser.userId()).willReturn(userId);

    LocalDateTime joinAt = LocalDateTime.of(2025, 12, 15, 10, 0);
    Long queueId = 777L;

    WaitingQueue queue = mock(WaitingQueue.class);
    given(queue.getEventId()).willReturn(eventId);
    given(queue.getJoinAt()).willReturn(joinAt);
    given(queue.getId()).willReturn(queueId);

    given(waitingQueueRepository.findByEventIdAndUserId(eventId, userId))
        .willReturn(Optional.of(queue));

    long aheadCount = 5L;
    given(eventQueryRepository.countAhead(eventId, joinAt, queueId))
        .willReturn(aheadCount);

    // when
    QueuePositionResponse response = eventService.getQueuePosition(eventId, authUser);

    // then
    assertThat(response).isNotNull();
    assertThat(response.position()).isEqualTo(aheadCount);

    then(waitingQueueRepository).should().findByEventIdAndUserId(eventId, userId);
    then(eventQueryRepository).should().countAhead(eventId, joinAt, queueId);
  }

  @Test
  void getQueuePosition_대기열_없으면_NOT_IN_QUEUE_예외() {
    // given
    Long eventId = 10L;
    long userId = 99L;

    AuthUser authUser = mock(AuthUser.class);
    given(authUser.userId()).willReturn(userId);

    given(waitingQueueRepository.findByEventIdAndUserId(eventId, userId))
        .willReturn(Optional.empty());

    // when / then
    assertThatThrownBy(() -> eventService.getQueuePosition(eventId, authUser))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(
            EventErrorCode.NOT_IN_QUEUE.getMessage());
  }

  @Test
  void selectEventNumber_정상적으로_번호_선택_성공() {
    // given
    Long eventId = 1L;
    long userId = 100L;
    int selectedNumber = 7;
    long reward = 1000L;
    LocalDateTime now = LocalDateTime.now();

    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    WaitingQueue headQueue = mock(WaitingQueue.class);
    given(headQueue.getUserId()).willReturn(userId);

    EventNumber eventNumber = createEventNumber(eventId, selectedNumber, reward, false);

    SelectEventNumberRequest request = new SelectEventNumberRequest(selectedNumber);

    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventQueryRepository.findHeadForUpdate(eventId)).willReturn(headQueue);
    given(eventQueryRepository.findEventNumberForUpdate(eventId, selectedNumber)).willReturn(
        eventNumber);

    // when
    EventRewardResponse response = eventService.selectEventNumber(request, eventId,
        new AuthUser(userId));

    // then
    assertThat(response.number()).isEqualTo(selectedNumber);
    assertThat(response.reward()).isEqualTo(reward);
    assertThat(eventNumber.isSelected()).isTrue(); // select() 호출 후 상태 확인
    then(eventEntryRepository).should(times(1)).save(any(EventEntry.class));
    then(waitingQueueRepository).should(times(1)).delete(headQueue);
  }

  @Test
  void selectEventNumber_이벤트_존재하지않으면_예외발생() {
    // given
    Long eventId = 999L;
    long userId = 100L;
    int selectedNumber = 7;

    SelectEventNumberRequest request = new SelectEventNumberRequest(selectedNumber);
    given(eventRepository.findById(eventId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> eventService.selectEventNumber(request, eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());

    then(eventQueryRepository).should(never()).findHeadForUpdate(anyLong());
  }

  @Test
  void selectEventNumber_이벤트_시작전이면_예외발생() {
    // given
    Long eventId = 1L;
    long userId = 100L;
    int selectedNumber = 7;
    LocalDateTime now = LocalDateTime.now();

    Event futureEvent = createEventWithTime("미래 이벤트", now.plusDays(1), now.plusDays(2),
        EventStatus.OPENED, 100L);

    SelectEventNumberRequest request = new SelectEventNumberRequest(selectedNumber);
    given(eventRepository.findById(eventId)).willReturn(Optional.of(futureEvent));

    // when & then
    assertThatThrownBy(
        () -> eventService.selectEventNumber(request, eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.EVENT_NOT_STARTED.getMessage());

    then(eventQueryRepository).should(never()).findHeadForUpdate(anyLong());
  }

  @Test
  void selectEventNumber_이벤트_종료후면_예외발생() {
    // given
    Long eventId = 1L;
    long userId = 100L;
    int selectedNumber = 7;
    LocalDateTime now = LocalDateTime.now();

    Event endedEvent = createEventWithTime("종료된 이벤트", now.minusDays(2), now.minusDays(1),
        EventStatus.CLOSED, 100L);

    SelectEventNumberRequest request = new SelectEventNumberRequest(selectedNumber);
    given(eventRepository.findById(eventId)).willReturn(Optional.of(endedEvent));

    // when & then
    assertThatThrownBy(
        () -> eventService.selectEventNumber(request, eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.EVENT_ENDED.getMessage());

    then(eventQueryRepository).should(never()).findHeadForUpdate(anyLong());
  }

  @Test
  void selectEventNumber_내_차례가_아니면_예외발생() {
    // given
    Long eventId = 1L;
    long userId = 100L;
    long headUserId = 200L; // 다른 사용자가 1순위
    int selectedNumber = 7;
    LocalDateTime now = LocalDateTime.now();

    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    WaitingQueue headQueue = mock(WaitingQueue.class);
    given(headQueue.getUserId()).willReturn(headUserId);

    SelectEventNumberRequest request = new SelectEventNumberRequest(selectedNumber);
    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventQueryRepository.findHeadForUpdate(eventId)).willReturn(headQueue);

    // when & then
    assertThatThrownBy(
        () -> eventService.selectEventNumber(request, eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.NOT_YOUR_TURN.getMessage());

    then(eventQueryRepository).should(never()).findEventNumberForUpdate(anyLong(), anyInt());
  }

  @Test
  void selectEventNumber_대기열이_비어있으면_예외발생() {
    // given
    Long eventId = 1L;
    long userId = 100L;
    int selectedNumber = 7;
    LocalDateTime now = LocalDateTime.now();

    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    SelectEventNumberRequest request = new SelectEventNumberRequest(selectedNumber);
    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventQueryRepository.findHeadForUpdate(eventId)).willReturn(null); // 대기열 비어있음

    // when & then
    assertThatThrownBy(
        () -> eventService.selectEventNumber(request, eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.NOT_YOUR_TURN.getMessage());

    then(eventQueryRepository).should(never()).findEventNumberForUpdate(anyLong(), anyInt());
  }

  @Test
  void selectEventNumber_번호가_존재하지않으면_예외발생() {
    // given
    Long eventId = 1L;
    long userId = 100L;
    int selectedNumber = 999; // 존재하지 않는 번호
    LocalDateTime now = LocalDateTime.now();

    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    WaitingQueue headQueue = mock(WaitingQueue.class);
    given(headQueue.getUserId()).willReturn(userId);

    SelectEventNumberRequest request = new SelectEventNumberRequest(selectedNumber);
    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventQueryRepository.findHeadForUpdate(eventId)).willReturn(headQueue);
    given(eventQueryRepository.findEventNumberForUpdate(eventId, selectedNumber)).willReturn(
        null); // 번호 없음

    // when & then
    assertThatThrownBy(
        () -> eventService.selectEventNumber(request, eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.NUMBER_NOT_FOUND.getMessage());

    then(eventEntryRepository).should(never()).save(any());
  }

  @Test
  void selectEventNumber_이미_선택된_번호면_예외발생() {
    // given
    Long eventId = 1L;
    long userId = 100L;
    int selectedNumber = 7;
    LocalDateTime now = LocalDateTime.now();

    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    WaitingQueue headQueue = mock(WaitingQueue.class);
    given(headQueue.getUserId()).willReturn(userId);

    EventNumber eventNumber = createEventNumber(eventId, selectedNumber, 1000L,
        true); // 이미 선택됨

    SelectEventNumberRequest request = new SelectEventNumberRequest(selectedNumber);
    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventQueryRepository.findHeadForUpdate(eventId)).willReturn(headQueue);
    given(eventQueryRepository.findEventNumberForUpdate(eventId, selectedNumber)).willReturn(
        eventNumber);

    // when & then
    assertThatThrownBy(
        () -> eventService.selectEventNumber(request, eventId, new AuthUser(userId)))
        .isInstanceOf(BusinessException.class)
        .hasMessage(EventErrorCode.NUMBER_ALREADY_TAKEN.getMessage());

    then(eventEntryRepository).should(never()).save(any());
  }

  //====================== 편의 메서드 ============================

  private Event createEventWithTime(String title, LocalDateTime startAt, LocalDateTime endAt,
      EventStatus status, long totalStock) {
    // Validation을 피하기 위해 일단 미래 시간으로 생성
    LocalDateTime tempTime = LocalDateTime.now().plusDays(10);
    Event event = Event.builder()
        .title(title)
        .description("설명")
        .startAt(tempTime)
        .endAt(tempTime.plusDays(1))
        .totalStock(totalStock)
        .status(status)
        .build();

    // Reflection으로 실제 원하는 시간 설정
    try {
      var startAtField = Event.class.getDeclaredField("startAt");
      startAtField.setAccessible(true);
      startAtField.set(event, startAt);

      var endAtField = Event.class.getDeclaredField("endAt");
      endAtField.setAccessible(true);
      endAtField.set(event, endAt);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return event;
  }

  private WaitingQueue createWaitingQueue(Long id, Long eventId, Long userId) {
    WaitingQueue queue = WaitingQueue.builder()
        .eventId(eventId)
        .userId(userId)
        .build();

    // Reflection을 사용하여 id와 joinAt 설정
    try {
      var idField = WaitingQueue.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(queue, id);

      var joinAtField = WaitingQueue.class.getDeclaredField("joinAt");
      joinAtField.setAccessible(true);
      joinAtField.set(queue, LocalDateTime.now().plusDays(2));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return queue;
  }

  private EventNumber createEventNumber(Long eventId, int number, long reward,
      boolean selected) {
    try {
      var constructor = EventNumber.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      EventNumber eventNumber = constructor.newInstance();

      var eventIdField = EventNumber.class.getDeclaredField("eventId");
      eventIdField.setAccessible(true);
      eventIdField.set(eventNumber, eventId);

      var numberField = EventNumber.class.getDeclaredField("number");
      numberField.setAccessible(true);
      numberField.set(eventNumber, number);

      var rewardField = EventNumber.class.getDeclaredField("reward");
      rewardField.setAccessible(true);
      rewardField.set(eventNumber, reward);

      var selectedField = EventNumber.class.getDeclaredField("selected");
      selectedField.setAccessible(true);
      selectedField.set(eventNumber, selected);

      return eventNumber;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
