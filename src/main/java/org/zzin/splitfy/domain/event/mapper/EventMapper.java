package org.zzin.splitfy.domain.event.mapper;

import org.zzin.splitfy.domain.event.dto.response.EventResponse;
import org.zzin.splitfy.domain.event.entity.Event;

public class EventMapper {

  public static EventResponse toResponse(Event event) {
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

}
