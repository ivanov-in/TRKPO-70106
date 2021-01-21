import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManLayoutyoutComponent } from './man-layoutyout.component';

describe('ManLayoutyoutComponent', () => {
  let component: ManLayoutyoutComponent;
  let fixture: ComponentFixture<ManLayoutyoutComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManLayoutyoutComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManLayoutyoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
