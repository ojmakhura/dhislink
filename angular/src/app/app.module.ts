import { BrowserModule } from '@angular/platform-browser';
import { NgModule, ErrorHandler } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatTabsModule} from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthenticationResponse } from './model/authentication/authentication-response';
import { AuthenticationService } from './service/authentication/authentication.service';
import { RedcapAuth } from './model/authentication/redcap-auth';
import { VerificationComponent } from './view/verification/verification.component';
import { LocationComponent } from './view/location/location.component';
import { JwtInterceptor } from './helpers/jwt-interceptor';
import { TestingDetectionComponent } from './view/testing-detection/testing-detection.component';
import { ResultingComponent } from './view/resulting/resulting.component';
import { LocationService } from './service/location/location.service';
import { RedcapDataService } from './service/data/redcap-data.service';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatSortModule } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthErrorHandler } from './helpers/auth-error-handler';
import { RequestOptions } from '@angular/http';
import { AuthRequestOptions } from './helpers/auth-request-options';
import { LoginComponent } from './view/login/login.component';
import { NgForm } from '@angular/forms';
import { NgIdleKeepaliveModule } from '@ng-idle/keepalive';
import { MomentModule } from 'angular2-moment';
import { ModalModule } from 'ngx-bootstrap/modal';
import { DialogBoxComponent } from './view/dialog-box/dialog-box.component';
import { RxReactiveFormsModule } from '@rxweb/reactive-form-validators';
import { BatchComponent } from './view/batch/batch.component';
import { LabReportComponent } from './view/lab-report/lab-report.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    VerificationComponent,
    ResultingComponent,
    LocationComponent,
    TestingDetectionComponent,
    DialogBoxComponent,
    LabReportComponent
  ],
  imports: [
    MatButtonModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatRippleModule,
    MatSelectModule,
    MatSidenavModule,
    MatSortModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    MatProgressBarModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    RxReactiveFormsModule,
    NgIdleKeepaliveModule.forRoot(),
    MomentModule,
    ModalModule.forRoot()
  ],
  exports: [
    MatButtonModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatRippleModule,
    MatSelectModule,
    MatSidenavModule,
    MatSortModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    //{ provide: RequestOptions,  useClass: AuthRequestOptions },
    AuthenticationService,
    AuthenticationResponse,
    LoginComponent,
    LocationService,
    RedcapDataService,
    RedcapAuth,
    DialogBoxComponent,
    { provide: ErrorHandler, useClass: AuthErrorHandler }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { 
  
}
