import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseResponse } from '../models/auth.model';
import { MerchantDto } from '../models/merchant.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class MerchantApiService {
  private readonly apiUrl = `${environment.apiUrl}/api/merchants`;

  constructor(private http: HttpClient) {}

  getAllMerchants(): Observable<BaseResponse<MerchantDto[]>> {
    return this.http.get<BaseResponse<MerchantDto[]>>(this.apiUrl);
  }
}
