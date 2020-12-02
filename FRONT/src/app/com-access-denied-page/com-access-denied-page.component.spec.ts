import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ComAccessDeniedPageComponent } from './com-access-denied-page.component';

describe('ComAccessDeniedPageComponent', () => {
  let component: ComAccessDeniedPageComponent;
  let fixture: ComponentFixture<ComAccessDeniedPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ComAccessDeniedPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ComAccessDeniedPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
