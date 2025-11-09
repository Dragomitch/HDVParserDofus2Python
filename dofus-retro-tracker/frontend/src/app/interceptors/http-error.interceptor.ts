import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, retry, throwError, timer } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * HTTP error interceptor with retry logic and error notification
 */
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    // Retry failed requests up to 2 times with exponential backoff
    retry({
      count: 2,
      delay: (error: HttpErrorResponse, retryCount: number) => {
        // Only retry on network errors or 5xx server errors
        if (error.status === 0 || error.status >= 500) {
          const delayMs = Math.pow(2, retryCount) * 1000; // Exponential backoff
          console.log(`Retrying request (attempt ${retryCount + 1}) after ${delayMs}ms`);
          return timer(delayMs);
        }
        // Don't retry on client errors (4xx)
        return throwError(() => error);
      }
    }),
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unknown error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Client Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 0:
            errorMessage = 'Unable to connect to server. Please check your connection.';
            break;
          case 400:
            errorMessage = 'Bad request. Please check your input.';
            break;
          case 404:
            errorMessage = 'Resource not found.';
            break;
          case 500:
            errorMessage = 'Internal server error. Please try again later.';
            break;
          default:
            errorMessage = `Server Error: ${error.status} - ${error.message}`;
        }
      }

      // Show error notification
      snackBar.open(errorMessage, 'Close', {
        duration: 5000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });

      console.error('HTTP Error:', error);
      return throwError(() => error);
    })
  );
};
