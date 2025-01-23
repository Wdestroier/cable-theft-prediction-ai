package com.github.wdestroier.predictionevaluation.models;

public record EvaluationResult(
		double aiAccuracy,
		double lowThresholdAccuracy,
		double moderateThresholdAccuracy,
		double severeThresholdAccuracy,
		double criticalThresholdAccuracy
) {
	@Override
	public String toString() {
		return String.format(
				"EvaluationResult{aiAccuracy=%.2f%%, lowThresholdAccuracy=%.2f%%, moderateThresholdAccuracy=%.2f%%, severeThresholdAccuracy=%.2f%%, criticalThresholdAccuracy=%.2f%%}",
				aiAccuracy * 100,
				lowThresholdAccuracy * 100,
				moderateThresholdAccuracy * 100,
				severeThresholdAccuracy * 100,
				criticalThresholdAccuracy * 100
		);
	}
}
