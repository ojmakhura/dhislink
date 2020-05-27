import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule }   from '@angular/forms';
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


@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    VerificationComponent,
    LocationComponent,
    TestingDetectionComponent
  ],
  imports: [
    CommonModule,
    MatTabsModule,
    MatListModule,
    MatSidenavModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatTableModule,
    MatSelectModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpClientModule
  ],
  exports: [
      MatTabsModule,
      MatSidenavModule,
      MatToolbarModule,
      MatButtonModule,
      MatIconModule,
      MatMenuModule,
      MatDatepickerModule,
      MatFormFieldModule,
      MatInputModule,
      MatCheckboxModule,
      MatTableModule,
      MatSelectModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    AuthenticationService,
    AuthenticationResponse,
    LoginComponent,
    RedcapAuth
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
