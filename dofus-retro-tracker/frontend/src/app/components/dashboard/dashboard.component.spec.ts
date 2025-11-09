import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DashboardComponent } from './dashboard.component';
import { ItemDTO } from '../../models';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  const mockItem: ItemDTO = {
    id: 1,
    itemGid: 100,
    itemName: 'Test Item',
    category: { id: 1, dofusId: 10, name: 'Weapons' },
    createdAt: '2025-01-01T00:00:00',
    updatedAt: null
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        DashboardComponent,
        HttpClientTestingModule,
        BrowserAnimationsModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start with no selected item', () => {
    expect(component.selectedItem()).toBeNull();
  });

  it('should update selected item when onItemSelected is called', () => {
    component.onItemSelected(mockItem);
    expect(component.selectedItem()).toEqual(mockItem);
  });

  it('should clear selected item when clearSelection is called', () => {
    component.selectedItem.set(mockItem);
    component.clearSelection();
    expect(component.selectedItem()).toBeNull();
  });

  it('should display current year in footer', () => {
    expect(component.currentYear).toBe(new Date().getFullYear());
  });
});
