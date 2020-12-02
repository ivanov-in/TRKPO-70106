import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManAggregatedReportsPageComponent } from './man-aggregated-reports-page.component';

describe('ManAggregatedReportsPageComponent', () => {
  let component: ManAggregatedReportsPageComponent;
  let fixture: ComponentFixture<ManAggregatedReportsPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManAggregatedReportsPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManAggregatedReportsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
