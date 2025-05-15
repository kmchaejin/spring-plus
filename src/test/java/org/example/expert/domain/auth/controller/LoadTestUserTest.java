package org.example.expert.domain.auth.controller;

import static org.example.expert.domain.user.enums.UserRole.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

//@Sql(scripts = "classpath:/init_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // @BeforeAll 어노테이션 사용하려고 적용
@ActiveProfiles("test")
@SpringBootTest
public abstract class LoadTestUserTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserService userService;

	//private static MySQLContainer<?> container = new MySQLContainer<>("mysql:8");

	@BeforeAll
	void insertUserData() {
		//랜덤 닉네임 로딩
		List<String> adjectives = new ArrayList<>();
		List<String> characters = new ArrayList<>();
		List<String> codes = new ArrayList<>();

		Random random = new Random();

		try (
			InputStream adjStream = ClassLoader.getSystemResourceAsStream("strings/adjectives.txt");
			BufferedReader adjReader = new BufferedReader(new InputStreamReader(adjStream));

			InputStream chrStream = ClassLoader.getSystemResourceAsStream("strings/chracters.txt");
			BufferedReader chrReader = new BufferedReader(new InputStreamReader(chrStream));

			InputStream codeStream = ClassLoader.getSystemResourceAsStream("strings/codes.txt");
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

			users.add(new User(i + "@example.com", "pw", USER,
				adjectives.get(adjIndex) + characters.get(chrIndex) + codes.get(codeIndex)));

			if (i % 1000 == 0) {
				userRepository.saveAll(users); // 내부적으로 각 user를 반복실행하므로 배치사이즈 설정 필요
				users.clear(); // 리스트에 최대 1000개만 들어가도록
			}
		}
	}

	@Test
	public void 닉네임으로_유저조회(){
		// given
		String nickname = "졸린잠만보200";

		// when
		List<User> uesrs = userService.findByNickname(nickname);

		// then

	}
}