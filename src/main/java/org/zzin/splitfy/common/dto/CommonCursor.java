package org.zzin.splitfy.common.dto;

public record CommonCursor(
    String cursor,
    boolean hasNext
) {

}
