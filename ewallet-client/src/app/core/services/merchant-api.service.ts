import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseResponse } from '../models/auth.model';
import { MerchantDto } from '../models/merchant.model';

@Injectable({
  providedIn: 'root',
})
export class MerchantApiService {
  private readonly apiUrl = 'http://localhost:8080/api/merchants';

  constructor(private http: HttpClient) {}

  getAllMerchants(): Observable<BaseResponse<MerchantDto[]>> {
    return this.http.get<BaseResponse<MerchantDto[]>>(this.apiUrl);
  }
}
