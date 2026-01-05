package org.zzin.splitfy.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.event.dto.request.CreateEventRequest;
import org.zzin.splitfy.domain.event.dto.request.SelectEventNumberRequest;
import org.zzin.splitfy.domain.event.dto.response.CreateEventResponse;
import org.zzin.splitfy.domain.event.dto.response.EventNumberListResponse;
import org.zzin.splitfy.domain.event.dto.response.EventNumberResponse;
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
import org.zzin.splitfy.domain.event.repository.EventNumberRepository;
import org.zzin.splitfy.domain.event.repository.EventQueryRepository;
import org.zzin.splitfy.domain.event.repository.EventRepository;
import org.zzin.splitfy.domain.event.repository.WaitingQueueRepository;
import org.zzin.splitfy.domain.event.service.EventService;
import org.zzin.splitfy.domain.point.service.PointInnerService;

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

  @Mock
  private EventNumberRepository eventNumberRepository;

  @InjectMocks
  private EventService eventService;


  @Test
  void createEvent_정상적으로_이벤트와_번호판_생성() {
    // given
    CreateEventRequest request = new CreateEventRequest(
        "테스트 이벤트",
        "설명",
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now().plusDays(2),
        10
    );

    Event savedEvent = Event.builder()
        .title(request.title())
        .description(request.description())
        .startAt(request.startAt())
        .endAt(request.endAt())
        .totalStock(request.totalStock())
        .status(EventStatus.SCHEDULED)
        .build();

    ReflectionTestUtils.setField(savedEvent, "id", 1L);

    given(eventRepository.save(any(Event.class))).willReturn(savedEvent);
    ArgumentCaptor<List<EventNumber>> captor = ArgumentCaptor.forClass(List.class);

    // when
    CreateEventResponse response = eventService.createEvent(request);

    // then
    assertThat(response.eventId()).isEqualTo(1L);
    then(eventRepository).should(times(1)).save(any(Event.class));
    then(eventNumberRepository).should(times(1)).saveAll(captor.capture());

    List<EventNumber> numbers = captor.getValue();

    // 개수 및 보상 규칙 검증
    assertThat(numbers)
        .hasSize(10)
        .allMatch(n -> n.getReward() >= 2_000)
        .allMatch(n -> n.getReward() % 1_000 == 0);

    // 보상 합계 검증
    long totalReward = numbers.stream()
        .mapToLong(EventNumber::getReward)
        .sum();
    assertThat(totalReward).isEqualTo(200_000L);

    // 번호 연속성 검증 (1~10)
    assertThat(numbers)
        .extracting(EventNumber::getNumber)
        .containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
  }


  @Test
  void getEvent_존재하는_이벤트조회_성공() {
    // given
    Long eventId = 1L;
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 10L);
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
  void getEventNumbers_정상적으로_번호_목록_조회_성공() {
    // given
    Long eventId = 1L;
    long userId = 100L;
    LocalDateTime now = LocalDateTime.now();

    Event event = createEventWithTime("진행중인 이벤트", now.minusHours(1), now.plusHours(1),
        EventStatus.OPENED, 100L);

    WaitingQueue headQueue = mock(WaitingQueue.class);
    given(headQueue.getUserId()).willReturn(userId);

    EventNumber number1 = createEventNumber(eventId, 1, 1000L, false);
    EventNumber number2 = createEventNumber(eventId, 2, 2000L, true);
    EventNumber number3 = createEventNumber(eventId, 3, 500L, false);

    EventNumberResponse response1 = new EventNumberResponse(1, false);
    EventNumberResponse response2 = new EventNumberResponse(2, true);
    EventNumberResponse response3 = new EventNumberResponse(3, false);

    given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
    given(eventQueryRepository.findHead(eventId)).willReturn(headQueue);
    given(eventNumberRepository.findByEventIdOrderByNumberAsc(eventId))
        .willReturn(java.util.List.of(number1, number2, number3));
    given(eventMapper.toResponse(number1)).willReturn(response1);
    given(eventMapper.toResponse(number2)).willReturn(response2);
    given(eventMapper.toResponse(number3)).willReturn(response3);

    // when
    EventNumberListResponse response = eventService.getEventNumbers(eventId, new AuthUser(userId));

    // then
    assertThat(response.eventId()).isEqualTo(eventId);
    assertThat(response.numbers()).hasSize(3);
    assertThat(response.numbers().get(0).number()).isEqualTo(1);
    assertThat(response.numbers().get(0).isSelected()).isFalse();
    assertThat(response.numbers().get(1).number()).isEqualTo(2);
    assertThat(response.numbers().get(1).isSelected()).isTrue();
    assertThat(response.numbers().get(2).number()).isEqualTo(3);
    assertThat(response.numbers().get(2).isSelected()).isFalse();

    then(eventRepository).should(times(1)).findById(eventId);
    then(eventQueryRepository).should(times(1)).findHead(eventId);
    then(eventNumberRepository).should(times(1)).findByEventIdOrderByNumberAsc(eventId);
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
    ReflectionTestUtils.setField(event, "startAt", startAt);
    ReflectionTestUtils.setField(event, "endAt", endAt);

    return event;
  }

  private WaitingQueue createWaitingQueue(Long id, Long eventId, Long userId) {
    WaitingQueue queue = WaitingQueue.builder()
        .eventId(eventId)
        .userId(userId)
        .build();

    // Reflection을 사용하여 id와 joinAt 설정
    ReflectionTestUtils.setField(queue, "id", id);
    ReflectionTestUtils.setField(queue, "joinAt", LocalDateTime.now().plusDays(2));

    return queue;
  }

  private EventNumber createEventNumber(Long eventId, int number, long reward,
      boolean selected) {
    EventNumber eventNumber = EventNumber.builder()
        .eventId(eventId)
        .number(number)
        .reward(reward)
        .build();

    if (selected) {
      ReflectionTestUtils.setField(eventNumber, "selected", true);
    }

    return eventNumber;
  }
}
