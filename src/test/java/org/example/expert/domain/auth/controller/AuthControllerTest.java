package org.example.expert.domain.auth.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Random;

@ActiveProfiles("test")
@SpringBootTest
@Sql("classpath:/init_table.sql")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@MockBean

	@InjectMocks
	AuthService authService;

	@Test
	void 유저_100만건_생성() {
		// given - user data 생성
		// 랜덤 닉네임 요소 로딩
		List<String> adjectives = new ArrayList<>();
		List<String> characters = new ArrayList<>();
		List<String> codes = new ArrayList<>();

		Random random = new Random();

		try (
			InputStream adjStream = ClassLoader.getSystemResourceAsStream("adjectives.txt");
			BufferedReader adjReader = new BufferedReader(new InputStreamReader(adjStream));

			InputStream chrStream = ClassLoader.getSystemResourceAsStream("characters.txt");
			BufferedReader chrReader = new BufferedReader(new InputStreamReader(chrStream));

			InputStream codeStream = ClassLoader.getSystemResourceAsStream("codes.txt");
			BufferedReader codeReader = new BufferedReader(new InputStreamReader(codeStream))) {

			String str;
			while ((str = adjReader.readLine()) != null) {
				adjectives.add(str.trim());
			}

			while ((str = chrReader.readLine()) != null) {
				characters.add(str.trim());
			}

			while ((str = codeReader.readLine()) != null) {
				codes.add(str.trim());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// when - 100만 건 insert
		for (int i = 0; i < 1000000; i++) {
			int adjIndex = random.nextInt(adjectives.size());
			int chrIndex = random.nextInt(characters.size());
			int codeIndex = random.nextInt(codes.size());

			authService.signup(new SignupRequest(i + "@example.com", "pw", "USER",
				adjectives.get(adjIndex) + characters.get(chrIndex) + codes.get(codeIndex)));
		}

		// then -
	}
}