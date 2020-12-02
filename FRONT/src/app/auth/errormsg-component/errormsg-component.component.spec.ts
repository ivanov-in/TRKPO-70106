import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrormsgComponentComponent } from './errormsg-component.component';

describe('ErrormsgComponentComponent', () => {
  let component: ErrormsgComponentComponent;
  let fixture: ComponentFixture<ErrormsgComponentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ErrormsgComponentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrormsgComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
