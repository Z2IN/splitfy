package org.zzin.splitfy.domain.event.mapper;

import org.springframework.stereotype.Component;
import org.zzin.splitfy.domain.event.dto.response.EventNumberResponse;
import org.zzin.splitfy.domain.event.dto.response.EventResponse;
import org.zzin.splitfy.domain.event.entity.Event;
import org.zzin.splitfy.domain.event.entity.EventNumber;

@Component
public class EventMapper {

  public EventResponse toResponse(Event event) {
    return new EventResponse(
        event.getId(),
        event.getTitle(),
        event.getDescription(),
        event.getStartAt(),
        event.getEndAt(),
        event.getTotalStock(),
        event.getStatus()
    );
  }

  public EventNumberResponse toResponse(EventNumber eventNumber) {
    return new EventNumberResponse(
        eventNumber.getNumber(),
        eventNumber.isSelected()
    );
  }

}
