import { Injectable } from '@angular/core';
import {j4care} from "./j4care.service";
import {Globalvar} from "../constants/globalvar";
import {J4careHttpService} from "./j4care-http.service";
import {AppService} from "../app.service";
import {Route, Router} from "@angular/router";

@Injectable()
export class PermissionService {

    user;
    uiConfig;
    constructor(private $http:J4careHttpService, private mainservice:AppService, private router: Router) { }

    getPermission(url){
        // console.log("permission user",this.mainservice.user.roles);
        if(this.mainservice.user && this.mainservice.user.roles){
            return this.checkSuperAdmin(url);
        }else
            return this.getConfigWithUser(()=>{
                if(this.mainservice.user && !this.mainservice.user.user && this.mainservice.user.roles && this.mainservice.user.roles.length === 0)
                    return true; //not secured
                else
                    if(this.mainservice.user.roles.indexOf(Globalvar.SUPER_ROOT) > -1)
                        return true;
                    else
                        return this.checkMenuTabAccess(url)
            });
    }
    checkSuperAdmin(url){
        if(this.mainservice.user.roles.indexOf(Globalvar.SUPER_ROOT) > -1)
            return true;
        else
            return this.getConfig(()=>{return this.checkMenuTabAccess(url)});
    }
    getConfig(response){
        if(!this.uiConfig)
            return this.$http.get('../devicename')
                .map(res => j4care.redirectOnAuthResponse(res))
                .switchMap(res => this.$http.get('../devices/' + res.dicomDeviceName))
                .map(res => res.json())
                .map((res)=>{
                    this.uiConfig = res.dcmDevice.dcmuiConfig["0"];
                    // return this.checkMenuTabAccess(url);
                    return response.apply(this,[]);
                });
        else
            return response.apply(this,[]);
    }
    getConfigWithUser(response){
        if(!this.uiConfig)
            return this.mainservice.getUserInfo()
                .map(user=>{
                    this.mainservice.user = user;
                    this.user = user;
                })
                .switchMap(res => this.$http.get('../devicename'))
                .map(res => j4care.redirectOnAuthResponse(res))
                .switchMap(res => this.$http.get('../devices/' + res.dicomDeviceName))
                .map(res => j4care.redirectOnAuthResponse(res))
                .map((res)=>{
                    this.uiConfig = res.dcmDevice.dcmuiConfig["0"];
                    // return this.checkMenuTabAccess(url);
                    return response.apply(this,[]);
                });
        else
            return response.apply(this,[]);
    }

    checkMenuTabAccess(url){
        let urlAction = Globalvar.LINK_PERMISSION(url);
        let checkObject = this.uiConfig.dcmuiPermission.filter(element=>{
            return urlAction && element.dcmuiAction === urlAction.permissionsAction && element.dcmuiActionParam.indexOf('accessible') > -1;
        });
        if(checkObject && checkObject[0]){
          let check = this.comparePermissionObjectWithRoles(checkObject);
          if(check && checkObject[0].dcmuiActionParam.indexOf('accessible') > -1)
            return true;
          else
              if(urlAction.nextCheck)
                  this.router.navigate([urlAction.nextCheck]);
          return false;
        }
        return false;
    }
    checkVisibility(permissionObject){
        if(this.mainservice.user && this.mainservice.user.roles && this.mainservice.user.roles.length > 0 && this.mainservice.user.roles.indexOf(Globalvar.SUPER_ROOT) > -1)
            return true;
        else
            return this.getConfig(()=>{
                let checkObject = this.uiConfig.dcmuiPermission.filter(element=>{
                    return element.dcmuiAction === permissionObject.id && element.dcmuiActionParam.indexOf(permissionObject.param) > -1;
                });
                return this.comparePermissionObjectWithRoles(checkObject);
            })
    }
    comparePermissionObjectWithRoles(object){
        try{
            let check = false;
            if(object[0])
                object[0].dcmAcceptedUserRole.forEach(role =>{
                    if(this.mainservice.user.roles.indexOf(role) > -1)
                        check = true;
                });
            return check;
        }catch (err){
            console.error("Error comparing permissions object with the roles",err);
            return false;
        }
    }

}
