import { Component, Output, EventEmitter, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { debounceTime, distinctUntilChanged, switchMap, catchError, of } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { ItemDTO, CategoryDTO } from '../../models';

/**
 * Item selector component with autocomplete search and category filtering
 */
@Component({
  selector: 'app-item-selector',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './item-selector.component.html',
  styleUrl: './item-selector.component.scss'
})
export class ItemSelectorComponent implements OnInit {
  @Output() itemSelected = new EventEmitter<ItemDTO>();

  searchControl = new FormControl('');
  categoryControl = new FormControl<number | null>(null);

  items = signal<ItemDTO[]>([]);
  categories = signal<CategoryDTO[]>([]);
  isLoading = signal(false);

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadCategories();
    this.setupSearch();
  }

  /**
   * Load all categories for filtering
   */
  private loadCategories(): void {
    this.apiService.getCategories()
      .pipe(
        catchError(error => {
          console.error('Error loading categories:', error);
          return of([]);
        })
      )
      .subscribe(categories => {
        this.categories.set(categories);
      });
  }

  /**
   * Setup autocomplete search with debouncing
   */
  private setupSearch(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(searchTerm => {
          if (!searchTerm || searchTerm.length < 2) {
            this.items.set([]);
            return of(null);
          }

          this.isLoading.set(true);
          const categoryId = this.categoryControl.value ?? undefined;

          return this.apiService.getItems(0, 10, searchTerm, categoryId)
            .pipe(
              catchError(error => {
                console.error('Error searching items:', error);
                this.isLoading.set(false);
                return of(null);
              })
            );
        })
      )
      .subscribe(response => {
        this.isLoading.set(false);
        if (response) {
          this.items.set(response.content);
        }
      });

    // Re-trigger search when category changes
    this.categoryControl.valueChanges.subscribe(() => {
      const currentSearch = this.searchControl.value;
      if (currentSearch && currentSearch.length >= 2) {
        this.searchControl.setValue(currentSearch);
      }
    });
  }

  /**
   * Handle item selection
   */
  onItemSelect(item: ItemDTO): void {
    this.itemSelected.emit(item);
  }

  /**
   * Display function for autocomplete
   */
  displayFn(item: ItemDTO | null): string {
    return item ? item.itemName : '';
  }
}
