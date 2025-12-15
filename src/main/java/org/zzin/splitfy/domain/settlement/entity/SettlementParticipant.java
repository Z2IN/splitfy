package org.zzin.splitfy.domain.settlement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

  @Column(name = "settlement_id", nullable = false)
  private long settlementId;

  @Column(nullable = false)
  private long participantId;

  @Column
  private long settlementAmount;

  public SettlementParticipant(Settlement settlement, Long participantId, Long settlementAmount) {
    this.settlementId = settlementId;
    this.participantId = participantId;
    this.settlementAmount = settlementAmount;
  }
}
