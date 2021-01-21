import { Component, OnInit } from '@angular/core';
import {Router} from '@angular/router';
import {AuthService} from '../../../shared/services/auth.service';

@Component({
  selector: 'app-man-layoutyout',
  templateUrl: './man-layoutyout.component.html',
  styleUrls: ['./man-layoutyout.component.css']
})
export class ManLayoutyoutComponent implements OnInit {

  constructor(private router: Router, private auth: AuthService) { }

  ngOnInit() {
  }
  man_tasks_click() {
    this.router.navigate(['/man/man_tasks']);
  }

  man_work_inspectors_click() {
    this.router.navigate(['/man/man_work_inspectors']);
  }

  man_aggregated_reports_click() {
    this.router.navigate(['/man/man_aggregated_reports']);
  }
}
