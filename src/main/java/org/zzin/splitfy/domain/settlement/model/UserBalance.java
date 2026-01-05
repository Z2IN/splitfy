package org.zzin.splitfy.domain.settlement.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserBalance {

  private final long userId;
  private long remaining;

  public void minusRemaining(long amount) {
    this.remaining -= amount;
  }

  public boolean isRemainingZero() {
    return this.remaining == 0;
  }
}
