package org.zzin.splitfy.domain.point.Service;

public interface PointInnerService {

  long initUserPoint(long userId);

  /* 
  TODO
  @NonNull
  PointChangeResultDTO addPoint(long userId, long amount);
  
  @NonNull
  PointTransferSummaryDTO transferPoint(long senderId, long receiverId, long amount); 
  */
}
