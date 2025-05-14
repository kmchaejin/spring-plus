package org.example.expert.domain.auth.controller;

import static org.example.expert.domain.user.enums.UserRole.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // @BeforeAll 어노테이션 사용하려고 적용
@ActiveProfiles("test") // 그냥 트랜잭셔널 어노테이션 쓰면 테스트 종료 후에 DB 원복 되지 않나
@SpringBootTest
@Sql("classpath:/init_table.sql") // 이 클래스 로딩될 때 실행하는 어노테이션인가?
class AuthControllerTest {

	@Autowired
	AuthService authService;

	@Autowired
	UserRepository userRepository;

	@PersistenceContext
	EntityManager entityManager;

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@BeforeAll
	void insertUserData() {
		// given
		// 랜덤 닉네임 로딩
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

		int adjSize = adjectives.size();
		int chrSize = characters.size();
		int codeSize = codes.size();

		// user 데이터 준비
		List<User> users = new ArrayList<>();

		for (Long i = 1L; i <= 1000000; i++) {
			int adjIndex = random.nextInt(adjSize);
			int chrIndex = random.nextInt(chrSize);
			int codeIndex = random.nextInt(codeSize);

			entityManager.persist(new User(i, i + "@example.com", "pw", USER,
				adjectives.get(adjIndex) + characters.get(chrIndex) + codes.get(codeIndex)));

			if(i % 1000 == 0) { // 1000건마다 DB 반영 및 영속성 컨텍스트 초기화(메모리 부족 문제 방지)
				entityManager.flush();
				entityManager.clear();
				//userRepository.saveAll(users); 내부적으로 save를 반복 실행하므로 이 경우는 properties에 batchsize 설정 필요
			}
		}
	}


	@Test
	void 유저_100만건_생성() {

	}
}