package com.github.wdestroier.cabletheftai.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.wdestroier.cabletheftai.dtos.CreatePredictionRequestDto;
import com.github.wdestroier.cabletheftai.services.PredictionService;

@RestController
public record PredictionController(PredictionService predictionService) {

	@PostMapping("/v1/predict")
	public ResponseEntity<?> predict(@RequestBody CreatePredictionRequestDto request) {
		try {
			return ResponseEntity.ok(predictionService.predict(request));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

}
