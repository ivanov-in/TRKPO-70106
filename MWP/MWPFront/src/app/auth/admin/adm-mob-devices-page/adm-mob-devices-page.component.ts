import { Component, OnInit } from '@angular/core';
import {RowClassArgs} from '@progress/kendo-angular-grid';
import {process, State} from '@progress/kendo-data-query';
import {UserService} from '../../../data/services/user.service';
import {devices} from '../../../data/services/devices.sevice';

@Component({
  selector: 'app-adm-mob-devices-page',
  templateUrl: './adm-mob-devices-page.component.html',
  styleUrls: ['./adm-mob-devices-page.component.css']
})
export class AdmMobDevicesPageComponent implements OnInit {

  public view: any;
  public gridState: State = {
    sort: [],
    skip: 0,
    take: 10
  };

  constructor(private userService: UserService) {

  }

  ngOnInit() {
    this.view = devices;
  }

  public rowCallback = (context: RowClassArgs) => {
    switch (context.dataItem.lock) {
      case true:
        return {gold : true};
      case false:
        return {green : true};
      default:
        return {};
    }
  }
}
