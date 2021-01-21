import {ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable, of} from 'rxjs';
import {Injectable} from '@angular/core';
import {AuthService} from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate, CanActivateChild {

  constructor(private auth: AuthService, private route: Router) {

  }


  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {

    if (this.auth.getRoles().length === 0) {
      return this.auth.getUserRoles();
    } else {
      return this.navigate(route, state);
    }
  }

  canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.canActivate(childRoute, state);
  }

  navigate(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    if (this.auth.isAuthenticated()) {
      if (state.url === '/admin/adm_devices') {
        if (this.auth.isAdmin()) {
          return of(true);
        }
        this.route.navigate(['/access-denied'], {});
        return of(true);
      }
      if (state.url === '/man/man_tasks') {
        if (this.auth.isMan()) {
          return of(true);
        }
        this.route.navigate(['/access-denied'], {});
        return of(true);
      }
      if (state.url === '/adm_self') {
        if (this.auth.isIns() || this.auth.isAdmin() || this.auth.isMan()) {
          return of(true);
        }
        this.route.navigate(['/access-denied'], {});
        return of(true);
      }
      return of(true);
    } else {
      this.route.navigate(['/login'], {
        queryParams: {
          accessDenied: true
        }
      });
      return of(false);
    }
  }
}
