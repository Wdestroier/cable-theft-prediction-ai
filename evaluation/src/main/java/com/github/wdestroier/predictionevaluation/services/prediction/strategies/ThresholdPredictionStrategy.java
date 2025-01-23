package com.github.wdestroier.predictionevaluation.services.prediction.strategies;

import com.github.wdestroier.predictionevaluation.dtos.ThresholdPredictionRequest;
import com.github.wdestroier.predictionevaluation.dtos.ThresholdPredictionResponse;
import com.github.wdestroier.predictionevaluation.models.AlertLevel;
import com.github.wdestroier.predictionevaluation.models.ReferenceValues;

public class ThresholdPredictionStrategy {

	private static final ReferenceValues LUMINOSITY_REFERENCE_VALUES =
			new ReferenceValues(0.0, 21.0, 100.0, 61.0);

	private static final ReferenceValues VIBRATION_REFERENCE_VALUES =
			new ReferenceValues(0.0, 10001.0, 50001.0, 30001.0);

	public ThresholdPredictionResponse predict(ThresholdPredictionRequest request) {
		var measurement = request.measurement();

		var luminosityAlertLevel = getAlertLevel(measurement.luminosity(), LUMINOSITY_REFERENCE_VALUES);
		var vibrationAlertLevel = getAlertLevel(measurement.vibration(), VIBRATION_REFERENCE_VALUES);

		var alertLevel = AlertLevel.max(luminosityAlertLevel, vibrationAlertLevel);

		return new ThresholdPredictionResponse(
				shouldAlert(alertLevel, AlertLevel.LOW),
				shouldAlert(alertLevel, AlertLevel.MODERATE),
				shouldAlert(alertLevel, AlertLevel.SEVERE),
				shouldAlert(alertLevel, AlertLevel.CRITICAL)
		);
	}

	/**
	 * If limit is LOW, then should return true if current is LOW or UNKNOWN.
	 *
	 * If limit is CRITICAL, then should return true if current is UNKNOWN, LOW,
	 * MODERATE, SEVERE or CRITICAL.
	 */
	private boolean shouldAlert(AlertLevel current, AlertLevel limit) {
		return current.getCode() >= limit.getCode();
	}
	
	private AlertLevel getAlertLevel(double measurement, ReferenceValues referenceValues) {
		AlertLevel alertLevel;

		if (measurement >= referenceValues.severe()) {
			alertLevel = AlertLevel.SEVERE;
		} else if (measurement >= referenceValues.critical()) {
			alertLevel = AlertLevel.CRITICAL;
		} else if (measurement >= referenceValues.moderate()) {
			alertLevel = AlertLevel.MODERATE;
		} else {
			alertLevel = AlertLevel.LOW;
		}

		return alertLevel;
	}
	
}
