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

    private onNewIns: Observable<any>;
    private onDelete: Observable<any>;
    private resultTask: Observable<any[]>;
    private resEvent: Observable<any[]>;

    private resultEvent: Observable<any[]>;

    // private resultDevices: Observable<any[]>;


    public fetch(): Observable<any[]> {
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        return this.http.post<any[]>('/authorised/admusers/admuserslist', null, {headers: allHeaders}).pipe();
    }

    public search_event(date, str): Observable<any[]> {
        const json = {
            datePic: date,
            string: str,
        };
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        return this.http.post<any[]>('/authorised/manusers/search_events', JSON.stringify(json), {headers: allHeaders}).pipe();
    }

    public set_role(roles, id) {
        const sub = this._set_role(roles, id).subscribe(res => {
            super.next(res);
            super.complete();
            sub.unsubscribe();
        });
    }

    public _set_role(roles, id): Observable<any> {
        const allHeaders = {
            'roles': roles,
            'id_user': id.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        return this.http.post<any>('/authorised/admusers/admuser_roles_add', null, {headers: allHeaders}).pipe();
    }

    public passSetDef(id) {
        const sub = this._passSetDef(id).subscribe(res => {
            super.next(res);
            super.complete();
            sub.unsubscribe();
        });
    }

    private _passSetDef(id): Observable<any> {
        const allHeaders = {
            'id_insp': id.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        return this.http.post<any>('/authorised/admusers/admuser_pass_set', null, {headers: allHeaders}).pipe();

    }

    public userLock(lock, id) {
        const subUserLock = this._userLock(lock, id).subscribe(res => {
            super.next(res);
            super.complete();
            subUserLock.unsubscribe();
        });
    }

    private _userLock(lock, id): Observable<any> {
        const allHeaders = {
            'lock_user': lock.toString(),
            'id_insp': id.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        return this.http.post<any>('/authorised/admusers/admuser_user_lock', null, {headers: allHeaders}).pipe();
    }

    public insertIns(fio, tel, roles) {
        const unSubInsert = this._insertIns(fio, tel, roles).subscribe(res => {
            super.next(res);
            super.complete();
            unSubInsert.unsubscribe();
        });
    }

    private _insertIns(fio, tel, roles): Observable<any> {
        const allHeaders = {
            'tel_insp': tel,
            'roles': roles,
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'text/plain'
        };
        this.onNewIns = this.http.post('/authorised/admusers/admuser_new_user', fio, {headers: allHeaders}).pipe(
            map(res => <any> res),
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
        const subDeleteUser = this._delete_user(id).pipe(
            tap(res => {
                console.log('User delete: ' + res);
            })
        ).subscribe(res => {
            super.next(res);
            super.complete();
            subDeleteUser.unsubscribe();
        });
    }

    private _delete_user(id): Observable<any> {
        const allHeaders = {
            'id_user': id.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'text/plain'
        };
        this.onDelete = this.http.post('/authorised/admusers/user_delete', null, {headers: allHeaders}).pipe(
            map(res => <any> res),
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

    public readTask(date, id, puser) {
        const unSubReadTask = this._readTask(date, id, puser).subscribe(res => {
            super.next(res);
            super.complete();
            unSubReadTask.unsubscribe();
        });
    }

    public _readTask(date, id, puser): Observable<any[]> {
        const allHeaders = {
            'datePic': date,
            'id_insp': id,
            'puser': puser,
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.resultTask = this.http.post<any[]>('/authorised/manusers/get_task_inspector', null, {headers: allHeaders}).pipe();
        return this.resultTask;
    }

    public getEvents() {
        const unSubEvent = this._getEvents().subscribe(res => {
            super.next(res);
            super.complete();
            unSubEvent.unsubscribe();
        });
    }

    public _getEvents(): Observable<any[]> {
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.resEvent = this.http.post('/authorised/admusers/events_list', null, {headers: allHeaders}).pipe(
            map(res => <any[]> res),
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
        return this.resEvent;
    }

    public set_tel(id, tel) {
        const unSubTel = this._set_tel(id, tel).subscribe(res => {
            super.next(res);
            super.complete();
            unSubTel.unsubscribe();
        });
    }

    private _set_tel(id, tel): Observable<any> {
        const allHeaders = {
            'id_user': id.toString(),
            'tel_user': tel.toString(),
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        return this.http.post<any>('/authorised/admusers/admuser_set_tel', null, {headers: allHeaders}).pipe();
    }

    public readEvents(date) {
        const unSubReadEvents = this._readEvents(date).subscribe(res => {
            super.next(res);
            // super.complete();
            unSubReadEvents.unsubscribe();
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
            map(res => <any[]> res),
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
        return this.resultEvent;
    }

    // public listDevices() {
    //     const unSubDeveces = this._listDevices().subscribe(res => {
    //         super.next(res);
    //     });
    // }

    // public _listDevices(): Observable<any[]> {
    //     const allHeaders = {
    //         'Content-Encoding': 'gzip, deflate',
    //         'accept-language': 'en-US,en;q=0.8',
    //         accept: 'application/json, application/json'
    //     };
    //     this.resultDevices = this.http.post('/authorised/admusers/devises_list', null, {headers: allHeaders}).pipe(
    //         map(res => <any[]> res),
    //         catchError(selector => {
    //             if (selector.status === 401) {
    //                 this.authService.getToken().subscribe((tokenResult: any) => {
    //                     if (tokenResult.token === undefined) {
    //                         this.authService.logout();
    //                         this.authService.setTokenInternal(null);
    //                         window.location.reload();
    //                     } else {
    //                         window.location.reload();
    //                     }
    //                 });
    //             }
    //             return of([]);
    //         })
    //     );
    //     return this.resultDevices;
    // }
}
