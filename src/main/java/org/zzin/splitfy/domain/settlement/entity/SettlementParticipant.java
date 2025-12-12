package org.zzin.splitfy.domain.settlement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlement_participants")
public class SettlementParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 정산 ID (FK)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_id", nullable = false)
  private Settlement settlement;

  // 사용자 ID
  @Column(nullable = false)
  private Long participantId;

  @Column
  private Long settlementAmount;

  public SettlementParticipant(Settlement settlement, Long participantId, Long settlementAmount) {
    this.settlement = settlement;
    this.participantId = participantId;
    this.settlementAmount = settlementAmount;
  }
}
