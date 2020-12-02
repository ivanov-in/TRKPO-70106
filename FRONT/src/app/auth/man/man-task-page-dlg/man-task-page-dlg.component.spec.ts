import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManTaskPageDlgComponent } from './man-task-page-dlg.component';

describe('ManTaskPageDlgComponent', () => {
  let component: ManTaskPageDlgComponent;
  let fixture: ComponentFixture<ManTaskPageDlgComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManTaskPageDlgComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManTaskPageDlgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
