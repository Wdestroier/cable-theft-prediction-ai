package com.github.wdestroier.predictionevaluation.models;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum AlertLevel {
	UNKNOWN(0),
	LOW(1),
	MODERATE(2),
	SEVERE(3),
	CRITICAL(4);

	private int code;

	private AlertLevel(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static AlertLevel valueOf(int code) {
		for (var value : AlertLevel.values()) {
			if (code == value.getCode()) {
				return value;
			}
		}

		throw new IllegalArgumentException("Invalid alert level code.");
	}

	public static AlertLevel max(AlertLevel... levels) {
		if (levels.length < 2) throw new NoSuchElementException("Alert levels must have at least 2 elements to find the max level.");
		return Arrays.stream(levels).reduce(AlertLevel::max).orElseThrow();
	}

	public static AlertLevel max(AlertLevel first, AlertLevel second) {
		return (first.code > second.code) ? first : second;
	}

}
