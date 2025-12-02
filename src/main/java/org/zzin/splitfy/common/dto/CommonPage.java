package org.zzin.splitfy.common.dto;

import java.util.List;

public record CommonPage<T>(
    List<T> contents,
    int totalPages
) {
}