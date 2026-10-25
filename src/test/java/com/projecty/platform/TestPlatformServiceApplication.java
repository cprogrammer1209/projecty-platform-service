package com.projecty.platform;

import org.springframework.boot.SpringApplication;

public class TestPlatformServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(PlatformServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
