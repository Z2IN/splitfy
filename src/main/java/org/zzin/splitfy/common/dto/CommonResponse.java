package org.zzin.splitfy.common.dto;

import edu.umd.cs.findbugs.annotations.Nullable;

public record CommonResponse<T>(
    boolean success,
    @Nullable String message,
    @Nullable T data
) {

  public static <T> CommonResponse<T> success(@Nullable T data) {
    return new CommonResponse<>(true, null, data);
  }

  public static <T> CommonResponse<T> failure(String message) {
    return new CommonResponse<>(false, message, null);
  }
}