package org.zzin.splitfy.domain.settlement.mapper;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementHistoryResponse;
import org.zzin.splitfy.domain.settlement.dto.response.SettlementPaymentResponse;
import org.zzin.splitfy.domain.settlement.entity.Payment;
import org.zzin.splitfy.domain.settlement.entity.Settlement;

@Component
public class SettlementHistoryMapper {

  public SettlementHistoryResponse toResponse(
      Settlement settlement,  // 정산 1건
      List<Payment> payments, // 정산에 속한 결제들
      Map<Long, String> userNameMap,  // userId -> username
      Map<Long, List<String>> allocationNameMap // paymentId -> List<allocationName>
  ) {

    return new SettlementHistoryResponse(
        settlement.getId(),
        settlement.getTotalAmount(),
        settlement.getStatus(),
        settlement.getIssuedAt(),
        settlement.getSucceededAt(),
        settlement.getRemainder(),
        payments.stream()
            .map(p -> toPaymentResponse(p, userNameMap, allocationNameMap))
            .toList()
    );
  }

  private SettlementPaymentResponse toPaymentResponse(
      Payment payment,
      Map<Long, String> userNameMap,
      Map<Long, List<String>> allocationNameMap
  ) {
    return new SettlementPaymentResponse(
        payment.getTitle(),
        payment.getPaidAmount(),
        userNameMap.get(payment.getPayerId()),
        allocationNameMap.getOrDefault(payment.getId(), List.of())
    );
  }
}
