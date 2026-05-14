package com.berijalan.ewallet.service;

import com.berijalan.ewallet.entity.MerchantTax;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class TaxCalculator {

    public TaxCalculationResult calculate(long baseAmount, List<MerchantTax> taxes) {
        long totalTax = 0L;
        List<TaxSnapshot> snapshots = new ArrayList<>();

        for (MerchantTax tax : taxes) {
            long calculatedTax = calculateTaxAmount(baseAmount, tax);
            totalTax += calculatedTax;
            snapshots.add(new TaxSnapshot(
                    tax.getTaxName(),
                    tax.getTaxType().name(),
                    tax.getValueType().name(),
                    tax.getTaxValue(),
                    calculatedTax
            ));
        }

        return new TaxCalculationResult(totalTax, snapshots);
    }

    private long calculateTaxAmount(long baseAmount, MerchantTax tax) {
        if (tax.getValueType() == TaxValueType.PERCENTAGE) {
            return BigDecimal.valueOf(baseAmount)
                    .multiply(tax.getTaxValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
        }
        return tax.getTaxValue().setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public record TaxCalculationResult(
            long totalTax,
            List<TaxSnapshot> snapshots
    ) {
    }
}
