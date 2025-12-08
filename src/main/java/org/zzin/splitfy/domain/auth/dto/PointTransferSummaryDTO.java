package org.zzin.splitfy.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class PointTransferSummaryDTO {

  private final long senderBeforePoint;
  private final long senderAfterPoint;
  private final long receiverBeforePoint;
  private final long receiverAfterPoint;
}
