export interface BaseResponse<T> {
  requestId: string;
  status: boolean;
  message: string;
  data: T;
}