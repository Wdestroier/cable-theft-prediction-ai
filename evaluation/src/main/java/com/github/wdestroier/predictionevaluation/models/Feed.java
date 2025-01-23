package com.github.wdestroier.predictionevaluation.models;

public record Feed(
		String deviceId,
		String createdAt,
		String feedEntryId,
		String luminosityValue,
		String vibrationValue,
		String alert
) {}
