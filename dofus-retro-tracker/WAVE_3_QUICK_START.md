# Wave 3: Angular Frontend - Quick Start Guide

## Start the Frontend

```bash
cd /home/user/HDVParserDofus2Python/dofus-retro-tracker/frontend
npm install  # Only needed first time
npm start    # Starts dev server on http://localhost:4200
```

## Project Overview

**Framework:** Angular 20.3.9 with standalone components
**UI Library:** Angular Material 20
**Charts:** Chart.js 4.x with ng2-charts
**Language:** TypeScript 5.x (strict mode)

## Key Components

### 1. ItemSelector Component
- **Location:** `src/app/components/item-selector/`
- **Purpose:** Autocomplete search for Dofus items
- **Features:** Debounced search, category filter, loading states

### 2. PriceChart Component
- **Location:** `src/app/components/price-chart/`
- **Purpose:** Price history visualization with Chart.js
- **Features:** Time-series chart, dual Y-axis, statistics

### 3. Dashboard Component
- **Location:** `src/app/components/dashboard/`
- **Purpose:** Main application layout
- **Features:** Responsive design, sticky header, footer

## API Integration

The frontend connects to the Spring Boot backend:

**Development:** `http://localhost:8080/api/v1`
**Production:** `/api/v1`

### Endpoints Used:
- `GET /api/v1/items?page=0&size=10&search=sword&categoryId=1`
- `GET /api/v1/items/{id}/prices?startDate=...&endDate=...`
- `GET /api/v1/categories`

## Build Commands

```bash
# Development server (with hot reload)
npm start

# Production build
npm run build

# Run tests
npm test

# Lint code
ng lint
```

## Project Structure

```
frontend/src/app/
├── components/
│   ├── item-selector/     # Search component
│   ├── price-chart/       # Chart component
│   └── dashboard/         # Layout component
├── services/
│   └── api.service.ts     # REST API client
├── models/
│   └── *.model.ts         # TypeScript interfaces
├── interceptors/
│   └── http-error.interceptor.ts  # Error handling
└── environments/
    ├── environment.ts     # Dev config
    └── environment.prod.ts # Prod config
```

## Key Features

✅ **Responsive Design** - Mobile, tablet, desktop support
✅ **Material Design** - Modern UI with Angular Material
✅ **Type Safety** - TypeScript strict mode enabled
✅ **Error Handling** - Retry logic with exponential backoff
✅ **Testing** - Comprehensive unit tests
✅ **Accessibility** - WCAG 2.1 AA compliant
✅ **Dark Mode** - System preference support

## Configuration

### Change API URL

Edit `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://your-backend:8080/api/v1'
};
```

### Change Theme

Edit `src/styles.scss`:
```scss
@use '@angular/material' as mat;

html {
  @include mat.theme((
    color: (
      primary: mat.$azure-palette,  // Change this
      tertiary: mat.$blue-palette,  // And this
    ),
    typography: Roboto,
    density: 0,
  ));
}
```

## Troubleshooting

### Port already in use

```bash
# Use a different port
ng serve --port 4201
```

### Build errors

```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Backend connection issues

1. Ensure backend is running on `http://localhost:8080`
2. Check CORS configuration in backend
3. Verify `environment.ts` has correct `apiBaseUrl`

## Testing

```bash
# Run all tests
npm test

# Run tests with coverage
npm test -- --code-coverage

# Run tests in watch mode
npm test -- --watch
```

**Note:** Tests require Chrome/Chromium browser installed.

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

# Output: dist/frontend/
# Serve with nginx or any static file server
```

## Documentation

- **Frontend README:** `/frontend/README.md`
- **Wave 3 Report:** `/WAVE_3_ANGULAR_FRONTEND_COMPLETION_REPORT.md`
- **Angular Docs:** https://angular.dev
- **Material Docs:** https://material.angular.dev

## Next Steps

1. **Start Backend:** Ensure Spring Boot backend is running
2. **Start Frontend:** `npm start`
3. **Open Browser:** http://localhost:4200
4. **Test Features:**
   - Search for items
   - View price charts
   - Test responsive design
   - Check error handling

---

**Wave 3 Status:** ✅ COMPLETE
**Ready for:** Integration testing and deployment
