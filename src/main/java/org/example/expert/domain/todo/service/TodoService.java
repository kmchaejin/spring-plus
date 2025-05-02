package org.example.expert.domain.todo.service;

import java.time.LocalDate;
import java.util.List;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {
	@PersistenceContext
	private EntityManager em;

	private final TodoRepository todoRepository;
	private final WeatherClient weatherClient;

	public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
		User user = User.fromAuthUser(authUser);

		String weather = weatherClient.getTodayWeather();

		Todo newTodo = new Todo(
			todoSaveRequest.getTitle(),
			todoSaveRequest.getContents(),
			weather,
			user
		);
		Todo savedTodo = todoRepository.save(newTodo);

		return new TodoSaveResponse(
			savedTodo.getId(),
			savedTodo.getTitle(),
			savedTodo.getContents(),
			weather,
			new UserResponse(user.getId(), user.getEmail())
		);
	}

	@Transactional(readOnly = true)
	public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDate modifiedFrom,
		LocalDate modifiedTo) {

		Pageable pageable = PageRequest.of(page - 1, size);

		StringBuilder sb = new StringBuilder("SELECT t FROM Todo t WHERE 1=1 ");
		StringBuilder sb2 = new StringBuilder("SELECT COUNT(t) FROM Todo t Where 1=1 ");

		if (!StringUtils.isBlank(weather)) {
			sb.append("AND t.weather = :weather ");
		}

		if (modifiedFrom != null) {
			sb.append("AND t.modifiedAt >= :modifiedFrom ");
		}

		if (modifiedTo != null) {
			sb.append("AND t.modifiedAt <= :modifiedTo");
		}

		TypedQuery<Todo> query = em.createQuery(sb.toString(), Todo.class);
		TypedQuery<Long> countQuery = em.createQuery(sb2.toString(), Long.class);

		if (!StringUtils.isBlank(weather)) {
			query.setParameter("weather", weather);
			countQuery.setParameter("weather", weather);
		}

		if (modifiedFrom != null) {
			query.setParameter("modifiedFrom", modifiedFrom);
			countQuery.setParameter("modifiedFrom", modifiedFrom);

		}

		if (modifiedTo != null) {
			query.setParameter("modifiedTo", modifiedTo.atTime(23, 59, 59));
			countQuery.setParameter("modifiedTo", modifiedTo.atTime(23, 59, 59));
		}

		List<Todo> resultList = query
			.setFirstResult((page - 1) * size)
			.setMaxResults(size)
			.getResultList();

		long total = countQuery.getSingleResult();

		PageImpl<Todo> impl = new PageImpl<>(resultList, pageable, total);

		return impl.map(todo -> new TodoResponse(
			todo.getId(),
			todo.getTitle(),
			todo.getContents(),
			todo.getWeather(),
			new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
			todo.getCreatedAt(),
			todo.getModifiedAt()
		));
	}

	@Transactional(readOnly = true)
	public TodoResponse getTodo(long todoId) {
		Todo todo = todoRepository.findByIdWithUser(todoId)
			.orElseThrow(() -> new InvalidRequestException("Todo not found"));

		User user = todo.getUser();

		return new TodoResponse(
			todo.getId(),
			todo.getTitle(),
			todo.getContents(),
			todo.getWeather(),
			new UserResponse(user.getId(), user.getEmail()),
			todo.getCreatedAt(),
			todo.getModifiedAt()
		);
	}
}
