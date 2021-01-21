import {Injectable} from '@angular/core';
import {Observable, BehaviorSubject, of} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {catchError, map, tap} from 'rxjs/operators';
import {AuthService} from '../../shared/services/auth.service';

@Injectable({
        providedIn: 'root'
    }
)
export class UserService extends BehaviorSubject<any[]> {
    constructor(private http: HttpClient, public authService: AuthService) {
        super([]);
    }

    private data: any[] = [];

    // private fetch(action: string = '', data?: any): Observable<any[]> {
    //   return this.http
    //     .jsonp(`https://demos.telerik.com/kendo-ui/service/Products/${action}?${this.serializeModels(data)}`, 'callback')
    //     .pipe(map(res => <any[]>res));
    // }
    private result: Observable<any[]>;

    public selectUsers() {
        this.fetch().pipe(
            tap(res => {
                this.data = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    private fetch(action: string = '', data?: any): Observable<any[]> {
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.result = this.http.post('/authorised/admusers/admuserslist', null, {headers: allHeaders}).pipe(
            map(res => <any[]>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.result
    }

    public set_role(roles, id) {
        this._set_role(roles, id).pipe(
            tap(res => {
                //this.data = res;
                res = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    private result_role: Observable<any>;

    private _set_role(roles, id): Observable<any> {
        const allHeaders = {
            'roles': roles,
            'id_user': id.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.result_role = this.http.post('/authorised/admusers/admuser_roles_add', null, {headers: allHeaders}).pipe(
            map(res => <any>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.result_role;
    }

    public passSetDef(id) {
        this._passSetDef(id).pipe(
            tap(res => {
                //this.data = res;
                res = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    private result_pass: Observable<any>;

    private _passSetDef(id): Observable<any> {
        const allHeaders = {
            'id_insp': id.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.result_pass = this.http.post('/authorised/admusers/admuser_pass_set', null, {headers: allHeaders}).pipe(
            map(res => <any>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.result_pass;
    }

    public userLock(lock, id) {
        this._userLock(lock, id).pipe(
            tap(res => {
                //this.data = res;
                res = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    private onLock: Observable<any>;

    private _userLock(lock, id): Observable<any> {
        const allHeaders = {
            'lock_user': lock.toString(),
            'id_insp': id.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.onLock = this.http.post('/authorised/admusers/admuser_user_lock', null, {headers: allHeaders}).pipe(
            map(res => <any>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.onLock;
    }

    public insertIns(fio, tel, roles) {
        this._insertIns(fio, tel, roles).pipe(
            tap(res => {
                //this.data = res;
                res = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    private onNewIns: Observable<any>;

    private _insertIns(fio, tel, roles): Observable<any> {
        const allHeaders = {
            'tel_insp': tel,
            'roles': roles,
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'text/plain'
        };
        this.onNewIns = this.http.post('/authorised/admusers/admuser_new_user', fio, {headers: allHeaders}).pipe(
            map(res => <any>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        window.location.reload();
        return this.onNewIns;
    }

    public delete_user(id) {
        this._delete_user(id).pipe(
            tap(res => {
                //this.data = res;
                res = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    private onDelete: Observable<any>;

    private _delete_user(id): Observable<any> {
        const allHeaders = {
            'id_user': id.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'text/plain'
        };
        this.onDelete = this.http.post('/authorised/admusers/user_delete', null, {headers: allHeaders}).pipe(
            map(res => <any>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.onDelete;
    }


    private dataTask: any[] = [];
    private resultTask: Observable<any[]>;

    public readTask(date) {
        this._readTask(date).pipe(
            tap(res => {
                this.dataTask = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    public _readTask(date): Observable<any[]> {
        const allHeaders = {
            'nowDate': date,
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.resultTask = this.http.post('/authorised/manusers/list_tasks', null, {headers: allHeaders}).pipe(
            map(res => <any[]>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.resultTask
    }

    private dataEvent: any[] = [];
    private resEvent: Observable<any[]>;

    public getEvents() {
        this._getEvents().pipe(
            tap(res => {
                this.dataEvent = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    public _getEvents(): Observable<any[]> {
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.resEvent = this.http.post('/authorised/admusers/events_list', null, {headers: allHeaders}).pipe(
            map(res => <any[]>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.resEvent
    }

    public set_tel(id, tel) {
        this._set_tel( id, tel).pipe(
            tap(res => {
                //this.data = res;
                res = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    private result_tel: Observable<any>;

    private _set_tel(id, tel): Observable<any> {
        const allHeaders = {
            'id_user': id.toString(),
            'tel_user': tel.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.result_tel = this.http.post('/authorised/admusers/admuser_set_tel', null, {headers: allHeaders}).pipe(
            map(res => <any>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.result_tel;
    }


    private resultEvent: Observable<any[]>;

    public readEvents(date) {
        this._readEvents(date).pipe(
            tap(res => {
                res = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    public _readEvents(date): Observable<any[]> {
        const allHeaders = {
            'nowDate': date,
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.resultEvent = this.http.post('/authorised/admusers/select_events_list', null, {headers: allHeaders}).pipe(
            map(res => <any[]>res),
            catchError(selector => {
                if (selector.status === 401) {
                    this.authService.getToken().subscribe((tokenResult: any) => {
                        if (tokenResult.token === undefined) {
                            this.authService.logout();
                            this.authService.setTokenInternal(null);
                            window.location.reload();
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return of([]);
            })
        );
        return this.resultEvent
    }
}
