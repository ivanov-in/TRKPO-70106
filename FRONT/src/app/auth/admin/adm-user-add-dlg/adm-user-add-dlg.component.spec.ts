import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdmUserAddDlgComponent } from './adm-user-add-dlg.component';

describe('AdmUserAddDlgComponent', () => {
  let component: AdmUserAddDlgComponent;
  let fixture: ComponentFixture<AdmUserAddDlgComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AdmUserAddDlgComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdmUserAddDlgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
