import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AuthService} from "../../shared/services/auth.service";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, map, tap} from "rxjs/operators";

@Injectable({
        providedIn: 'root'
    }
)

export class ManService extends BehaviorSubject<any[]> {
    constructor(private http: HttpClient, public authService: AuthService) {
        super([]);
    }

    private data: any[] = [];

    private result: Observable<any[]>;

    public read() {
        this._read().pipe(
            tap(res => {
                this.data = res;
            })
        ).subscribe(res => {
            super.next(res);
        });
    }

    private _read(action: string = '', data?: any): Observable<any[]> {
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.result = this.http.post('/authorised/manusers/inspectors', null, {headers: allHeaders}).pipe(
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

    public addTask(adr, city, street, house, nd, purpose, prim, ttime, id_ins, email, lat, lan, s_zulu, b_zullu, ststus) {
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        const jsonParam = {
            'address': adr,
            'city': city,
            'street': street,
            'korpus': '',
            'house': nd,
            'purpose': purpose.toString(),
            'prim': '',
            'time': ttime.toLocaleDateString() + ' ' + ttime.toLocaleTimeString(),
            'id_ins': id_ins,
            'email': email,
            'lat': lat.toString(),
            'lan': lan.toString(),
            'szulu': '',
            'bzulu': '',
            'status': ststus.toString()
        }
        this.http.post('/authorised/manusers/add_task', JSON.stringify(jsonParam), {headers: allHeaders})
            .subscribe(response => {
                window.location.reload()
                return response
            })
    }

    public changeTask(adr, city, street, house, nd, purpose, prim, ttime, id_ins, lat, lan, s_zulu, b_zullu, ststus) {
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        const jsonParam = {
            'address': adr,
            'city': city,
            'street': street,
            'korpus': '',
            'house': nd,
            'purpose': purpose.toString(),
            'prim': '',
            'time': ttime.toLocaleDateString() + ' ' + ttime.toLocaleTimeString(),
            'id_ins': id_ins.toString(),
            'lat': lat.toString(),
            'lan': lan.toString(),
            'szulu': '',
            'bzulu': '',
            'status': ststus.toString()
        }
        this.http.post('/authorised/admusers/change_task', JSON.stringify(jsonParam), {headers: allHeaders})
            .subscribe(response => {
                window.location.reload()
                return response
            })
    }

    public getObj(text) {
        const allHeaders = {
            'Content-Encoding': 'gzip, deflate',
            'accept-language': 'en-US,en;q=0.8',
            accept: 'application/json, application/json'
        };
        this.http.post('/authorised/manusers/get_obj', text, {headers: allHeaders})
            .subscribe(response => {
                return response
            })
    }
}
