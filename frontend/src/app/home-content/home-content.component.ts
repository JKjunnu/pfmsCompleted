import { Component } from '@angular/core';
import { AxiosService } from '../axios.service';

@Component({
  selector: 'app-home-content',
  templateUrl: './home-content.component.html',
  styleUrls: ['./home-content.component.css']
})
export class HomeContentComponent {

  componentToShow: string = "welcome";

  constructor(private axiosService: AxiosService) { }

  showComponent(componentToShow: string): void {
    this.componentToShow = componentToShow;
  }

  onLogin(input: any): void {


    this.axiosService.request(
      "POST",
      "/login",
      {
        email: input.email,
        password: input.password
      }
    ).then(
      response => {
        this.axiosService.setAuthToken(response.data.token);
        this.componentToShow = "messages";
      }).catch(
        error => {
          this.axiosService.setAuthToken(null);
          this.componentToShow = "welcome";
        });

  }

  onRegister(input: any): void {


    this.axiosService.request(
      "POST",
      "/register",
      {
        firstName: input.firstName,
        lastName: input.lastName,
        email: input.email,
        password: input.password

      }
    ).then(
      response => {
        this.axiosService.setAuthToken(response.data.token);
        this.componentToShow = "messages";
      }).catch(
        error => {
          this.axiosService.setAuthToken(null);
          this.componentToShow = "welcome";
        }
      );

  }



}
