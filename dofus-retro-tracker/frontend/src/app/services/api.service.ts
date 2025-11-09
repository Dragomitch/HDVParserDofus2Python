import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ItemDTO, CategoryDTO, PriceEntryDTO, PagedResponse } from '../models';

/**
 * API service for communicating with the backend REST API
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly apiUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  /**
   * Get paginated list of items with optional filters
   */
  getItems(
    page: number = 0,
    size: number = 10,
    search?: string,
    categoryId?: number
  ): Observable<PagedResponse<ItemDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) {
      params = params.set('search', search);
    }

    if (categoryId !== undefined) {
      params = params.set('categoryId', categoryId.toString());
    }

    return this.http.get<PagedResponse<ItemDTO>>(`${this.apiUrl}/items`, { params });
  }

  /**
   * Get a single item by ID
   */
  getItem(id: number): Observable<ItemDTO> {
    return this.http.get<ItemDTO>(`${this.apiUrl}/items/${id}`);
  }

  /**
   * Get price history for an item
   */
  getItemPrices(
    itemId: number,
    startDate?: string,
    endDate?: string
  ): Observable<PriceEntryDTO[]> {
    let params = new HttpParams();

    if (startDate) {
      params = params.set('startDate', startDate);
    }

    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get<PriceEntryDTO[]>(
      `${this.apiUrl}/items/${itemId}/prices`,
      { params }
    );
  }

  /**
   * Get all categories
   */
  getCategories(): Observable<CategoryDTO[]> {
    return this.http.get<CategoryDTO[]>(`${this.apiUrl}/categories`);
  }

  /**
   * Get a single category by ID
   */
  getCategory(id: number): Observable<CategoryDTO> {
    return this.http.get<CategoryDTO>(`${this.apiUrl}/categories/${id}`);
  }

  /**
   * Get items in a category
   */
  getCategoryItems(categoryId: number): Observable<ItemDTO[]> {
    return this.http.get<ItemDTO[]>(`${this.apiUrl}/categories/${categoryId}/items`);
  }

  /**
   * Health check endpoint
   */
  healthCheck(): Observable<{ status: string }> {
    return this.http.get<{ status: string }>(`${this.apiUrl}/health`);
  }
}
