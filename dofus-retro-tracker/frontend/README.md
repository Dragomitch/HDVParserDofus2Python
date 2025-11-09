# Dofus Retro Price Tracker - Frontend

Angular 20 frontend application for the Dofus Retro Price Tracker project. This application provides a modern, responsive interface for tracking and visualizing Dofus Retro auction house prices.

## Technology Stack

### Core Framework
- **Angular 20.3.9** - Latest Angular with standalone components
- **TypeScript 5.x** - Strict mode enabled
- **RxJS 7.x** - Reactive programming
- **Angular Signals** - Modern state management

### UI Framework
- **Angular Material 20** - Material Design components
- **Material Icons** - Icon library
- **SCSS** - Styling with CSS preprocessor

### Data Visualization
- **Chart.js 4.x** - Interactive charts
- **ng2-charts** - Angular wrapper for Chart.js

### Build & Development
- **Angular CLI 20** - Build and development tools
- **ESBuild** - Fast build compilation
- **Karma + Jasmine** - Unit testing

## Features

### 1. Item Search & Selection
- Autocomplete search with debouncing (300ms)
- Category filtering
- Real-time search as you type
- Minimum 2 characters required
- Loading indicators

### 2. Price Visualization
- Interactive time-series chart
- Dual Y-axis (price and quantity)
- Last 30 days of data
- Price statistics (min, max, average)
- Zoom and pan capabilities
- Responsive chart sizing

### 3. Responsive Design
- Mobile-first approach
- Breakpoints for mobile, tablet, desktop
- Touch-friendly controls
- Dark mode support (via system preferences)
- Accessibility features (WCAG 2.1 AA)

### 4. Error Handling
- HTTP error interceptor with retry logic
- Material Snackbar notifications
- Exponential backoff for retries
- User-friendly error messages

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── item-selector/          # Autocomplete search component
│   │   │   ├── price-chart/            # Chart.js visualization component
│   │   │   └── dashboard/              # Main layout component
│   │   ├── services/
│   │   │   └── api.service.ts          # HTTP API service
│   │   ├── models/
│   │   │   ├── item.model.ts           # Item DTO interface
│   │   │   ├── price-entry.model.ts    # Price entry DTO interface
│   │   │   ├── category.model.ts       # Category DTO interface
│   │   │   └── paged-response.model.ts # Pagination wrapper interface
│   │   ├── interceptors/
│   │   │   └── http-error.interceptor.ts # Global error handling
│   │   ├── app.ts                       # Root component
│   │   ├── app.config.ts                # Application configuration
│   │   └── app.routes.ts                # Route definitions
│   ├── environments/
│   │   ├── environment.ts               # Development config
│   │   └── environment.prod.ts          # Production config
│   ├── styles.scss                      # Global styles
│   └── index.html                       # HTML entry point
├── angular.json                         # Angular CLI configuration
├── package.json                         # NPM dependencies
├── tsconfig.json                        # TypeScript configuration
└── README.md                            # This file
```

## Getting Started

### Prerequisites

- Node.js 18+ or 22+ (LTS recommended)
- npm 10.x or later
- Backend API running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install
```

### Development Server

```bash
# Start development server
npm start

# Or with explicit command
ng serve
```

Navigate to `http://localhost:4200/`. The application will automatically reload if you change any source files.

### Building

```bash
# Development build
npm run build

# Production build
ng build --configuration=production
```

The build artifacts will be stored in the `dist/frontend/` directory.

### Testing

```bash
# Run unit tests
npm test

# Run tests with coverage
npm run test -- --code-coverage

# Run tests in watch mode
npm run test -- --watch
```

### Linting

```bash
# Lint TypeScript files
ng lint
```

## API Integration

The frontend connects to the Spring Boot backend at the following base URL:

- **Development**: `http://localhost:8080/api/v1`
- **Production**: `/api/v1` (relative path)

### API Endpoints Used

```typescript
// Items
GET /api/v1/items?page=0&size=10&search=sword&categoryId=1
GET /api/v1/items/{id}
GET /api/v1/items/{id}/prices?startDate=2025-01-01&endDate=2025-11-09

// Categories
GET /api/v1/categories

// Health Check
GET /api/v1/health
```

### CORS Configuration

The backend is configured to accept requests from:
- `http://localhost:4200` (Angular dev server)
- `http://localhost:4201` (Alternate port)

## Component Documentation

### ItemSelectorComponent

**Purpose**: Provides autocomplete search for Dofus items with category filtering.

**Features**:
- Debounced search (300ms)
- Minimum 2 characters
- Category filter dropdown
- Loading spinner
- Empty state handling

**Inputs**: None

**Outputs**:
- `itemSelected: EventEmitter<ItemDTO>` - Emits when item is selected

**Usage**:
```html
<app-item-selector (itemSelected)="onItemSelected($event)"></app-item-selector>
```

### PriceChartComponent

**Purpose**: Displays price history using Chart.js with statistics.

**Features**:
- Time-series line chart
- Dual Y-axis (price & quantity)
- Price statistics display
- Auto-refresh on item change
- Error handling with retry

**Inputs**:
- `item: ItemDTO | null` - Item to display prices for

**Outputs**: None

**Usage**:
```html
<app-price-chart [item]="selectedItem"></app-price-chart>
```

### DashboardComponent

**Purpose**: Main application layout with header, content, and footer.

**Features**:
- Sticky header
- Responsive grid layout
- Clear selection button
- Usage instructions
- Footer with copyright

## Configuration

### Environment Variables

**Development** (`environment.ts`):
```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1'
};
```

**Production** (`environment.prod.ts`):
```typescript
export const environment = {
  production: true,
  apiBaseUrl: '/api/v1'
};
```

### Angular Material Theme

The application uses Angular Material's default theme with:
- Primary color: Azure
- Tertiary color: Blue
- Typography: Roboto
- Density: 0 (default)

Custom theme can be configured in `src/styles.scss`.

## Accessibility

The application follows WCAG 2.1 AA guidelines:

- Semantic HTML elements
- ARIA labels on interactive elements
- Keyboard navigation support
- Focus indicators
- Color contrast compliance
- Screen reader friendly
- Reduced motion support

## Performance Optimizations

- **OnPush Change Detection**: Used where possible
- **Lazy Loading**: Route-based code splitting
- **Tree Shaking**: Unused code elimination
- **Production Build**: Minification and optimization
- **Debouncing**: Search input debounced to reduce API calls
- **Chart Optimization**: Only render when data changes

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Android)

## Troubleshooting

### Build Errors

**Problem**: Font inlining error
```
Inlining of fonts failed. https://fonts.googleapis.com returned 403
```

**Solution**: Font inlining is disabled in `angular.json`:
```json
"optimization": {
  "fonts": false
}
```

### API Connection Issues

**Problem**: CORS errors or connection refused

**Solution**:
1. Ensure backend is running on `http://localhost:8080`
2. Check CORS configuration in backend `WebConfig.java`
3. Verify `environment.ts` has correct `apiBaseUrl`

### TypeScript Strict Mode Errors

**Problem**: Strict null checks failing

**Solution**: All nullable values are properly handled with:
- Null checks (`if (value !== null)`)
- Optional chaining (`item?.category`)
- Type guards

## Development Guidelines

### Code Style

- Follow Angular Style Guide
- Use TypeScript strict mode
- No `any` types
- Explicit return types
- OnPush change detection preferred
- Signals for reactive state

### Component Structure

```typescript
@Component({
  selector: 'app-example',
  standalone: true,
  imports: [...],
  templateUrl: './example.component.html',
  styleUrl: './example.component.scss'
})
export class ExampleComponent {
  // Signals
  data = signal<Data[]>([]);

  // Inputs
  @Input() config: Config | null = null;

  // Outputs
  @Output() action = new EventEmitter<Action>();

  // Constructor
  constructor(private service: Service) {}

  // Lifecycle hooks
  ngOnInit(): void { }

  // Methods
  public performAction(): void { }
}
```

### Testing

All components and services have comprehensive unit tests:

- **Services**: HTTP mocking with `HttpClientTestingModule`
- **Components**: Isolated testing with TestBed
- **Coverage**: Target 80%+ code coverage

Example test:
```typescript
describe('ExampleComponent', () => {
  let component: ExampleComponent;
  let fixture: ComponentFixture<ExampleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExampleComponent, ...]
    }).compileComponents();

    fixture = TestBed.createComponent(ExampleComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
```

## Deployment

### Development

```bash
npm start
# Opens http://localhost:4200
```

### Production

```bash
# Build for production
npm run build

# Serve with a static file server
npx http-server dist/frontend/browser -p 8080
```

### Docker

The application can be served with nginx in production:

```dockerfile
FROM nginx:alpine
COPY dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.

## License

This project is part of the Dofus Retro Price Tracker system.

## Contributing

1. Follow Angular style guide
2. Write unit tests for all new code
3. Ensure TypeScript strict mode compliance
4. Test on multiple browsers
5. Update documentation

---

**Built with Angular 20 & Material Design**
