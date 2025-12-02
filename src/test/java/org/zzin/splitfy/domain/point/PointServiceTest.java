package org.zzin.splitfy.domain.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zzin.splitfy.domain.auth.dto.UserPointChangeDetailDTO;
import org.zzin.splitfy.domain.auth.service.AuthInnerService;
import org.zzin.splitfy.domain.point.Service.PointService;
import org.zzin.splitfy.domain.point.dto.response.DepositResponse;
import org.zzin.splitfy.domain.point.mapper.PointMapper;
import org.zzin.splitfy.domain.transaction.dto.CreateDepositTransactionParamDTO;
import org.zzin.splitfy.domain.transaction.service.TransactionInnerService;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

  @Mock
  private AuthInnerService authInnerService;

  @Mock
  private TransactionInnerService transactionInnerService;

  @Spy
  private PointMapper pointMapper;

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
        .willReturn(new UserPointChangeDetailDTO(beforePoint, afterPoint));

    DepositResponse response = pointService.deposit(userId, amount);

    assertThat(response).isNotNull();
    assertThat(response.amount()).isEqualTo(amount);
    assertThat(response.point()).isEqualTo(afterPoint);

    then(authInnerService).should().addPoint(userId, amount);

    ArgumentCaptor<CreateDepositTransactionParamDTO> captor = ArgumentCaptor.forClass(
        CreateDepositTransactionParamDTO.class);
    then(transactionInnerService).should().createDepositTransaction(captor.capture());

    CreateDepositTransactionParamDTO dto = captor.getValue();
    assertThat(dto).isNotNull();
    assertThat(dto.getUserId()).isEqualTo(userId);
    assertThat(dto.getAmount()).isEqualTo(amount);
    assertThat(dto.getBeforePoint()).isEqualTo(beforePoint);
    assertThat(dto.getAfterPoint()).isEqualTo(afterPoint);
    assertThat(dto.getTransactionUUID()).isNotBlank();
  }

}
