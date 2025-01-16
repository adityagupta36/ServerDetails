package com.aditya.Server_Details;

import com.aditya.Server_Details.service.smps_service.SMPS_Service2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ServerProcessMonitoringSystemApplication {

	public static void main(String[] args) {
		ApplicationContext co = SpringApplication.run(ServerProcessMonitoringSystemApplication.class, args);
	}

}
