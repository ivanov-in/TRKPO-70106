import {Component, OnInit} from '@angular/core';
import {RowClassArgs, GridDataResult, DataStateChangeEvent} from '@progress/kendo-angular-grid';
import {State, process} from '@progress/kendo-data-query';
import {worksinspector} from '../../../data/services/worksinspector.service';
import {FormControl, FormGroup} from '@angular/forms';

@Component({
  selector: 'app-man-work-inspectors-page',
  templateUrl: './man-work-inspectors-page.component.html',
  styleUrls: ['./man-work-inspectors-page.component.css']
})

export class ManWorkInspectorsPageComponent implements OnInit {
  public gridData: any[] = worksinspector;
  public gridView: any[];
  public view: any;
  public gridState: State = {
    skip: 0,
    take: 5,
    filter: {
      logic: 'and',
      filters: [{field: 'data', operator: 'contains', value: ''}]
    }
  };

  public min: Date = new Date(2020, 0, 1);
  public max: Date = new Date();
  public dataValue: Date = new Date();
  public registerForm: FormGroup = new FormGroup({
    date: new FormControl(new Date())
  });
  // public submitForm(): void {
  //   this.registerForm.markAllAsTouched();
  // }
  //
  // public clearForm(): void {
  //   this.registerForm.reset();
  // }

  public ngOnInit(): void {
    this.gridView = this.gridData;
    this.view = worksinspector;
  }

  public onMinDateClick(event) {
    this.dataValue = new Date(this.dataValue.valueOf() - 24 * 60 * 60 * 1000);
  }

  public onMaxDateClick(event) {
    this.dataValue = new Date(this.dataValue.valueOf() + 24 * 60 * 60 * 1000);
  }

  public dataStateChange(state: DataStateChangeEvent): void {
    this.gridState = state;
    this.view = process(worksinspector, this.gridState);
  }

  public onDatePicker(inpValue: string): void {
    this.view = process(this.gridData, {
      filter: {
        logic: 'or',
        filters: [
          {
            field: 'data',
            operator: 'contains',
            value: inpValue
          }
        ]
      }
    }).data;
  }

  public onFilter(inputValue: string): void {
    this.view = process(this.gridData, {
      filter: {
        logic: 'or',
        filters: [
          {
            field: 'data',
            operator: 'contains',
            value: inputValue
          },
          {
            field: 'wnum',
            operator: 'contains',
            value: inputValue
          },
          {
            field: 'man_full_name',
            operator: 'contains',
            value: inputValue
          },
          {
            field: 'phone',
            operator: 'contains',
            value: inputValue
          },
          {
            field: 'address',
            operator: 'contains',
            value: inputValue
          },
          {
            field: 'status_ru',
            operator: 'contains',
            value: inputValue
          },
          {
            field: 'trusted_phone',
            operator: 'contains',
            value: inputValue
          },
          {
            field: 'dt_end',
            operator: 'contains',
            value: inputValue
          },
          {
            field: 'result_rut',
            operator: 'contains',
            value: inputValue
          }
        ],
      }
    }).data;
  }

  public rowCallback = (context: RowClassArgs) => {
    switch (context.dataItem.lock) {
      case true:
        return {gold: true};
      case false:
        return {green: true};
      default:
        return {};
    }
  }
}
