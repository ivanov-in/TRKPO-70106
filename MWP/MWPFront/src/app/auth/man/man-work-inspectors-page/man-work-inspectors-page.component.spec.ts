import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ManWorkInspectorsPageComponent } from './man-work-inspectors-page.component';

describe('ManWorkInspectorsPageComponent', () => {
  let component: ManWorkInspectorsPageComponent;
  let fixture: ComponentFixture<ManWorkInspectorsPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManWorkInspectorsPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManWorkInspectorsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
