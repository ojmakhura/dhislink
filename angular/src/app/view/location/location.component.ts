import { Component, OnInit, ViewChild } from '@angular/core';
import { LocationVO } from 'src/app/model/location/location-vo';
import { Router } from '@angular/router';
import { AuthenticationService, CURRENT_ROUTE } from 'src/app/service/authentication/authentication.service';
import { LocationService } from 'src/app/service/location/location.service';
import { MatTabGroup } from '@angular/material/tabs';

@Component({
  selector: 'app-location',
  templateUrl: './location.component.html',
  styleUrls: ['./location.component.css']
})
export class LocationComponent implements OnInit {

  locationVO: LocationVO;
  locations: LocationVO[];
  searchText: string;
  selectedIndex: number = 0;

  @ViewChild('tabGroup') private tabGroup: MatTabGroup;

  constructor(private router: Router, 
              private authService: AuthenticationService,
              private locationService: LocationService) {
    
    this.locationVO = new LocationVO();
    this.locations = [];
    this.searchText = '';
    localStorage.setItem(CURRENT_ROUTE, '/location');
  }

  ngOnInit(): void {
    
    if(this.authService.isTokenExpired(this.authService.getToken())) {
      
      this.router.navigate(['/login']);
    }
  }

  search() {

    if(this.searchText && this.searchText.length > 0) {

      this.locationService.searchByName(this.searchText).subscribe(data => {
        this.locations = data;
      });
    } else {
      this.locationService.findAll().subscribe(results => {
        this.locations = results;
      })
    }
  }

  clearSearch() {
    this.searchText = '';
    this.locations = [];
  }

  save() {
    
    if(!this.locationVO.id || this.locationVO.id === undefined) {
      
      this.locationService.createLocation(this.locationVO).subscribe(loc => {
        console.log(loc);
      });
    } else {
      
      this.locationService.updateLocation(this.locationVO);
    }
  }

  new() {
    this.locationVO = new LocationVO();
  }

  onSearchFieldChange(event) {
    this.searchText = event.target.value;
    this.search();
  }

  editLocation(id: number) {
    this.locationService.findById(id).subscribe(data => {
      this.locationVO = data;
    });
  }
}
