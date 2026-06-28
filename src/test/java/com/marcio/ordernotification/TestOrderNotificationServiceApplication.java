package com.marcio.ordernotification;

import org.springframework.boot.SpringApplication;

public class TestOrderNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderNotificationServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
