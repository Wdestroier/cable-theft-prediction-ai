package com.github.wdestroier.predictionevaluation.services.prediction.strategies;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wdestroier.predictionevaluation.dtos.AiPredictionRequestDto;
import com.github.wdestroier.predictionevaluation.dtos.AiPredictionResponseDto;

public class AiPredictionStrategy {

	private static final String BASE_URL = "http://localhost:8080";
	private static final String ENDPOINT_PATH = "/v1/predict";
	private static final String ENDPOINT_URL = BASE_URL + ENDPOINT_PATH;

	private ObjectMapper objectMapper;

	public AiPredictionStrategy() {
		this.objectMapper = new ObjectMapper();
	}

	public AiPredictionResponseDto predict(AiPredictionRequestDto request) {
		if (request == null) {
			throw new IllegalArgumentException("Request cannot be null.");
		}

		try {
			var url = new URL(ENDPOINT_URL);
			var connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			// Serialize the request object to JSON.
			var jsonRequest = objectMapper.writeValueAsString(request);

			// Write JSON to the output stream.
			try (var outputStream = connection.getOutputStream()) {
				outputStream.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
				outputStream.flush();
			}

			// Read the response.
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return objectMapper.readValue(connection.getInputStream(), AiPredictionResponseDto.class);
			} else {
				throw new RuntimeException("Failed to call prediction API. HTTP code: " + connection.getResponseCode() + ".");
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to call prediction API: " + e.getMessage() + ".", e);
		}
	}
}
