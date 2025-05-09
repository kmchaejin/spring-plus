package org.example.expert.domain.todo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.QUser;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.security.CustomUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Service
@RequiredArgsConstructor
public class TodoService {
	@PersistenceContext
	private EntityManager em;

	private final TodoRepository todoRepository;
	private final WeatherClient weatherClient;
	private final UserRepository userRepository;

	public TodoSaveResponse saveTodo(CustomUserPrincipal userPrincipal, TodoSaveRequest todoSaveRequest) {
		Long userId = Long.parseLong(userPrincipal.getUsername());
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new InvalidRequestException("User not found"));

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
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);

		QTodo t = QTodo.todo;
		QUser u = QUser.user;

		Todo todo = queryFactory
			.selectFrom(t)
			.join(t.user, u).fetchJoin()
			.where(t.id.eq(todoId))
			.fetchFirst();

		Optional.ofNullable(todo)
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

	public Page<TodoSearchResponse> searchTodos(String title, String nickname, LocalDate createdFrom, LocalDate createdTo, int page, int size) {
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		QTodo todo = QTodo.todo;
		QManager manager = QManager.manager;
		QComment comment = QComment.comment;

		// projection
		// JPAExpression
		List<TodoSearchResponse> responseDto = queryFactory
			.select(Projections.constructor(TodoSearchResponse.class,
				todo.title,
				JPAExpressions
					.select(manager.count())
					.from(manager)
					.where(manager.todo.id.eq(todo.id)),
				JPAExpressions
					.select(comment.count())
					.from(comment)
					.where(comment.todo.id.eq(todo.id))))
			.from(todo)
			.where(
				StringUtils.isBlank(title) ? null : todo.title.like("%" + title + "%"),
				StringUtils.isBlank(nickname) ? null : todo.user.nickname.like("%" + nickname + "%"),
				createdFrom == null ? null : todo.createdAt.after(createdFrom.atStartOfDay()),
				createdTo == null ? null : todo.createdAt.before(createdTo.atTime(23, 59, 59))
			)
			.offset((long) (page - 1) * size)
			.limit(size)
			.orderBy(todo.createdAt.desc())
			.fetch();

		Long total = Optional.ofNullable(queryFactory
			.select(todo.count())
			.from(todo)
			.where(
				StringUtils.isBlank(title) ? null : todo.title.like("%" + title + "%"),
				StringUtils.isBlank(nickname) ? null : todo.user.nickname.like("%" + nickname + "%"),
				createdFrom == null ? null : todo.createdAt.after(createdFrom.atStartOfDay()),
				createdTo == null ? null : todo.createdAt.before(createdTo.atTime(23, 59, 59))
			).fetchOne())
			.orElse(0L);

		return new PageImpl<>(responseDto, PageRequest.of(page, size), total);
	}
}
