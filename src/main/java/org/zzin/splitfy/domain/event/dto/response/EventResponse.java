package org.zzin.splitfy.domain.event.dto.response;

import java.time.LocalDateTime;
import org.zzin.splitfy.domain.event.enums.EventStatus;

public record EventResponse(
    Long id,
    String title,
    String description,
    LocalDateTime startAt,
    LocalDateTime endAt,
    long totalStock,
    EventStatus eventStatus
) {

}
