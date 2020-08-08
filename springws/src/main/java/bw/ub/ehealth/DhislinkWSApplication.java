package bw.ub.ehealth;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(
		scanBasePackages = {"bw.ub.ehealth"}
		//,exclude = { SecurityAutoConfiguration.class }
)
public class DhislinkWSApplication {

	public static void main(String[] args) {
		SpringApplication.run(DhislinkWSApplication.class, args);
	}
	
	@PostConstruct
	void started() {
	  TimeZone.setDefault(TimeZone.getTimeZone("Africa/Gaborone"));
	}

}
