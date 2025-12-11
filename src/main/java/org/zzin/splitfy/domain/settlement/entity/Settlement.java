package org.zzin.splitfy.domain.settlement.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  // Payment(1:N)
  @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL)
  private List<Payment> payments = new ArrayList<>();

  // Participant(1:N)
  @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SettlementParticipant> participants = new ArrayList<>();

  public Settlement(Long issuerId, long totalAmount) {
    this.issuerId = issuerId;
    this.totalAmount = totalAmount;
    this.status = SettlementStatus.PENDING;
    this.issuedAt = LocalDateTime.now();
  }

  public void succeed() {
    this.status = SettlementStatus.SUCCEEDED;
    this.succeededAt = LocalDateTime.now();
  }

  public void fail() {
    this.status = SettlementStatus.FAILED;
  }

  // 양방향 연결
  public void addPayment(Payment payment) {
    this.payments.add(payment);
    payment.setSettlement(this);
  }
}
