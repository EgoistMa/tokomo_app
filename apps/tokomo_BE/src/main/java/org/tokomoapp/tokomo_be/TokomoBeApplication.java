package org.tokomoapp.tokomo_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TokomoBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TokomoBeApplication.class, args);
		System.out.println("Server is running on port 8080");
	}

}
