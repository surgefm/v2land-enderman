import * as Rx from 'rxjs';

// export const apiUrl = `http://10.8.124.160:880`;
export const apiUrl = `http://portal.gun.com/api`;

export function rxFetch<T>(url: string, ...args: any[]): Rx.Observable<T> {
  return Rx.Observable.create((observer: any) => {
    fetch(url, ...args)
      .then(response => response.json()) // or text() or blob() etc.
      .then(data => {
        observer.next(data);
        observer.complete();
      })
      .catch(err => observer.error(err));
  })
}

export async function easyFetch<T>(url: string, ...args: any[]) {
  const resp = await fetch(url, ...args);
  const data = await resp.json()
  return data as T;
}

export function sleep(timeout: number) {
  return new Promise(resolve => setTimeout(resolve, timeout));
}
