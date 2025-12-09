package org.zzin.splitfy.domain.event;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
}
