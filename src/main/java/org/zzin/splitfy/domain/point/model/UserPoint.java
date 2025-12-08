package org.zzin.splitfy.domain.point.model;

public record UserPoint(
    long point
) {

  public boolean hasEnoughPoint(long requiredPoint) {
    return this.point >= requiredPoint;
  }
}
