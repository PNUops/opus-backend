package com.opus.opus;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpusApplication {

	private static final String ZONE = "Asia/Seoul";

	// 투표 기간 등 시각 비교를 서버 환경 설정과 관계없이 KST 기준으로 맞춘다
	@PostConstruct
	protected void init() {
		TimeZone.setDefault(TimeZone.getTimeZone(ZONE));
	}

	public static void main(String[] args) {
		SpringApplication.run(OpusApplication.class, args);
	}

}
