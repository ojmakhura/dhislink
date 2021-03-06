import { Component, ViewChild, OnInit } from '@angular/core';
import { Idle, DEFAULT_INTERRUPTSOURCES } from '@ng-idle/core';
import { Keepalive } from '@ng-idle/keepalive';
import { Router } from '@angular/router';
import { AuthenticationService } from './service/authentication/authentication.service';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  title = 'Detection, Resulting and Verification';
  idleState = 'Not started.';
  timedOut = false;
  lastPing?: Date = null;
  user: string;
  loggedIn: boolean = false;
  isLoggedIn$: Observable<boolean>;

  public modalRef: BsModalRef;
  @ViewChild('childModal', { static: false }) childModal: ModalDirective;

  constructor(private idle: Idle,
              private keepalive: Keepalive,
              private authService: AuthenticationService,
              private router: Router) {
    // sets an idle timeout of 5 seconds, for testing purposes.
    idle.setIdle(300);
    // sets a timeout period of 5 seconds. after 10 seconds of inactivity, the user will be considered timed out.
    idle.setTimeout(240);
    // sets the default interrupts, in this case, things like clicks, scrolls, touches to the document
    idle.setInterrupts(DEFAULT_INTERRUPTSOURCES);

    idle.onIdleEnd.subscribe(() => {
      this.idleState = 'No longer idle.';
      console.log(this.idleState);
      this.reset();
    });

    idle.onTimeout.subscribe(() => {
      this.idleState = 'Timed out!';
      this.timedOut = true;
      this.logout();
      console.log(this.idleState);
      this.router.navigate(['/login']);
    });

    idle.onIdleStart.subscribe(() => {
        this.idleState = 'You\'ve gone idle!';
        console.log(this.idleState);
        this.childModal.show();
    });

    idle.onTimeoutWarning.subscribe((countdown) => {
      this.idleState = 'You will time out in ' + countdown + ' seconds!';
      console.log(this.idleState);
    });

    // sets the ping interval to 15 seconds
    this.keepalive.interval(15);

    this.keepalive.onPing.subscribe(() => this.lastPing = new Date());

    if (this.authService.getCurrentUser()) {

      idle.watch();
      this.timedOut = false;
    } else {
      idle.stop();
    }
  }

  ngOnInit() {
    this.isLoggedIn$ = of(this.authService.isLoggedIn()); // {2}
  }

  reset() {
    this.idle.watch();
    this.idleState = 'Started.';
    this.timedOut = false;
  }

  hideChildModal(): void {
    this.childModal.hide();
  }

  stay() {
    this.childModal.hide();
    this.reset();
  }

  logout() {
    this.childModal.hide();
    this.authService.logout();
    this.loggedIn = false;
    this.user = '';

    this.router.navigate(['/login']);
  }

  getUser() {

    this.user = this.authService.getCurrentUser();

    if (!this.user) {
      this.user = '';
      this.loggedIn = false;
    } else {
      this.loggedIn = true;
    }

    return this.user;
  }

  changeForm(destination: string) {
    this.router.navigate([destination]);
  }

  isLogggedIn() {
    this.getUser();
    return this.loggedIn;
  }
}

