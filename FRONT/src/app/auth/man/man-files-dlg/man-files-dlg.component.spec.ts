import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManFilesDlgComponent } from './man-files-dlg.component';

describe('ManFilesDlgComponent', () => {
  let component: ManFilesDlgComponent;
  let fixture: ComponentFixture<ManFilesDlgComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManFilesDlgComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManFilesDlgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
