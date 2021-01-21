import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {AuthService} from './auth.service';

@Injectable({
    providedIn: 'root'
  }
)
export class AdmDevices {
  constructor(private http: HttpClient, private authService: AuthService) {

  }

  getUsers(): Observable<any> {
    const allHeaders = {
      'Content-Encoding': 'gzip, deflate',
      'accept-language': 'en-US,en;q=0.8',
      accept: 'application/json, application/json'
    };

    return this.http.post('/authorised/admusers/admuserslist', null, {headers: allHeaders}).pipe(
      map(data => {
        return data;
      }),
      catchError(selector => {
        if (selector.status === 401) {
          this.authService.getToken().subscribe((tokenResult: any) => {
            if (tokenResult.token === undefined) {
              this.authService.logout();
              window.location.reload();
            } else {
              window.location.reload();
            }
          });
        }
        return of(false);
      })
    );
  }
}
