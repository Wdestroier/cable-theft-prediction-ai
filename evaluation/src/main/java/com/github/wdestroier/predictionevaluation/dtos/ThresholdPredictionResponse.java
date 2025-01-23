package com.github.wdestroier.predictionevaluation.dtos;

public record ThresholdPredictionResponse(
		boolean low,
		boolean moderate,
		boolean severe,
		boolean critical
) {}
