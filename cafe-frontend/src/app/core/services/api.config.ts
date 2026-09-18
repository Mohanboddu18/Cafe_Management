export function getBaseUrl(): string {
  if (typeof window !== 'undefined' && window.location && window.location.origin) {
    const origin = window.location.origin;
    if (origin.includes(':4200')) {
      return 'http://localhost:8080';
    }
    return origin;
  }
  return '';
}
