import { BrowserModule } from '@angular/platform-browser';
import { NgModule, ErrorHandler } from '@angular/core';
import { FormsModule, ReactiveFormsModule }   from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatTabsModule} from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table'  
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

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthenticationResponse } from './model/authentication/authentication-response';
import { LoginComponent } from './login/login.component';
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
import { MatDialogModule } from '@angular/material/dialog';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatSortModule } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthErrorHandler } from './helpers/auth-error-handler';
import { RequestOptions } from '@angular/http';
import { AuthRequestOptions } from './helpers/auth-request-options';


@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    VerificationComponent,
    ResultingComponent,
    LocationComponent,
    TestingDetectionComponent
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
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule
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
    { provide: ErrorHandler, useClass: AuthErrorHandler }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
