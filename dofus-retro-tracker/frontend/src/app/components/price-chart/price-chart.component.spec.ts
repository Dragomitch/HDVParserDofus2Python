import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { PriceChartComponent } from './price-chart.component';
import { ApiService } from '../../services/api.service';
import { ItemDTO, PriceEntryDTO } from '../../models';

describe('PriceChartComponent', () => {
  let component: PriceChartComponent;
  let fixture: ComponentFixture<PriceChartComponent>;
  let apiService: jasmine.SpyObj<ApiService>;

  const mockItem: ItemDTO = {
    id: 1,
    itemGid: 100,
    itemName: 'Test Item',
    category: { id: 1, dofusId: 10, name: 'Weapons' },
    createdAt: '2025-01-01T00:00:00',
    updatedAt: null
  };

  const mockPrices: PriceEntryDTO[] = [
    {
      id: 1,
      price: 1000,
      quantity: 5,
      createdAt: '2025-01-01T00:00:00'
    },
    {
      id: 2,
      price: 1200,
      quantity: 3,
      createdAt: '2025-01-02T00:00:00'
    }
  ];

  beforeEach(async () => {
    const apiServiceSpy = jasmine.createSpyObj('ApiService', ['getItemPrices']);

    await TestBed.configureTestingModule({
      imports: [
        PriceChartComponent,
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: ApiService, useValue: apiServiceSpy }
      ]
    }).compileComponents();

    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    fixture = TestBed.createComponent(PriceChartComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load price data when item changes', () => {
    apiService.getItemPrices.and.returnValue(of(mockPrices));
    component.item = mockItem;
    component.ngOnChanges({
      item: {
        currentValue: mockItem,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    });

    expect(apiService.getItemPrices).toHaveBeenCalled();
    expect(component.priceData().length).toBe(2);
  });

  it('should handle error when loading price data', () => {
    apiService.getItemPrices.and.returnValue(throwError(() => new Error('Error')));
    component.item = mockItem;
    component.loadPriceData();

    expect(component.hasError()).toBeTrue();
    expect(component.isLoading()).toBeFalse();
  });

  it('should calculate statistics correctly', () => {
    component.priceData.set(mockPrices);
    const stats = component.getStats();

    expect(stats).not.toBeNull();
    expect(stats!.min).toBe(1000);
    expect(stats!.max).toBe(1200);
    expect(stats!.avg).toBe(1100);
    expect(stats!.count).toBe(2);
  });

  it('should return null stats when no data', () => {
    component.priceData.set([]);
    const stats = component.getStats();

    expect(stats).toBeNull();
  });
});
