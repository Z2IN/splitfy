package org.zzin.splitfy.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record CommonPage<T>(
    List<T> contents,
    int totalPages
) {

  public static <T> CommonPage<T> of(Page<T> page) {
    return new CommonPage<>(page.getContent(), page.getTotalPages());
  }
}