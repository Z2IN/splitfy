package org.zzin.splitfy.domain.settlement.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.zzin.splitfy.domain.settlement.enums.SettlementStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlements")
public class Settlement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long issuerId;

  @Column(nullable = false)
  private Long totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SettlementStatus status;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime issuedAt;

  @Column
  private LocalDateTime succeededAt;
}
