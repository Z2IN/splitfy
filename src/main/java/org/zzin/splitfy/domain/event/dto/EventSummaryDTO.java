package org.zzin.splitfy.domain.event.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.zzin.splitfy.domain.event.enums.EventStatus;

@Builder
@AllArgsConstructor
@Getter
@NullMarked
public class EventSummaryDTO {

  private final long eventId;
  private final String title;
  private final LocalDateTime startAt;
  private final LocalDateTime endAt;
  private final long totalStock;
  private final EventStatus status;

}
