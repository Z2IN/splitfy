package org.zzin.splitfy.domain.auth.service;

import lombok.NonNull;
import org.jspecify.annotations.NullMarked;
import org.zzin.splitfy.domain.auth.dto.PointChangeResultDTO;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;

@NullMarked
public interface AuthInnerService {

  long getPointBy(long userId);

  @NonNull
  PointChangeResultDTO addPoint(long userId, long amount);

  @NonNull
  PointTransferSummaryDTO transferPoint(long senderId, long receiverId, long amount);

}
