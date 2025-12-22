package org.zzin.splitfy.domain.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "waiting_queues")
public class WaitingQueue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private long eventId;

  @Column(nullable = false)
  private long userId;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime joinAt; //대기열에 참가한 시간

  private LocalDateTime expireAt; //입장 순서 후 만료 시간(입장 후 30초)

  @Builder
  public WaitingQueue(long eventId, long userId) {
    this.eventId = eventId;
    this.userId = userId;
  }

  public void startTurn(LocalDateTime now) {
    if (this.expireAt != null) {
      return;
    }
    expireAt = now.plusSeconds(30);
  }

}
