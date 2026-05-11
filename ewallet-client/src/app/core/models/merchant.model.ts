export interface MerchantTaxDto {
  taxName: string;
  taxType: string;
  valueType: 'FIXED' | 'PERCENTAGE';
  taxValue: number;
}

export interface MerchantDto {
  id: number;
  name: string;
  isActive: boolean;
  taxes: MerchantTaxDto[];
}
