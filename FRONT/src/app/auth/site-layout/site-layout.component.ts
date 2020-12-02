import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AuthService} from '../../shared/services/auth.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
    selector: 'app-site-layout',
    templateUrl: './site-layout.component.html',
    styleUrls: ['./site-layout.component.scss']
})
export class SiteLayoutComponent implements OnInit {
    form: FormGroup;
    public disabled = false;

    constructor(private router: Router, private auth: AuthService) {
    }

    ngOnInit() {
        this.form = new FormGroup({
            // btn_adm: new FormControl(null),
            // btn_man: new FormControl(null)
        });
    }


    isAdmin(): boolean {
        return this.auth.isAdmin();
    }

    isMan(): boolean {
        return this.auth.isMan();
    }

    getCurrentOutletSigment() {
        return this.router.url.toString();
    }

    canShowBtnAdmMan(): boolean {
        if (this.isAdmin()) {
            if (this.getCurrentOutletSigment().startsWith('/adm_self') || this.getCurrentOutletSigment().startsWith('/man')) {
                return true;
            }
        } else {
            return false;
        }
    }

    canShowBtnAdmManLogOut(): boolean {
        if (this.getCurrentOutletSigment().startsWith('/adm_self') || this.getCurrentOutletSigment().startsWith('/man') || this.getCurrentOutletSigment().startsWith('/admin')) {
            return true;
        }
    }

    canShowBtnAdmManSelf(): boolean {
        if (this.isAdmin() || this.isMan()) {
            if (this.getCurrentOutletSigment().startsWith('/man/') || this.getCurrentOutletSigment().startsWith('/admin') || this.getCurrentOutletSigment().startsWith('/adm_self')) {
                if (this.getCurrentOutletSigment().startsWith('/adm_self')) {
                    this.disabled = true;
                }
                return true;
            }
        } else {
            return false;
        }
    }

    canShowBtnMan(): boolean {
        if (this.isMan()) {
            if (this.getCurrentOutletSigment().startsWith('/admin/')) {
                return true;
            }
        } else {
            return false;
        }
    }


    adm_page_click() {
        if (this.isAdmin()) {
            this.router.navigate(['/admin/adm_users']);
            this.disabled = false;
        }
    }

    log_out_click() {
        this.router.navigate(['/login']);
        this.disabled = false;
    }

    self_adm_page_click() {
        if (this.disabled !== true) {
            this.router.navigate(['/adm_self']);
            this.disabled = false;
        }
    }

    man_page_click() {
        if (this.isAdmin()) {
            this.router.navigate(['/man/man_tasks']);
            this.disabled = false;
        }
    }

    onActivate(event) {

    }

    onDeactivate(event) {

    }
}
