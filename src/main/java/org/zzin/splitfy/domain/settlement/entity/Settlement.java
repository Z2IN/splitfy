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
import org.springframework.data.annotation.CreatedDate;
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
  private long totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SettlementStatus status;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime issuedAt;

  @Column
  private LocalDateTime succeededAt;

  public Settlement(Long issuerId, long totalAmount) {
    this.issuerId = issuerId;
    this.totalAmount = totalAmount;
    this.status = SettlementStatus.PENDING;
    this.issuedAt = LocalDateTime.now();
  }

  public Payment createPayment(long paidAmount, long payerId, String title) {
    Payment payment = new Payment(paidAmount, payerId, paidAmount, title);
    payment.setSettlementId(this.id);
    return payment;
  }

  public SettlementParticipant createParticipant(Long userId, long netAmount) {
    return new SettlementParticipant(this, userId, netAmount);
  }
}
