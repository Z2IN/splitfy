package org.zzin.splitfy.domain.event.dto.response;

import java.time.LocalDateTime;
import org.zzin.splitfy.domain.event.dto.EventSummaryDTO;
import org.zzin.splitfy.domain.event.enums.EventStatus;

public record GetEventsByResponse(
    long eventId,
    String title,
    LocalDateTime startAt,
    LocalDateTime endAt,
    long totalStock,
    EventStatus status
) {

  public static GetEventsByResponse fromDto(EventSummaryDTO dto) {
    return new GetEventsByResponse(
        dto.getEventId(),
        dto.getTitle(),
        dto.getStartAt(),
        dto.getEndAt(),
        dto.getTotalStock(),
        dto.getStatus()
    );
  }

}
