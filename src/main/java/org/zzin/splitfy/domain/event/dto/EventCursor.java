package org.zzin.splitfy.domain.event.dto;

import org.jspecify.annotations.Nullable;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.event.exception.EventErrorCode;

public record EventCursor(
    int statusPriority,
    long eventId
) {

  public static EventCursor of(int statusPriority, long eventId) {
    return new EventCursor(statusPriority, eventId);
  }

  @Nullable
  public static EventCursor from(@Nullable String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    String[] parts = cursor.split("_");
    if (parts.length != 2) {
      throw new BusinessException(EventErrorCode.INVALID_CURSOR);
    }

    try {
      int priority = Integer.parseInt(parts[0]);
      long eventId = Long.parseLong(parts[1]);

      if (priority < 0 || priority > 2 || eventId <= 0) {
        throw new BusinessException(EventErrorCode.INVALID_CURSOR);
      }

      return new EventCursor(priority, eventId);
    } catch (NumberFormatException e) {
      throw new BusinessException(EventErrorCode.INVALID_CURSOR);
    }
  }

  public String encode() {
    return statusPriority + "_" + eventId;
  }
}
