import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdmUserEditDlgComponent } from './adm-user-edit-dlg.component';

describe('AdmUserEditDlgComponent', () => {
  let component: AdmUserEditDlgComponent;
  let fixture: ComponentFixture<AdmUserEditDlgComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AdmUserEditDlgComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdmUserEditDlgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
