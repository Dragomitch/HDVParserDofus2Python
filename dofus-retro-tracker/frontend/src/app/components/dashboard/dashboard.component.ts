import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { ItemSelectorComponent } from '../item-selector/item-selector.component';
import { PriceChartComponent } from '../price-chart/price-chart.component';
import { ItemDTO } from '../../models';

/**
 * Main dashboard component with responsive layout
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    ItemSelectorComponent,
    PriceChartComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  selectedItem = signal<ItemDTO | null>(null);
  currentYear = new Date().getFullYear();

  /**
   * Handle item selection from the item selector
   */
  onItemSelected(item: ItemDTO): void {
    this.selectedItem.set(item);
  }

  /**
   * Clear selected item
   */
  clearSelection(): void {
    this.selectedItem.set(null);
  }
}
