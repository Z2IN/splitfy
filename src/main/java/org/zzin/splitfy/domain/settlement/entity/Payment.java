package org.zzin.splitfy.domain.settlement.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "settlement_id", nullable = false)
  private long settlementId;

  @Column(name = "paid_amount", nullable = false)
  private long paidAmount;

  @Column(name = "payer_id", nullable = false)
  private long payerId;

  @Column(name = "share_amount", nullable = false)
  private long shareAmount;

  @Column(nullable = false)
  private String title;

  public Payment(long paidAmount, long payerId, long shareAmount, String title) {
    this.paidAmount = paidAmount;
    this.payerId = payerId;
    this.shareAmount = shareAmount;
    this.title = title;
  }

  public void setSettlementId(long settlementId) {
    this.settlementId = settlementId;
  }
}
