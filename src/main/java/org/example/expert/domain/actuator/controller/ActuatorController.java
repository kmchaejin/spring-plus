package org.example.expert.domain.actuator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class ActuatorController {
	@GetMapping
	public ResponseEntity<String> healthCheck() {
		return new ResponseEntity<>("--running--", HttpStatus.OK);
	}
}
