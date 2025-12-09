package org.zzin.splitfy.domain.event;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.event.dto.response.EventResponse;
import org.zzin.splitfy.domain.event.entity.Event;
import org.zzin.splitfy.domain.event.exception.EventErrorCode;
import org.zzin.splitfy.domain.event.mapper.EventMapper;
import org.zzin.splitfy.domain.event.repository.EventRepository;
import org.zzin.splitfy.domain.event.service.EventService;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private EventMapper eventMapper;

  @InjectMocks
  private EventService eventService;

  private Event createEvent() {
    LocalDateTime now = LocalDateTime.now();
    return Event.builder()
        .title("테스트 이벤트")
        .description("설명")
        .startAt(now.plusDays(1))
        .endAt(now.plusDays(2))
        .totalStock(100L)
        .build();
  }

  @Test
  void getEvent_존재하는_이벤트조회_성공() {
    // given
    Long eventId = 1L;
    Event event = createEvent();
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
}
