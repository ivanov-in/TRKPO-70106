import {Component, Inject, OnInit, ViewEncapsulation} from '@angular/core';
import {GridDataResult, RowClassArgs} from '@progress/kendo-angular-grid';
import {UserService} from '../../../data/services/user.service';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {State, process} from '@progress/kendo-data-query';
import {FormControl, FormGroup} from "@angular/forms";
import {getLocaleId} from "@angular/common";

@Component({
    selector: 'app-adm-users-page',
    templateUrl: './adm-users-page.component.html',
    styleUrls: ['./adm-users-page.component.css'],
    encapsulation: ViewEncapsulation.None
})


export class AdmUsersPageComponent implements OnInit {
    public view: Observable<GridDataResult>;
    public opened = false;
    public openChange = false;
    public onblock = false;
    public disabled = true;
    public password = false;
    public personFIO: any;
    public personTEL: any;
    public ins: boolean;
    public adm: boolean;
    public man: boolean;
    public delete = false;
    public fio: any;
    public phone: any;
    public blokirator: any;
    public ondelete: boolean;
    public login: any;
    public gridState: State = {
        sort: [],
        skip: 0,
        take: 25
    };

    constructor(private userService: UserService) {
    }

    ngOnInit() {
        this.view = this.userService.pipe(map(
            data => process(data, this.gridState)));
        this.userService.selectUsers();
    }

    public open(event) {
        this.disabled = true;
        this.opened = true;
        this.ins = false
        this.adm = false
        this.man = false
    }

    public openedChange(event, dataItem) {
        this.openChange = true;
        this.fio = dataItem.FIO
        this.phone = dataItem.TEL
        this.login = dataItem.LOGIN
        this.ondelete = false;
        this.adm = false;
        this.man = false;
        this.ins = false;
        this.password = false;
        if (dataItem.USER_LOCK == 0) {
            this.blokirator = this.onblock = false;
        } else {
            this.blokirator = this.onblock = true;
        }

        if (dataItem.ROLELIST != null) {
            if (dataItem.ROLELIST.length == 3) {
                if (dataItem.ROLELIST[0] == "a") {
                    this.adm = true
                    this.ins = false
                    this.man = false
                } else if (dataItem.ROLELIST[0] == "i") {
                    this.ins = true;
                    this.man = false
                    this.adm = false
                } else {
                    this.man = true;
                    this.ins = false
                    this.adm = false
                }
            } else if (dataItem.ROLELIST.length == 7) {
                if (dataItem.ROLELIST[0] == "a" && dataItem.ROLELIST[4] == "i") {
                    this.adm = true;
                    this.ins = true
                    this.man = false
                } else if (dataItem.ROLELIST[0] == "a" && dataItem.ROLELIST[4] == "m") {
                    this.adm = true;
                    this.man = true
                    this.ins = false
                } else {
                    this.ins = true;
                    this.man = true;
                    this.adm = false
                }
            } else if (dataItem.ROLELIST.length > 7) {
                this.adm = true;
                this.man = true;
                this.ins = true;
            }
        }
    }

    public close(status) {
        console.log(`Dialog result: ${status}`);
        this.opened = false;
        this.openChange = false;
        this.delete = false;
    }

    public disabledCange() {
        this.disabled = !(this.personFIO != null && this.personFIO.length != 0);
    }

    public onNewPersone(event) {
        let roleList_ru = [];
        let role = [];
        if (this.ins == true) {
            roleList_ru.push("Инспектор")
            role.push('ins')
        }
        if (this.adm == true) {
            roleList_ru.push("Администратор")
            role.push('adm')
        }
        if (this.man == true) {
            roleList_ru.push("Руководитель")
            role.push('man')
        }
        if (role.toString().length == 0) {
            role = ['']
        }
        if (this.personTEL == null || this.personTEL == undefined) {
            this.userService.insertIns(this.personFIO, "", role.toString());
        } else {
            this.userService.insertIns(this.personFIO, this.personTEL, role.toString());
        }

    }

    public deleteUser(event) {
        for (let i = 0; i < this.view['destination']._value.length; i++) {
            if (this.view['destination']._value[i].LOGIN == this.login) {
                this.userService.delete_user(this.view['destination']._value[i].ID_USER)

            }
        }
        window.location.reload();
    }

    public onChange(event) {
        let roles = '';
        let id;
        let tel;
        for (let i = 0; i < this.view['destination']._value.length; i++) {
            if (this.view['destination']._value[i].LOGIN == this.login) {
                id = this.view['destination']._value[i].ID_USER
                // document.getElementById('changePhone').inputMode
                if (this.phone != undefined) {
                    this.view['destination']._value[i].TEL = tel = this.phone;
                } else {
                    tel = "";
                }
                if (this.onblock == true) {
                    this.view['destination']._value[i].USER_LOCK = 1;
                } else {
                    this.view['destination']._value[i].USER_LOCK = 0;
                }
                if (this.adm == true && this.man == true && this.ins == true) {
                    this.view['destination']._value[i].ROLELIST_RU = "Инспектор,Администратор,Руководитель"
                    this.view['destination']._value[i].ROLELIST = roles = "ins,adm,man"
                } else if (this.adm == true && this.ins == true) {
                    this.view['destination']._value[i].ROLELIST_RU = "Инспектор,Администратор"
                    this.view['destination']._value[i].ROLELIST = roles = "ins,adm"
                } else if (this.adm == true && this.man == true) {
                    this.view['destination']._value[i].ROLELIST_RU = "Администратор,Руководитель"
                    this.view['destination']._value[i].ROLELIST = roles = "adm,man"
                } else if (this.man == true && this.ins == true) {
                    this.view['destination']._value[i].ROLELIST_RU = "Инспектор,Руководитель"
                    this.view['destination']._value[i].ROLELIST = roles = "ins,man"
                } else if (this.adm == true) {
                    this.view['destination']._value[i].ROLELIST_RU = "Администратор"
                    this.view['destination']._value[i].ROLELIST = roles = "adm"
                } else if (this.ins == true) {
                    this.view['destination']._value[i].ROLELIST_RU = "Инспектор"
                    this.view['destination']._value[i].ROLELIST = roles = "ins"
                } else if (this.man == true) {
                    this.view['destination']._value[i].ROLELIST_RU = "Руководитель"
                    this.view['destination']._value[i].ROLELIST = roles = "man"
                } else {
                    this.view['destination']._value[i].ROLELIST_RU = ""
                    this.view['destination']._value[i].ROLELIST = roles = ""
                }
            }
            if (this.ondelete == true) {
                this.delete = true
            }
        }
        this.userService.set_role(roles, id);
        this.userService.set_tel(id, tel);
        if (this.password == true) {
            this.userService.passSetDef(id);
        }
        if (this.onblock == true && this.onblock != this.blokirator) {
            this.userService.userLock(1, id);
        } else if (this.onblock == false && this.onblock != this.blokirator) {
            this.userService.userLock(0, id);
        }
        window.location.reload()
    }


    public rowCallback = (context: RowClassArgs) => {
        switch (context.dataItem.USER_LOCK) {
            case 1:
                return {gold: true};
            case 0:
                return {green: true};
            default:
                return {};
        }
    }
}
