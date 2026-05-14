package com.berijalan.ewallet.mapper;

import com.berijalan.ewallet.dto.response.ResPaymentDto;
import com.berijalan.ewallet.dto.response.ResTransactionHistoryDto;
import com.berijalan.ewallet.dto.response.ResTransactionItemDto;
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
        return new ResPaymentDto(
                transaction.getId().longValue(),
                transaction.getReferenceId(),
                transaction.getAmount(),
                transaction.getBaseAmount(),
                transaction.getTaxAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getMerchant().getName(),
                transaction.getType().name(),
                transaction.getStatus().name()
        );
    }

    public ResTransactionHistoryDto toHistoryDto(Page<Transaction> transactionPage) {
        List<ResTransactionItemDto> items = transactionPage.getContent().stream()
                .map(this::toHistoryItemDto)
                .toList();

        return new ResTransactionHistoryDto(
                items,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
    }

    private ResTransactionItemDto toHistoryItemDto(Transaction transaction) {
        return new ResTransactionItemDto(
                transaction.getId().longValue(),
                transaction.getUser().getId().longValue(),
                transaction.getUser().getName(),
                transaction.getUser().getEmail(),
                transaction.getReferenceId(),
                transaction.getAmount(),
                transaction.getBaseAmount(),
                transaction.getTaxAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getType().name(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getMerchant() != null ? transaction.getMerchant().getName() : null,
                transaction.getCreatedAt()
        );
    }
}
