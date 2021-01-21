import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdmSelfPageComponent } from './adm-self-page.component';

describe('AdmUsersPageComponent', () => {
    let component: AdmSelfPageComponent;
    let fixture: ComponentFixture<AdmSelfPageComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ AdmSelfPageComponent ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AdmSelfPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
