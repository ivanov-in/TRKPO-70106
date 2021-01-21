import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../../shared/services/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login-page-component',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent implements OnInit {
  form: FormGroup;

  constructor(private auth: AuthService,
              private router: Router) {
  }

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
    this.auth.getTicketForTicket(user.Email).subscribe((data: any) => {
        if (data.data === undefined) {
          this.form.enable();
          this.form.get('password').setErrors({incorrect: true});
        } else if (data.data.server_key === undefined) {
          this.form.enable();
          this.form.get('password').setErrors({incorrect: true});
        } else {
          this.auth.getTicketForToken(this.form.get('password').value).subscribe((ticketForTokenResult: any) => {
            if (ticketForTokenResult === undefined) {
              this.form.enable();
              this.form.get('password').setErrors({incorrect: true});
            } else if (ticketForTokenResult.ticket_for_token === undefined) {
              this.form.enable();
              this.form.get('password').setErrors({incorrect: true});
            } else {
              this.auth.getToken().subscribe((tokenResult: any) => {
                if (tokenResult.token === undefined) {
                  this.form.enable();
                  this.form.get('password').setErrors({incorrect: true});
                } else {
                  this.auth.getUserRoles().subscribe((rows: any) => {
                    this.onAuch();
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
