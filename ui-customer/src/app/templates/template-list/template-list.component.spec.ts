import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TemplateListComponent } from "./template-list.component";

describe("TemplateListComponent", () => {
    let component: TemplateListComponent;
    let fixture: ComponentFixture<TemplateListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TemplateListComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TemplateListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
