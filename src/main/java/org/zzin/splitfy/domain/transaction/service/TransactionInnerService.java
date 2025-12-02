package org.zzin.splitfy.domain.transaction.service;

import org.zzin.splitfy.domain.transaction.dto.CreateDepositTransactionParamDTO;

public interface TransactionInnerService {

  void createDepositTransaction(CreateDepositTransactionParamDTO param);

}
