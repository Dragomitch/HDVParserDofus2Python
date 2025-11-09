import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { environment } from '../../environments/environment';
import { ItemDTO, CategoryDTO, PriceEntryDTO, PagedResponse } from '../models';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiBaseUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getItems', () => {
    it('should fetch items with pagination', () => {
      const mockResponse: PagedResponse<ItemDTO> = {
        content: [
          {
            id: 1,
            itemGid: 100,
            itemName: 'Test Item',
            category: null,
            createdAt: '2025-01-01T00:00:00',
            updatedAt: null
          }
        ],
        page: 0,
        size: 10,
        totalElements: 1,
        totalPages: 1,
        first: true,
        last: true,
        hasNext: false,
        hasPrevious: false
      };

      service.getItems(0, 10).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(response.content.length).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/items?page=0&size=10`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should include search parameter when provided', () => {
      const mockResponse: PagedResponse<ItemDTO> = {
        content: [],
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
        hasNext: false,
        hasPrevious: false
      };

      service.getItems(0, 10, 'sword').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/items?page=0&size=10&search=sword`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getItem', () => {
    it('should fetch a single item', () => {
      const mockItem: ItemDTO = {
        id: 1,
        itemGid: 100,
        itemName: 'Test Item',
        category: null,
        createdAt: '2025-01-01T00:00:00',
        updatedAt: null
      };

      service.getItem(1).subscribe(item => {
        expect(item).toEqual(mockItem);
      });

      const req = httpMock.expectOne(`${apiUrl}/items/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockItem);
    });
  });

  describe('getItemPrices', () => {
    it('should fetch price history for an item', () => {
      const mockPrices: PriceEntryDTO[] = [
        {
          id: 1,
          price: 1000,
          quantity: 5,
          createdAt: '2025-01-01T00:00:00'
        }
      ];

      service.getItemPrices(1).subscribe(prices => {
        expect(prices).toEqual(mockPrices);
      });

      const req = httpMock.expectOne(`${apiUrl}/items/1/prices`);
      expect(req.request.method).toBe('GET');
      req.flush(mockPrices);
    });

    it('should include date range parameters when provided', () => {
      const mockPrices: PriceEntryDTO[] = [];

      service.getItemPrices(1, '2025-01-01', '2025-11-09').subscribe();

      const req = httpMock.expectOne(
        `${apiUrl}/items/1/prices?startDate=2025-01-01&endDate=2025-11-09`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockPrices);
    });
  });

  describe('getCategories', () => {
    it('should fetch all categories', () => {
      const mockCategories: CategoryDTO[] = [
        { id: 1, dofusId: 10, name: 'Weapons' }
      ];

      service.getCategories().subscribe(categories => {
        expect(categories).toEqual(mockCategories);
      });

      const req = httpMock.expectOne(`${apiUrl}/categories`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCategories);
    });
  });

  describe('healthCheck', () => {
    it('should perform health check', () => {
      const mockResponse = { status: 'UP' };

      service.healthCheck().subscribe(response => {
        expect(response.status).toBe('UP');
      });

      const req = httpMock.expectOne(`${apiUrl}/health`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });
});
