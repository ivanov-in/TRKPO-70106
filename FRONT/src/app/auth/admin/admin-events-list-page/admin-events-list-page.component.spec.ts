import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminEventsListPageComponent } from './admin-events-list-page.component';

describe('AdminEventsListPageComponent', () => {
  let component: AdminEventsListPageComponent;
  let fixture: ComponentFixture<AdminEventsListPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AdminEventsListPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminEventsListPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
