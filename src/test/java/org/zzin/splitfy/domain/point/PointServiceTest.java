package org.zzin.splitfy.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.domain.auth.dto.PointChangeResultDTO;
import org.zzin.splitfy.domain.auth.service.AuthInnerService;
import org.zzin.splitfy.domain.point.Service.PointService;
import org.zzin.splitfy.domain.auth.dto.PointTransferSummaryDTO;
import org.zzin.splitfy.domain.point.dto.response.DepositResponse;
import org.zzin.splitfy.domain.point.dto.response.TransferResponse;
import org.zzin.splitfy.domain.transaction.dto.TransactionInfoDTO;
import org.zzin.splitfy.domain.transaction.service.TransactionInnerService;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

  @Mock
  private AuthInnerService authInnerService;

  @Mock
  private TransactionInnerService transactionInnerService;

  @InjectMocks
  private PointService pointService;

  @Test
  void deposit_제대로된_데이터가_주어지면_입금_성공() {
    // 테스트 데이터
    long userId = 42L;
    long amount = 50L;
    long beforePoint = 100L;
    long afterPoint = 150L;

    given(authInnerService.addPoint(userId, amount))
        .willReturn(new PointChangeResultDTO(beforePoint, afterPoint));

    DepositResponse response = pointService.deposit(userId, amount);

    assertThat(response).isNotNull();
    assertThat(response.amount()).isEqualTo(amount);
    assertThat(response.point()).isEqualTo(afterPoint);

    then(authInnerService).should().addPoint(userId, amount);

    ArgumentCaptor<TransactionInfoDTO> captor = ArgumentCaptor.forClass(
        TransactionInfoDTO.class);
    then(transactionInnerService).should().createDepositTransaction(captor.capture());

    TransactionInfoDTO dto = captor.getValue();
    assertThat(dto).isNotNull();
    assertThat(dto.getUserId()).isEqualTo(userId);
    assertThat(dto.getAmount()).isEqualTo(amount);
    assertThat(dto.getBeforePoint()).isEqualTo(beforePoint);
    assertThat(dto.getAfterPoint()).isEqualTo(afterPoint);
    assertThat(dto.getTransactionUUID()).isNotBlank();
  }

  @Test
  void transferTo_정상적으로_송금되면_성공() {
    long meId = 1L;
    long toUserId = 2L;
    long amount = 50L;

    long myBefore = 100L;
    long myAfter = 50L;
    long otherBefore = 0L;
    long otherAfter = 50L;

    given(authInnerService.transferPoint(meId, toUserId, amount)).willReturn(
        PointTransferSummaryDTO.builder()
            .senderBeforePoint(myBefore)
            .senderAfterPoint(myAfter)
            .receiverBeforePoint(otherBefore)
            .receiverAfterPoint(otherAfter)
            .build());

    TransferResponse response = pointService.transferTo(toUserId, amount, meId);

    assertThat(response).isNotNull();
    assertThat(response.amount()).isEqualTo(amount);
    assertThat(response.beforePoint()).isEqualTo(myBefore);
    assertThat(response.afterPoint()).isEqualTo(myAfter);

    then(authInnerService).should().transferPoint(meId, toUserId, amount);

    ArgumentCaptor<TransactionInfoDTO> captor = ArgumentCaptor
        .forClass(TransactionInfoDTO.class);
    then(transactionInnerService).should().createTransferOutTransaction(captor.capture());
    TransactionInfoDTO out = captor.getValue();
    assertThat(out.getUserId()).isEqualTo(meId);
    assertThat(out.getAmount()).isEqualTo(amount);
    assertThat(out.getBeforePoint()).isEqualTo(myBefore);
    assertThat(out.getAfterPoint()).isEqualTo(myAfter);
    assertThat(out.getTransactionUUID()).isNotBlank();

    ArgumentCaptor<TransactionInfoDTO> captorIn = ArgumentCaptor
        .forClass(TransactionInfoDTO.class);
    then(transactionInnerService).should().createTransferInTransaction(captorIn.capture());
    TransactionInfoDTO in = captorIn.getValue();
    assertThat(in.getUserId()).isEqualTo(toUserId);
    assertThat(in.getAmount()).isEqualTo(amount);
    assertThat(in.getBeforePoint()).isEqualTo(otherBefore);
    assertThat(in.getAfterPoint()).isEqualTo(otherAfter);
    assertThat(in.getTransactionUUID()).isNotBlank();
    assertThat(out.getTransactionUUID()).isEqualTo(in.getTransactionUUID());
  }
}
