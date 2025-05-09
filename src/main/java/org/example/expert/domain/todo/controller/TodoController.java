package org.example.expert.domain.todo.controller;

import java.time.LocalDate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.security.CustomUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TodoController {

	private final TodoService todoService;

	@PostMapping("/todos")
	public ResponseEntity<TodoSaveResponse> saveTodo(
		@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
		@Valid @RequestBody TodoSaveRequest todoSaveRequest
	) {
		return ResponseEntity.ok(todoService.saveTodo(userPrincipal, todoSaveRequest));
	}

	@GetMapping("/todos")
	public ResponseEntity<Page<TodoResponse>> getTodos(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String weather,
		@RequestParam(required = false) LocalDate modifiedFrom,
		@RequestParam(required = false) LocalDate modifiedTo
	) {
		return ResponseEntity.ok(todoService.getTodos(page, size, weather, modifiedFrom, modifiedTo));
	}

	@GetMapping("/todos/{todoId}")
	public ResponseEntity<TodoResponse> getTodo(@PathVariable long todoId) {
		return ResponseEntity.ok(todoService.getTodo(todoId));
	}

	// level 3-10
	@GetMapping("/search/todos")
	public ResponseEntity<Page<TodoSearchResponse>> searchTodos(
		@RequestParam(required = false) String title,
		@RequestParam(required = false) String nickname,
		@RequestParam(required = false) LocalDate createdFrom,
		@RequestParam(required = false) LocalDate createdTo,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
		){
		return ResponseEntity.ok(todoService.searchTodos(title, nickname, createdFrom, createdTo, page, size));
	}
}
