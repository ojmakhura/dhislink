import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { LocationComponent } from './view/location/location.component';
import { TestingDetectionComponent } from './view/testing-detection/testing-detection.component';
import { ResultingComponent } from './view/resulting/resulting.component';
import { VerificationComponent } from './view/verification/verification.component';

const routes: Routes = [
  {path: 'login', component: LoginComponent},
  {path: 'location', component: LocationComponent},
  {path: 'detection', component: TestingDetectionComponent},
  {path: 'resulting', component: ResultingComponent},
  {path: 'detection', component: VerificationComponent}
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forRoot(routes)
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }