package org.zzin.splitfy.domain.settlement.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

  // 정산 ID (FK)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_id", nullable = false)
  private Settlement settlement;

  @Column(name = "paid_amount", nullable = false)
  private Long paidAmount;

  @Column(name = "payer_id", nullable = false)
  private Long payerId;

  @Column(name = "share_amount", nullable = false)
  private Long shareAmount;

  @Column(nullable = false)
  private String title;

  public Payment(Long paidAmount, Long payerId, Long shareAmount, String title) {
    this.paidAmount = paidAmount;
    this.payerId = payerId;
    this.shareAmount = shareAmount;
    this.title = title;
  }

  public void setSettlement(Settlement settlement) {
    this.settlement = settlement;
  }
}
