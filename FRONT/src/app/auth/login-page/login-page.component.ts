import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../../shared/services/auth.service';
import {Router} from '@angular/router';
import {Observable, Subscribable, Subscription} from 'rxjs';

@Component({
    selector: 'app-login-page-component',
    templateUrl: './login-page.component.html',
    styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent implements OnInit {

    constructor(private auth: AuthService,
                private router: Router) {
    }
    form: FormGroup;

    subOnSubmit: Subscription;

    ngOnInit() {
        this.form = new FormGroup({
            email: new FormControl(null, [Validators.required, Validators.required]),
            password: new FormControl(null, [Validators.required, Validators.minLength(6)])
        });
    }

    onSubmit() {
        const user = {
            Email: this.form.value.email,
            password: this.form.value.password
        };
        this.form.disable();
        this.auth.email = user.Email;
        localStorage.setItem('email', user.Email);
        this.subOnSubmit = this.auth.getTicketForTicket(user.Email).subscribe((data: any) => {
                if (data.data === undefined) {
                    this.form.enable();
                    this.form.get('password').setErrors({incorrect: true});
                    this.subOnSubmit.unsubscribe();
                } else if (data.data.server_key === undefined) {
                    this.form.enable();
                    this.form.get('password').setErrors({incorrect: true});
                    this.subOnSubmit.unsubscribe();
                } else {
                    const subGetTicketForToken: Subscription = this.auth.getTicketForToken(this.form.get('password').value).subscribe((ticketForTokenResult: any) => {
                        if (ticketForTokenResult === undefined) {
                            this.form.enable();
                            this.form.get('password').setErrors({incorrect: true});
                            this.subOnSubmit.unsubscribe();
                            subGetTicketForToken.unsubscribe();
                        } else if (ticketForTokenResult.ticket_for_token === undefined) {
                            this.form.enable();
                            this.form.get('password').setErrors({incorrect: true});
                            this.subOnSubmit.unsubscribe();
                            subGetTicketForToken.unsubscribe();
                        } else {
                            const subGetToken: Subscription = this.auth.getToken().subscribe((tokenResult: any) => {
                                if (tokenResult.token === undefined) {
                                    this.form.enable();
                                    this.form.get('password').setErrors({incorrect: true});
                                    this.subOnSubmit.unsubscribe();
                                    subGetTicketForToken.unsubscribe();
                                    subGetToken.unsubscribe();
                                } else {
                                    const subAuth: Subscription = this.auth.getUserRoles().subscribe((rows: any) => {
                                        this.onAuch();
                                        this.subOnSubmit.unsubscribe();
                                        subGetTicketForToken.unsubscribe();
                                        subGetToken.unsubscribe();
                                        subAuth.unsubscribe();
                                    });
                                }
                            });
                        }
                    });
                }
            }
        );
    }


    onAuch() {
        if (this.auth.isMan()) {
            this.router.navigate(['/man/man_tasks']);
        } else if (this.auth.isAdmin()) {
            this.router.navigate(['/admin/adm_devices']);
        } else if (this.auth.isIns() || this.auth.isAdmin() || this.auth.isMan()) {
            this.router.navigate(['/adm_self']);
        } else {
            this.router.navigate(['/access-denied']);
        }
    }
}
