export type AdminTaxType =
  | 'PPN'
  | 'SERVICE_FEE'
  | 'PLATFORM_FEE'
  | 'ADMIN_FEE'
  | 'TRANSFER_FEE'
  | 'PROCESSING_FEE';

export type AdminTaxValueType = 'PERCENTAGE' | 'FIXED';

export interface AdminMerchantTaxDto {
  id: number;
  taxName: string;
  taxType: AdminTaxType;
  valueType: AdminTaxValueType;
  taxValue: number;
  isActive: boolean;
  effectiveAt: string;
  expiredAt: string | null;
}

export interface AdminMerchantDto {
  id: number;
  name: string;
  isActive: boolean;
  taxes: AdminMerchantTaxDto[];
}

export interface AdminMerchantTaxPayload {
  id?: number | null;
  taxName: string;
  taxType: AdminTaxType;
  valueType: AdminTaxValueType;
  taxValue: number;
  isActive: boolean;
  effectiveAt: string;
  expiredAt: string | null;
}

export interface AdminMerchantConfigPayload {
  name: string;
  isActive: boolean;
  taxes: AdminMerchantTaxPayload[];
}
