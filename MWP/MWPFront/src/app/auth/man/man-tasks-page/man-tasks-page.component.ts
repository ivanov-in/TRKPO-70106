import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {GridDataResult, RowClassArgs, SelectableSettings} from '@progress/kendo-angular-grid';
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {process, State} from '@progress/kendo-data-query';
import {ILoadEvent} from 'angular8-yandex-maps';
import {map} from "rxjs/operators";
import {ManService} from "../../../data/services/man.service";
import {Observable} from "rxjs";
import {UserService} from "../../../data/services/user.service";


class Point {
    constructor(
        public lat: number,
        public long: number
    ) {
    }

    public get feature() {
        return [this.lat, this.long];
    }

    public get options() {
        return {
            draggable: true
        };
    }
}

interface IUser {
    FIO: string,
    ID: number
}

interface IObj {
    PAYER_NAME: string,
    NDOG: number
}

interface IAdr {
    address: string,
    coord: {
        lat: string,
        long: string
    }
}

@Component({
    selector: 'app-man-tasks-page',
    templateUrl: './man-tasks-page.component.html',
    styleUrls: ['./man-tasks-page.component.css'],
})
export class ManTasksPageComponent implements OnInit {
    public points: Point[] = [];

    constructor(private changeDetectorRef: ChangeDetectorRef, private formBuilder: FormBuilder, private manService: ManService, private userService: UserService) {
        this.setSelectableSettings(null);
    }

    public view: Observable<GridDataResult>;
    public viewListofTask: Observable<GridDataResult> = new Observable<GridDataResult>()
    public selectedInspToChange: any;
    public selectedItem: IAdr;
    public ndFalse = false;
    public text = '';
    public selectedName: IUser;
    public selectedObj: IObj;
    public selectedDog: IObj;
    public listObjASUSE: any[] = [];
    public selectedAim: any;
    public yMaps: any = null;
    public listItems: Array<IAdr> = [];
    public selectedObjASUSE: any;
    public listAim = ['Технический аудит', 'Присоединение', 'Контроль ПУ'];
    public listInsp: Array<IUser> = [];
    public listObj: Array<IObj> = [];
    public disabled = true;
    public disabledSaveChange = true;
    public emptyTask = false
    public emptyDOG: boolean;
    public gridState: State = {
        sort: [],
        skip: 0,
        take: 25
    };
    public inspRoutesArrArr = [{
        inspRoutesArr: [{
            coord: ['55.784390, 49.120760'],
            addr: ['ул. Марселя Салимжанова, 1, Казань']
        }]
    }];
    public valueData: Date = new Date();
    public min: Date = new Date(2020, 0, 1);
    public max: Date = new Date(new Date().getFullYear() + 1, new Date().getMonth() + 12, new Date().getDate() + 30);
    public registerForm: FormGroup = new FormGroup({
        date: new FormControl(new Date()),
        combo: new FormControl(),
        comboEdit: new FormControl(),
        comboAim: new FormControl(),
        comboPurpose: new FormControl(),
        comboInsp: new FormControl(),
        comboAdr: new FormControl(),
        comboObj: new FormControl(),
        comboChangeInsp: new FormControl(),
        emptyDOG: new FormControl(),
        comboDogovor: new FormControl(),
        comboAbon: new FormControl()
    });

    public checkboxOnly = false;
    public selectableSettings: SelectableSettings;
    public opened = false;
    public openedChange = false;
    public selectedInsp: any = null;
    public clusterer = {
        preset: 'islands#invertedVioletClusterIcons',
        hasBaloon: false
    };
    public canDeselect = true;
    public selectUsers: string[] = [];

    public async ngOnInit(): Promise<void> {

        this.view = this.manService.pipe(map(
            data => process(data, this.gridState)));
        this.manService.read();
        this.viewListofTask = this.userService.pipe(map(
            data => process(data, this.gridState)));
        this.userService.readTask(new Date().toLocaleDateString());

    }

    public indexClickHandler({sender, rowIndex, columnIndex, dataItem, isEdited}) {
        if (!isEdited) {
            sender.editCell(rowIndex, columnIndex, this.createFormGroup(dataItem));
        }
    }

    public createFormGroup(dataItem: any): FormGroup {
        return this.formBuilder.group({
            time: dataItem.time
        });
    }

    public assignValues(target: any, source: any): void {
        Object.assign(target, source);
    }

    public indexCloseHandler(args: any) {
        const {formGroup, dataItem} = args;
        if (!formGroup.valid) {
            args.preventDefault();
        } else if (formGroup.dirty) {
            this.assignValues(dataItem, formGroup.value);
            this.setRoutes();
        }
    }

    public setDistance(event) {
        let distance = 0;
        let strDistanse;
        for (const i of event.instance._pointsDragController.multiRouteJsonView._activeRoute.model._jsonModel._json.features) {
            if (parseFloat(i.features[0].properties.text.replace(/,/gi, '.').split(' км')[0]) > 99) {
                distance = distance + parseFloat(i.features[0].properties.text.replace(/,/gi, '.').split(' км')[0]) / 1000;
            } else {
                distance = distance + parseFloat(i.features[0].properties.text.replace(/,/gi, '.').split(' км')[0]);
            }
            strDistanse = distance.toFixed(1);
        }
        for (const i of this.view['destination']._value) {
            if (i.ID_USER === this.selectUsers[0]) {
                i.distance = strDistanse + ' км';
            }
        }
    }

    public selectUsersClick(event) {
        if (this.canDeselect) {
            this.canDeselect = true;
            this.selectUsers = [];
            this.setFilterTask();
            return;
        } else {
            this.setFilterTask();
        }
        this.canDeselect = true;

    }

    public setFilterTask() {
        const manTaskNameArr = [];
        if (this.selectUsers.length > 0) {
            for (const item of this.viewListofTask['destination']._value) {
                if (item.ID_INSPECTOR === this.selectUsers[0]) {
                    manTaskNameArr.push(item);
                }
            }
            this.setRoutes();
            this.viewListofTask['destination']._value = manTaskNameArr;
            this.viewListofTask = this.userService.pipe(map(
                data => process(manTaskNameArr, this.gridState)));
        } else {
            this.viewListofTask = this.userService.pipe(map(
                data => process(data, this.gridState)));
            this.userService.readTask(new Date().toLocaleDateString());
            this.inspRoutesArrArr = [{
                inspRoutesArr: [{
                    coord: ['55.784390, 49.120760'],
                    addr: ['ул. Марселя Салимжанова, 1, Казань']
                }]
            }];
        }
    }

    public setRoutes() {
        const arrSortIndex = this.viewListofTask['destination']._value.sort((a, b) => a.index - b.index).map((example, index, array) => this.viewListofTask['destination']._value);
        if (arrSortIndex.length > 0) {
            this.viewListofTask['destination']._value = arrSortIndex[0];
        }
        const routesArr = [{
            inspRoutesArr: [{
                coord: ['55.784390, 49.120760'],
                addr: ['ул. Марселя Салимжанова, 1, Казань']
            }]
        }];
        if (this.selectUsers.length > 0) {
            for (const item of this.viewListofTask['destination']._value) {
                if (item.ID_INSPECTOR === this.selectUsers[0]) {
                    routesArr[0].inspRoutesArr[0].coord.push(String(item.LAT + ',' + item.LAN));
                    routesArr[0].inspRoutesArr[0].addr.push(item.ADR);
                }
            }
            this.inspRoutesArrArr = routesArr;
        } else {
            this.inspRoutesArrArr = [{
                inspRoutesArr: [{
                    coord: ['55.784390, 49.120760'],
                    addr: ['ул. Марселя Салимжанова, 1, Казань']
                }]
            }];
        }
        for (let task of this.viewListofTask['destination']._value) {
            this.points.push(new Point(task.LAT, task.LAN));
        }
        this.changeDetectorRef.checkNoChanges()
    }


    public setSelectableSettings(event): void {
        if (event === null) {
            this.selectedInsp = null;
            return;
        } else if (event.selectedRows.length === 0) {
            this.selectedInsp = null;
        } else {
            this.selectedInsp = event.selectedRows[0].dataItem.ID_USER;
        }
        this.selectableSettings = {
            checkboxOnly: this.checkboxOnly,
            mode: 'single'
        };
        this.canDeselect = false;
        this.points = []
    }

    public comboClear(): void {
        this.disabled = true;
    }

    public comboSaveClear(): void {
        this.disabledSaveChange = true;
    }

    public comboChange(): void {
        if (this.selectedItem === undefined) {
            this.disabled = true;
        } else {
            this.disabled = this.selectedItem.address.length === 0;
        }
    }

    public comboSaveChange(): void {
        if (this.selectedItem === undefined) {
            this.disabledSaveChange = true;
        } else {
            this.disabledSaveChange = this.selectedItem.address.length === 0;
        }
    }

    public onLoad(event: ILoadEvent) {
        this.yMaps = event.ymaps;
        this.yMaps.geocode('ул. Звенигородская')
            .then((res) => {
                console.log(
                    res.geoObjects.get(0).properties.get('metaDataProperty')
                );
                console.log(
                    res.geoObjects.get(1).properties.get('metaDataProperty')
                );
            });
    }

    public onSearchObjASUSE(event) {
        // if (this.text === '') {
        //     this.text = event.key
        // } else {
        //     this.text = this.text + event.key
        // }
        // if(this.text.length > 3) {
        //     this.listObjASUSE = this.manService.pipe(map(
        //         data => process(data, this.gridState)));
        //     this.manService.getObj(this.text);
        // }
    }

    public onkeydown(event) {
        this.ndFalse = false
        this.yMaps.geocode('Россия, Республика Татарстан, Казань,' + event.target.value)
            .then((res) => {
                this.listItems = [];
                this.listItems.push({
                    address: res.geoObjects.get(0).properties.get('metaDataProperty').GeocoderMetaData.Address.formatted,
                    coord: {
                        lat: res.geoObjects.get(0).geometry._coordinates[0],
                        long: res.geoObjects.get(0).geometry._coordinates[1]
                    }
                });
                this.listItems.push({
                    address: res.geoObjects.get(1).properties.get('metaDataProperty').GeocoderMetaData.Address.formatted,
                    coord: {
                        lat: res.geoObjects.get(1).geometry._coordinates[0],
                        long: res.geoObjects.get(1).geometry._coordinates[1]
                    }
                });
                this.listItems.push({
                    address: res.geoObjects.get(2).properties.get('metaDataProperty').GeocoderMetaData.Address.formatted,
                    coord: {
                        lat: res.geoObjects.get(2).geometry._coordinates[0],
                        long: res.geoObjects.get(2).geometry._coordinates[1]
                    }
                });
                this.listItems.push({
                    address: res.geoObjects.get(3).properties.get('metaDataProperty').GeocoderMetaData.Address.formatted,
                    coord: {
                        lat: res.geoObjects.get(3).geometry._coordinates[0],
                        long: res.geoObjects.get(3).geometry._coordinates[1]
                    }
                });
                this.listItems.push({
                    address: res.geoObjects.get(4).properties.get('metaDataProperty').GeocoderMetaData.Address.formatted,
                    coord: {
                        lat: res.geoObjects.get(4).geometry._coordinates[0],
                        long: res.geoObjects.get(4).geometry._coordinates[1]
                    }
                });
            });
    }

    public onNewTask(event) {
        if (this.selectedItem.address.split(',').length >= 5) {
            let adr = this.selectedItem.address;
            let city;
            let house;
            let street;
            let nd;
            if (this.selectedItem.address.split(',').length == 5) {
                city = this.selectedItem.address.split(',')[2];
                street = this.selectedItem.address.split(',')[3];
                if (this.selectedItem.address.split(',')[4].split('к').length >= 2) {
                    house = this.selectedItem.address.split(',')[4].split('к')[1]
                    nd = this.selectedItem.address.split(',')[4].split('к')[0]
                } else {
                    house = null
                    nd = this.selectedItem.address.split(',')[4].split('к')[0]
                }
            } else if (this.selectedItem.address.split(',').length > 5) {
                city = this.selectedItem.address.split(',')[2];
                street = this.selectedItem.address.split(',')[4];
                if (this.selectedItem.address.split(',')[5].split('к').length >= 2) {
                    house = this.selectedItem.address.split(',')[5].split('к')[1]
                    nd = this.selectedItem.address.split(',')[5].split('к')[0]
                } else {
                    house = null
                    nd = this.selectedItem.address.split(',')[5].split('к')[0]
                }
            }
            let purpose;
            if (this.selectedAim == 'Технический аудит') {
                purpose = 1
            } else if (this.selectedAim == "Присоединение") {
                purpose = 2
            } else if (this.selectedAim == "Контроль ПУ") {
                purpose = 3
            } else {
                purpose = ''
            }
            let prim = '';
            let ttime = this.valueData;
            let id_ins;
            if (this.selectedInsp !== null) {
                id_ins = this.selectedInsp.ID;
            } else {
                id_ins = ''
            }
            let lat = this.selectedItem.coord.lat;
            let lan = this.selectedItem.coord.long;
            let s_zulu = null;
            let b_zullu = null;
            let ststus = 1;

            this.manService.addTask(adr, city, street, house, nd, purpose, prim, ttime, id_ins, localStorage.getItem('email'), lat, lan, s_zulu, b_zullu, ststus);

            const coords = [this.selectedItem.coord.lat, this.selectedItem.coord.long];
            this.points.push(new Point(coords['lat'], coords['long']));
            this.changeDetectorRef.detectChanges();
            this.setFilterTask();
            this.opened = false;
        } else {
            this.opened = true;
            this.ndFalse = true
        }
    }

    public getInsp(event, dataItem) {
        this.selectedInspToChange = dataItem;
        let i = 0;
        while (i < this.view['destination']._value.length) {
            this.listInsp.push({
                FIO: this.view['destination']._value[i].FIO,
                ID: this.view['destination']._value[i].ID_USER
            })
            i++
        }
    }

    public onChangeTask(event) {
        if (this.selectedItem.address.split(',').length >= 5) {
            let adr = this.selectedItem.address;
            let city;
            let house;
            let street;
            let nd;
            if (this.selectedItem.address.split(',').length == 5) {
                city = this.selectedItem.address.split(',')[2].trim();
                street = this.selectedItem.address.split(',')[3];
                if (this.selectedItem.address.split(',')[4].split('к').length >= 2) {
                    house = this.selectedItem.address.split(',')[4].split('к')[1]
                    nd = this.selectedItem.address.split(',')[4].split('к')[0]
                } else {
                    house = null
                    nd = this.selectedItem.address.split(',')[4].split('к')[0]
                }
            } else if (this.selectedItem.address.split(',').length > 5) {
                city = this.selectedItem.address.split(',')[2];
                street = this.selectedItem.address.split(',')[4];
                if (this.selectedItem.address.split(',')[5].split('к').length >= 2) {
                    house = this.selectedItem.address.split(',')[5].split('к')[1]
                    nd = this.selectedItem.address.split(',')[5].split('к')[0]
                } else {
                    house = null
                    nd = this.selectedItem.address.split(',')[5].split('к')[0]
                }
            }
            let purpose;
            if (this.selectedAim == 'Технический аудит') {
                purpose = 1
            } else if (this.selectedAim == "Присоединение") {
                purpose = 2
            } else if (this.selectedAim == "Контроль ПУ") {
                purpose = 3
            } else {
                purpose = ''
            }
            let prim = '';
            let ttime = this.valueData;
            let id_ins;
            if (this.selectedName !== null) {
                id_ins = this.selectedName['ID'];
            } else {
                id_ins = ''
            }
            let lat = this.selectedItem.coord.lat;
            let lan = this.selectedItem.coord.long;
            let s_zulu = null;
            let b_zullu = null;
            let ststus = 1;

            this.manService.changeTask(adr, city, street, house, nd, purpose, prim, ttime, id_ins, lat, lan, s_zulu, b_zullu, ststus);
            const coords = [this.selectedItem.coord.lat, this.selectedItem.coord.long];
            this.points.push(new Point(coords['lat'], coords['long']));
            this.changeDetectorRef.detectChanges();
            this.setFilterTask();
            this.opened = false;
        } else {
            this.opened = true;
            this.ndFalse = true
        }
        this.emptyDOG = false
    }

    public onFilter(inputValue: string): void {
        this.viewListofTask['destination']._value = process(this.viewListofTask['destination']._value, {
            filter: {
                logic: 'or',
                filters: [
                    {
                        field: 'FIO',
                        operator: 'contains',
                        value: inputValue
                    },
                ],
            }
        }).data;
    }

    public close(status) {
        console.log(`Dialog result: ${status}`);
        if (this.ndFalse == false) {
            this.opened = false;
        }
    }

    public open(event) {
        this.opened = true;
    }

    public openChange(dataItem) {
        let i = 0
        while (i < this.viewListofTask['destination']._value.length) {
            this.listObj.push({
                PAYER_NAME: this.viewListofTask['destination']._value[i].PAYER_NAME,
                NDOG: this.viewListofTask['destination']._value[i].NDOG
            })
            this.listObjASUSE.push(this.viewListofTask['destination']._value[i].ADR
            )
            i++
        }
        if (this.selectedInspToChange.PURPOSE == 1) {
            this.selectedAim = this.listAim[0]
        } else if (this.selectedInspToChange.PURPOSE == 2) {
            this.selectedAim = this.listAim[1]
        } else if (this.selectedInspToChange.PURPOSE == 3) {
            this.selectedAim = this.listAim[2]
        }
        var r = this.listInsp.filter(value =>
            value.ID === this.selectedInspToChange.ID_INSPECTOR
        )
        this.selectedName = r[0]
        var m = this.listObjASUSE.filter(value =>
            value === this.selectedInspToChange.ADR
        )
        this.selectedObjASUSE = m[0]
        var l = this.listObj.filter(value =>
            value.NDOG === this.selectedInspToChange.NDOG
        )
        this.selectedObj = this.selectedDog = l[0]
        this.listItems.push({
            address: this.selectedInspToChange.ADR,
            coord: {
                lat: this.selectedInspToChange.LAT,
                long: this.selectedInspToChange.LAN
            }
        })
        this.selectedItem = this.listItems[0]

        this.openedChange = true;
        this.emptyDOG = false
    }

    public closeChange(status) {

        console.log(`Dialog result: ${status}`);
        this.openedChange = false;
        this.text = ''
    }

    public onDatePicker(event) {
        this.viewListofTask = this.userService.pipe(map(
            data => process(data, this.gridState)));
        this.userService.readTask(event.toLocaleDateString());
    }

    public changeButton(event, dataItem) {
        if (dataItem.PURPOSE !== undefined && dataItem.PURPOSE !== null) {
            dataItem.STATUS = 2;
        } else {
            if (dataItem['destination']._value[0] !== undefined) {
                let i = 0;
                while (i < dataItem['destination']._value.length) {
                    if (dataItem['destination']._value[i].STATUS === 1 && dataItem['destination']._value[i].PURPOSE !== undefined && dataItem['destination']._value[i].PURPOSE.length !== 0) {
                        dataItem['destination']._value[i].STATUS = 2;
                    }
                    i++;
                }
            } else {
                alert('Введите цель задания');
            }
        }
    }


    public rowCallback = (context: RowClassArgs) => {
        this.emptyTask = context.dataItem.NDOG === null || context.dataItem.PAYER_NAME === null
        switch (context.dataItem.NDOG || context.dataItem.PAYER_NAME) {
            case null:
                return {invalidinsp: true};
            default:
                return {validinsp: true};
        }
    }
    public rowDisable = (context: RowClassArgs) => {
        switch (context.dataItem.DATE_UDAL != null || context.dataItem.USER_LOCK == 1) {
            case true:
                return {gold: true};
            case false:
                return {green: true};
            default:
                return {};
        }
    }
}
