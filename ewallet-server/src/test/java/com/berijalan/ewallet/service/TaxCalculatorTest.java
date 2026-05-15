package com.berijalan.ewallet.service;

import com.berijalan.ewallet.entity.Merchant;
import com.berijalan.ewallet.entity.MerchantTax;
import com.berijalan.ewallet.entity.constant.TaxType;
import com.berijalan.ewallet.entity.constant.TaxValueType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaxCalculatorTest {

    private final TaxCalculator taxCalculator = new TaxCalculator();

    @Test
    void calculate_shouldReturnInternalSnapshots() {
        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setName("Gopay");

        MerchantTax fixedTax = createTax(merchant, "Admin Fee", TaxType.ADMIN_FEE, TaxValueType.FIXED, "1000.0000");
        MerchantTax percentageTax = createTax(merchant, "Service Fee", TaxType.SERVICE_FEE, TaxValueType.PERCENTAGE, "1.5000");

        TaxCalculator.TaxCalculationResult result = taxCalculator.calculate(100_000L, List.of(fixedTax, percentageTax));

        assertThat(result.totalTax()).isEqualTo(2_500L);
        assertThat(result.snapshots()).hasSize(2);
        assertThat(result.snapshots().get(0)).isInstanceOf(TaxCalculator.TaxSnapshot.class);
        assertThat(result.snapshots().get(0).taxName()).isEqualTo("Admin Fee");
        assertThat(result.snapshots().get(0).calculatedTax()).isEqualTo(1_000L);
        assertThat(result.snapshots().get(1).taxName()).isEqualTo("Service Fee");
        assertThat(result.snapshots().get(1).calculatedTax()).isEqualTo(1_500L);
    }

    @Test
    void calculate_whenNoTaxes_shouldReturnZeroAndEmptySnapshots() {
        TaxCalculator.TaxCalculationResult result = taxCalculator.calculate(100_000L, List.of());

        assertThat(result.totalTax()).isZero();
        assertThat(result.snapshots()).isEmpty();
    }

    @Test
    void calculate_shouldRoundFixedTaxHalfUp() {
        Merchant merchant = new Merchant();
        MerchantTax fixedTax = createTax(merchant, "Admin Fee", TaxType.ADMIN_FEE, TaxValueType.FIXED, "1000.5000");

        TaxCalculator.TaxCalculationResult result = taxCalculator.calculate(100_000L, List.of(fixedTax));

        assertThat(result.totalTax()).isEqualTo(1_001L);
        assertThat(result.snapshots().get(0).calculatedTax()).isEqualTo(1_001L);
    }

    @Test
    void calculate_shouldRoundPercentageTaxHalfUp() {
        Merchant merchant = new Merchant();
        MerchantTax percentageTax = createTax(merchant, "Service Fee", TaxType.SERVICE_FEE, TaxValueType.PERCENTAGE, "1.5000");

        TaxCalculator.TaxCalculationResult result = taxCalculator.calculate(3_333L, List.of(percentageTax));

        assertThat(result.totalTax()).isEqualTo(50L);
        assertThat(result.snapshots().get(0).calculatedTax()).isEqualTo(50L);
    }

    private MerchantTax createTax(
            Merchant merchant,
            String taxName,
            TaxType taxType,
            TaxValueType valueType,
            String taxValue
    ) {
        MerchantTax tax = new MerchantTax();
        tax.setMerchant(merchant);
        tax.setTaxName(taxName);
        tax.setTaxType(taxType);
        tax.setValueType(valueType);
        tax.setTaxValue(new BigDecimal(taxValue));
        tax.setIsActive(true);
        return tax;
    }
}
