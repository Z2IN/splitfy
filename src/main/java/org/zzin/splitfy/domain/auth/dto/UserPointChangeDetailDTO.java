package org.zzin.splitfy.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserPointChangeDetailDTO {

  private final long beforePoint;
  private final long afterPoint;
}
