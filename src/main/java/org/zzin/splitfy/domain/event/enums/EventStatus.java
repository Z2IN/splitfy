package org.zzin.splitfy.domain.event.enums;

public enum EventStatus {

  SCHEDULED, // 이벤트 시작 전
  OPENED, // 이벤트 시작(대기열, 참여 가능)
  CLOSED, // 이벤트 마감

}
