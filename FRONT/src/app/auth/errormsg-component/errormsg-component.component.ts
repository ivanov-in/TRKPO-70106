import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
    selector: 'app-errormsg-component',
    templateUrl: './errormsg-component.component.html',
    styleUrls: ['./errormsg-component.component.css']
})
export class ErrormsgComponentComponent implements OnInit {
    public message: any;
    @Output() close = new EventEmitter<string>();

    @Input() set EditItem(value: any) {
        this.message = value;
    }

    constructor() {
    }

    ngOnInit() {
    }

    public closeErr(event) {
        this.close.emit('cancel');
    }
}
