import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-layout-page',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css']
})
export class AdminLayoutComponent implements OnInit {

  constructor() {
  }

  ngOnInit(): void {
  }

  onNavDevicesClick(): void {

  }

  onNavUsersClick(): void {

  }

  onOutletChenge(component): void {
    // if (component.constructor.name === 'AdmDevicesActivityComponent'){
    //   document.getElementById('nav_adm_devices').setAttribute('class', 'active');
    //   document.getElementById('nav_adm_users').removeAttribute('class');
    // }
    // if (component.constructor.name === 'AdmUsersPageComponent') {
    //   document.getElementById('nav_adm_users').setAttribute('class', 'active');
    //   document.getElementById('nav_adm_devices').removeAttribute('class');
    // }
  }
}
