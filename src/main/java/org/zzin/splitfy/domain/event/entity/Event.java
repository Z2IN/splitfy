package org.zzin.splitfy.domain.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.event.enums.EventStatus;
import org.zzin.splitfy.domain.event.exception.EventErrorCode;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "events")
public class Event {

  private static final long MIN_STOCK = 1L;
  private static final long MAX_STOCK = 100_000L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventStatus status;

  @Column(nullable = false)
  private long totalStock;

  @Column(nullable = false)
  private LocalDateTime startAt;

  @Column(nullable = false)
  private LocalDateTime endAt;


  @Builder
  private Event(String title, String description, long totalStock,
      LocalDateTime startAt, LocalDateTime endAt, EventStatus status) {

    validateEventTime(startAt, endAt);
    validateStock(totalStock);

    this.title = title;
    this.description = description;
    this.totalStock = totalStock;
    this.startAt = startAt;
    this.endAt = endAt;
    this.status = (status != null ? status : EventStatus.SCHEDULED);
  }

  @NullMarked
  private void validateEventTime(LocalDateTime startAt, LocalDateTime endAt) {
    LocalDateTime now = LocalDateTime.now();

    if (startAt.isBefore(now)) {
      throw new BusinessException(EventErrorCode.PAST_START_TIME);
    }
    if (startAt.isAfter(endAt)) {
      throw new BusinessException(EventErrorCode.INVALID_EVENT_TIME);
    }
  }

  private void validateStock(long totalStock) {
    if (totalStock < MIN_STOCK) {
      throw new BusinessException(EventErrorCode.INVALID_STOCK);
    }
    if (totalStock > MAX_STOCK) {
      throw new BusinessException(EventErrorCode.STOCK_LIMIT_EXCEEDED);
    }
  }

  @NullMarked
  public void validateEventPeriod(LocalDateTime now) {
    if (now.isBefore(this.startAt)) {
      throw new BusinessException(EventErrorCode.EVENT_NOT_STARTED);
    }
    if (now.isAfter(this.endAt)) {
      throw new BusinessException(EventErrorCode.EVENT_ENDED);
    }
  }

}
