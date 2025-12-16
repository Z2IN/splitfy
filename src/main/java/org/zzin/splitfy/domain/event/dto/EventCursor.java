package org.zzin.splitfy.domain.event.dto;

import java.util.Base64;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.event.exception.EventErrorCode;

@NullMarked
public record EventCursor(
    int statusPriority,
    long eventId
) {

  public static EventCursor of(int statusPriority, long eventId) {
    return new EventCursor(statusPriority, eventId);
  }

  public static @Nullable EventCursor from(@Nullable String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    try {
      String decoded = new String(Base64.getUrlDecoder().decode(cursor));
      String[] parts = decoded.split("_");

      if (parts.length != 2) {
        throw new BusinessException(EventErrorCode.INVALID_CURSOR);
      }

      int priority = Integer.parseInt(parts[0]);
      long eventId = Long.parseLong(parts[1]);

      if (priority < 0 || priority > 2 || eventId <= 0) {
        throw new BusinessException(EventErrorCode.INVALID_CURSOR);
      }

      return new EventCursor(priority, eventId);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(EventErrorCode.INVALID_CURSOR);
    }
  }

  public String encode() {
    String raw = statusPriority + "_" + eventId;
    return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
  }
}
