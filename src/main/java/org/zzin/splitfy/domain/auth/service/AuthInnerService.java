package org.zzin.splitfy.domain.auth.service;

import lombok.NonNull;
import org.jspecify.annotations.NullMarked;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;

@NullMarked
public interface AuthInnerService {

  @NonNull
  PointTransferSummaryDTO transferPoint(long senderId, long receiverId, long amount);

}
