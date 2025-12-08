package org.zzin.splitfy.domain.point.dto.response;

import lombok.Builder;

@Builder
public record TransferResponse(long amount, long beforePoint, long afterPoint) {
}
