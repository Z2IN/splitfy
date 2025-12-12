package org.zzin.splitfy.domain.event;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.event.entity.Event;
import org.zzin.splitfy.domain.event.enums.EventStatus;
import org.zzin.splitfy.domain.event.exception.EventErrorCode;

public class EventEntityTest {

  @Test
  void createEvent_Event_생성_성공() {
    // given
    LocalDateTime startAt = LocalDateTime.now().plusHours(1);
    LocalDateTime endAt = startAt.plusHours(2);

    // when
    Event event = Event.builder()
        .title("Test Event")
        .description("description")
        .totalStock(10L)
        .startAt(startAt)
        .endAt(endAt)
        .build();

    // then
    assertThat(event).isNotNull();
    assertThat(event.getTitle()).isEqualTo("Test Event");
    assertThat(event.getStatus()).isEqualTo(EventStatus.SCHEDULED);
  }

  @Test
  void createEvent_시작_시간이_현재보다_이전이면_PAST_START_TIME_예외_발생() {
    // Given
    LocalDateTime startAt = LocalDateTime.now().minusHours(1);
    LocalDateTime endAt = LocalDateTime.now().plusHours(1);

    // When & Then
    assertThatThrownBy(() ->
        Event.builder()
            .title("title")
            .description("desc")
            .totalStock(10L)
            .startAt(startAt)
            .endAt(endAt)
            .build()
    )
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.PAST_START_TIME);
  }

  @Test
  void createEvent_시작_시간이_종료_시간보다_늦으면_INVALID_EVENT_TIME_예외_발생() {
    // Given
    LocalDateTime startAt = LocalDateTime.now().plusHours(3);
    LocalDateTime endAt = LocalDateTime.now().plusHours(1);

    // When & Then
    assertThatThrownBy(() ->
        Event.builder()
            .title("title")
            .description("desc")
            .totalStock(10L)
            .startAt(startAt)
            .endAt(endAt)
            .build()
    )
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_EVENT_TIME);
  }

  @Test
  void createEvent_재고가_최소값보다_작으면_INVALID_STOCK_예외_발생() {
    // Given
    LocalDateTime startAt = LocalDateTime.now().plusHours(1);
    LocalDateTime endAt = startAt.plusHours(1);

    // When & Then
    assertThatThrownBy(() ->
        Event.builder()
            .title("title")
            .description("desc")
            .totalStock(0L)
            .startAt(startAt)
            .endAt(endAt)
            .build()
    )
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_STOCK);
  }


  @Test
  void createEvent_재고가_최대치를_초과하면_STOCK_LIMIT_EXCEEDED_예외_발생() {
    // Given
    LocalDateTime startAt = LocalDateTime.now().plusHours(1);
    LocalDateTime endAt = startAt.plusHours(1);

    // When & Then
    assertThatThrownBy(() ->
        Event.builder()
            .title("title")
            .description("desc")
            .totalStock(100_001L)
            .startAt(startAt)
            .endAt(endAt)
            .build()
    )
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.STOCK_LIMIT_EXCEEDED);
  }

  @Test
  void validateEventPeriod_진행중인_이벤트는_검증_성공() {
    // given
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime(now.minusHours(1), now.plusHours(1));

    // when & then
    // 예외가 발생하지 않아야 함
    assertThatCode(() -> event.validateEventPeriod(now))
        .doesNotThrowAnyException();
  }

  @Test
  void validateEventPeriod_시작전_이벤트는_EVENT_NOT_STARTED_예외_발생() {
    // given
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime(now.plusHours(1), now.plusHours(2));

    // when & then
    assertThatThrownBy(() -> event.validateEventPeriod(now))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_NOT_STARTED);
  }

  @Test
  void validateEventPeriod_종료된_이벤트는_EVENT_ENDED_예외_발생() {
    // given
    LocalDateTime now = LocalDateTime.now();
    Event event = createEventWithTime(now.minusDays(2), now.minusDays(1));

    // when & then
    assertThatThrownBy(() -> event.validateEventPeriod(now))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.EVENT_ENDED);
  }

  private Event createEventWithTime(LocalDateTime startAt, LocalDateTime endAt) {
    // Validation을 피하기 위해 일단 미래 시간으로 생성
    LocalDateTime tempTime = LocalDateTime.now().plusDays(10);
    Event event = Event.builder()
        .title("테스트 이벤트")
        .description("설명")
        .startAt(tempTime)
        .endAt(tempTime.plusDays(1))
        .totalStock(100L)
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

}
