package org.zzin.splitfy.domain.event.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.event.dto.request.CreateEventRequest;
import org.zzin.splitfy.domain.event.dto.response.CreateEventResponse;
import org.zzin.splitfy.domain.event.dto.response.EventResponse;
import org.zzin.splitfy.domain.event.entity.Event;
import org.zzin.splitfy.domain.event.exception.EventErrorCode;
import org.zzin.splitfy.domain.event.mapper.EventMapper;
import org.zzin.splitfy.domain.event.repository.EventRepository;

@Service
@NullMarked
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final EventMapper eventMapper;

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
}
