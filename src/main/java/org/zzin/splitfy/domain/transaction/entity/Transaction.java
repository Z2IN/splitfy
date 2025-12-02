package org.zzin.splitfy.domain.transaction.entity;

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

import org.springframework.data.annotation.CreatedDate;
import org.zzin.splitfy.domain.transaction.enums.TransactionType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "transactions")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionType type;

  @Column(nullable = false)
  private Long beforePoint;

  @Column(nullable = false)
  private Long afterPoint;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime transactionTime;

  @Column(nullable = false, unique = true)
  private String uuid;

  @Builder
  public Transaction(long userId, long amount, TransactionType type, long beforePoint,
      long afterPoint, String uuid) {
    this.userId = userId;
    this.amount = amount;
    this.type = type;
    this.beforePoint = beforePoint;
    this.afterPoint = afterPoint;
    this.uuid = uuid;
  }
}
