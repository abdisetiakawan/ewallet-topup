package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.contract.model.ResPaymentDto;
import com.berijalan.ewallet.contract.model.ResTransactionDetailDto;
import com.berijalan.ewallet.contract.model.ResTransactionHistoryDto;
import com.berijalan.ewallet.contract.model.ResTransactionItemDto;
import com.berijalan.ewallet.contract.model.TaxSnapshotDto;
import com.berijalan.ewallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionMapper implements BaseMapper<Transaction, ResPaymentDto> {

    @Override
    public ResPaymentDto toDto(Transaction transaction) {
        return toPaymentDto(transaction);
    }

    public ResPaymentDto toPaymentDto(Transaction transaction) {
        return new ResPaymentDto()
                .transactionId(transaction.getId().longValue())
                .referenceId(transaction.getReferenceId())
                .amount(transaction.getAmount())
                .baseAmount(transaction.getBaseAmount())
                .taxAmount(transaction.getTaxAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .merchantName(transaction.getMerchant().getName())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name());
    }

    public ResTransactionHistoryDto toHistoryDto(Page<Transaction> transactionPage) {
        List<ResTransactionItemDto> items = transactionPage.getContent().stream()
                .map(this::toHistoryItemDto)
                .toList();

        return new ResTransactionHistoryDto()
                .content(items)
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages());
    }

    public ResTransactionDetailDto toDetailDto(Transaction transaction, List<TaxSnapshotDto> taxDetails) {
        return new ResTransactionDetailDto()
                .transactionId(transaction.getId().longValue())
                .referenceId(transaction.getReferenceId())
                .amount(transaction.getAmount())
                .baseAmount(transaction.getBaseAmount())
                .taxAmount(transaction.getTaxAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .merchantName(transaction.getMerchant() != null ? transaction.getMerchant().getName() : null)
                .taxDetails(taxDetails)
                .createdAt(transaction.getCreatedAt());
    }

    private ResTransactionItemDto toHistoryItemDto(Transaction transaction) {
        return new ResTransactionItemDto()
                .transactionId(transaction.getId().longValue())
                .userId(transaction.getUser().getId().longValue())
                .userName(transaction.getUser().getName())
                .userEmail(transaction.getUser().getEmail())
                .referenceId(transaction.getReferenceId())
                .amount(transaction.getAmount())
                .baseAmount(transaction.getBaseAmount())
                .taxAmount(transaction.getTaxAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .merchantName(transaction.getMerchant() != null ? transaction.getMerchant().getName() : null)
                .createdAt(transaction.getCreatedAt());
    }
}
