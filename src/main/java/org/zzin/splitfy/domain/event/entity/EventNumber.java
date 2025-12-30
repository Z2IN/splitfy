package org.zzin.splitfy.domain.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "event_numbers")
public class EventNumber {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private long eventId;

  @Column(nullable = false)
  private int number;

  @Column(nullable = false)
  private long reward;

  @Column(nullable = false)
  private boolean selected;

  @Builder
  public EventNumber(long eventId, int number, long reward, boolean selected) {
    this.eventId = eventId;
    this.number = number;
    this.reward = reward;
  }

  public void select() {
    this.selected = true;
  }
}
