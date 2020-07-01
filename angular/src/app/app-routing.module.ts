import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';
import { LocationComponent } from './view/location/location.component';
import { TestingDetectionComponent } from './view/testing-detection/testing-detection.component';
import { ResultingComponent } from './view/resulting/resulting.component';
import { VerificationComponent } from './view/verification/verification.component';
import { AuthGuard } from './helpers/auth-guard';
import { LoginComponent } from './view/login/login.component';

const routes: Routes = [
  {path: '', redirectTo: '/detection', pathMatch: 'full', canActivate: [AuthGuard]},
  {path: 'login', component: LoginComponent},
  {path: 'location', component: LocationComponent, canActivate: [AuthGuard]},
  {path: 'detection', component: TestingDetectionComponent, canActivate: [AuthGuard]},
  {path: 'resulting', component: ResultingComponent, canActivate: [AuthGuard]},
  {path: 'verification', component: VerificationComponent, canActivate: [AuthGuard]}
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forRoot(routes)
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
