import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { ItemSelectorComponent } from './item-selector.component';
import { ApiService } from '../../services/api.service';
import { ItemDTO, CategoryDTO, PagedResponse } from '../../models';

describe('ItemSelectorComponent', () => {
  let component: ItemSelectorComponent;
  let fixture: ComponentFixture<ItemSelectorComponent>;
  let apiService: jasmine.SpyObj<ApiService>;

  const mockCategories: CategoryDTO[] = [
    { id: 1, dofusId: 10, name: 'Weapons' },
    { id: 2, dofusId: 20, name: 'Armor' }
  ];

  const mockItems: ItemDTO[] = [
    {
      id: 1,
      itemGid: 100,
      itemName: 'Test Sword',
      category: mockCategories[0],
      createdAt: '2025-01-01T00:00:00',
      updatedAt: null
    }
  ];

  const mockPagedResponse: PagedResponse<ItemDTO> = {
    content: mockItems,
    page: 0,
    size: 10,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
    hasNext: false,
    hasPrevious: false
  };

  beforeEach(async () => {
    const apiServiceSpy = jasmine.createSpyObj('ApiService', ['getCategories', 'getItems']);

    await TestBed.configureTestingModule({
      imports: [
        ItemSelectorComponent,
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: ApiService, useValue: apiServiceSpy }
      ]
    }).compileComponents();

    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    fixture = TestBed.createComponent(ItemSelectorComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    apiService.getCategories.and.returnValue(of([]));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load categories on init', () => {
    apiService.getCategories.and.returnValue(of(mockCategories));
    fixture.detectChanges();

    expect(apiService.getCategories).toHaveBeenCalled();
    expect(component.categories().length).toBe(2);
  });

  it('should handle category loading error gracefully', () => {
    apiService.getCategories.and.returnValue(throwError(() => new Error('Error')));
    fixture.detectChanges();

    expect(component.categories().length).toBe(0);
  });

  it('should emit itemSelected when an item is selected', (done) => {
    apiService.getCategories.and.returnValue(of([]));
    fixture.detectChanges();

    component.itemSelected.subscribe((item: ItemDTO) => {
      expect(item).toEqual(mockItems[0]);
      done();
    });

    component.onItemSelect(mockItems[0]);
  });

  it('should display item name correctly', () => {
    expect(component.displayFn(mockItems[0])).toBe('Test Sword');
    expect(component.displayFn(null)).toBe('');
  });
});
