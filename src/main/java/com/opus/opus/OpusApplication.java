package com.opus.opus;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpusApplication {

	private static final String ZONE = "Asia/Seoul";

	// 배포 환경 설정과 관계없이 JVM 기본 시간대를 KST로 고정한다
	@PostConstruct
	protected void init() {
		TimeZone.setDefault(TimeZone.getTimeZone(ZONE));
	}

	public static void main(String[] args) {
		SpringApplication.run(OpusApplication.class, args);
	}

}
