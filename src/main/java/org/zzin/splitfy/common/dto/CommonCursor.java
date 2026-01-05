package org.zzin.splitfy.common.dto;

import java.util.List;

public record CommonCursor<T>(
    List<T> contents,
    String nextCursor,
    boolean hasNext
) {

  public static <T> CommonCursor<T> of(List<T> contents, String nextCursor,
      boolean hasNext) {
    return new CommonCursor<>(contents, nextCursor, hasNext);
  }
}
