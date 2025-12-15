package org.zzin.splitfy.common.dto;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record CommonCursor<T>(
    List<T> contents,
    @Nullable String nextCursor,
    boolean hasNext
) {

  public static <T> CommonCursor<T> of(List<T> contents, @Nullable String nextCursor,
      boolean hasNext) {
    return new CommonCursor<>(contents, nextCursor, hasNext);
  }
}
