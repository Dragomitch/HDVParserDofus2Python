import { Component, Input, OnChanges, SimpleChanges, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, ChartOptions, registerables } from 'chart.js';
import { ApiService } from '../../services/api.service';
import { ItemDTO, PriceEntryDTO } from '../../models';
import { catchError, of } from 'rxjs';

// Register Chart.js components
Chart.register(...registerables);

/**
 * Price chart component displaying time-series price data using Chart.js
 */
@Component({
  selector: 'app-price-chart',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatIconModule,
    BaseChartDirective
  ],
  templateUrl: './price-chart.component.html',
  styleUrl: './price-chart.component.scss'
})
export class PriceChartComponent implements OnChanges {
  @Input() item: ItemDTO | null = null;
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  isLoading = signal(false);
  hasError = signal(false);
  priceData = signal<PriceEntryDTO[]>([]);

  // Chart configuration
  chartData: ChartConfiguration<'line'>['data'] = {
    datasets: []
  };

  chartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      mode: 'index',
      intersect: false
    },
    plugins: {
      title: {
        display: true,
        text: 'Price History',
        font: {
          size: 16,
          weight: 'bold'
        }
      },
      legend: {
        display: true,
        position: 'top'
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            const label = context.dataset.label || '';
            const value = context.parsed.y;
            if (value === null || value === undefined) {
              return label;
            }
            return `${label}: ${value.toLocaleString()} kamas`;
          }
        }
      }
    },
    scales: {
      x: {
        type: 'time',
        time: {
          unit: 'day',
          displayFormats: {
            day: 'MMM dd'
          }
        },
        title: {
          display: true,
          text: 'Date'
        }
      },
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: 'Price (kamas)'
        },
        ticks: {
          callback: (value) => {
            if (typeof value === 'number') {
              return value.toLocaleString();
            }
            return value;
          }
        }
      }
    }
  };

  constructor(private apiService: ApiService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['item'] && this.item) {
      this.loadPriceData();
    }
  }

  /**
   * Load price data for the selected item
   */
  loadPriceData(): void {
    if (!this.item) {
      return;
    }

    this.isLoading.set(true);
    this.hasError.set(false);

    // Get last 30 days of data
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30);

    const startDateStr = startDate.toISOString().split('T')[0];
    const endDateStr = endDate.toISOString().split('T')[0];

    this.apiService.getItemPrices(this.item.id, startDateStr, endDateStr)
      .pipe(
        catchError(error => {
          console.error('Error loading price data:', error);
          this.hasError.set(true);
          this.isLoading.set(false);
          return of([]);
        })
      )
      .subscribe(prices => {
        this.isLoading.set(false);
        this.priceData.set(prices);
        this.updateChart(prices);
      });
  }

  /**
   * Update chart with new data
   */
  private updateChart(prices: PriceEntryDTO[]): void {
    if (prices.length === 0) {
      this.chartData = {
        datasets: []
      };
      this.chart?.update();
      return;
    }

    // Sort by date
    const sortedPrices = [...prices].sort((a, b) =>
      new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );

    // Prepare data for chart
    const labels = sortedPrices.map(p => new Date(p.createdAt));
    const priceValues = sortedPrices.map(p => p.price);
    const quantities = sortedPrices.map(p => p.quantity);

    this.chartData = {
      labels,
      datasets: [
        {
          label: 'Price',
          data: priceValues,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.2)',
          tension: 0.1,
          pointRadius: 3,
          pointHoverRadius: 5
        },
        {
          label: 'Quantity',
          data: quantities,
          borderColor: 'rgb(255, 99, 132)',
          backgroundColor: 'rgba(255, 99, 132, 0.2)',
          tension: 0.1,
          pointRadius: 3,
          pointHoverRadius: 5,
          yAxisID: 'y1'
        }
      ]
    };

    // Add second Y axis for quantity
    if (this.chartOptions.scales) {
      this.chartOptions.scales['y1'] = {
        type: 'linear',
        position: 'right',
        title: {
          display: true,
          text: 'Quantity'
        },
        grid: {
          drawOnChartArea: false
        }
      };
    }

    this.chart?.update();
  }

  /**
   * Retry loading data
   */
  retry(): void {
    this.loadPriceData();
  }

  /**
   * Get statistics from price data
   */
  getStats(): { min: number; max: number; avg: number; count: number } | null {
    const prices = this.priceData();
    if (prices.length === 0) {
      return null;
    }

    const priceValues = prices.map(p => p.price);
    return {
      min: Math.min(...priceValues),
      max: Math.max(...priceValues),
      avg: Math.round(priceValues.reduce((a, b) => a + b, 0) / priceValues.length),
      count: prices.length
    };
  }
}
