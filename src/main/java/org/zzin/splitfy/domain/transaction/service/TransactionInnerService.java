package org.zzin.splitfy.domain.transaction.service;

import org.zzin.splitfy.domain.transaction.dto.TransactionInfoDTO;

public interface TransactionInnerService {

  void createDepositTransaction(TransactionInfoDTO param);

  void createTransferInTransaction(TransactionInfoDTO param);

  void createTransferOutTransaction(TransactionInfoDTO param);

}
