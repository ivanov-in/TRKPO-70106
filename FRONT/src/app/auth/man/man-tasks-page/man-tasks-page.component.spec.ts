import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManTasksPageComponent } from './man-tasks-page.component';

describe('ManTasksPageComponent', () => {
  let component: ManTasksPageComponent;
  let fixture: ComponentFixture<ManTasksPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManTasksPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManTasksPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
