package com.github.wdestroier.predictionevaluation.services.prediction;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import com.github.wdestroier.predictionevaluation.dtos.AiPredictionRequestDto;
import com.github.wdestroier.predictionevaluation.dtos.MeasurementDto;
import com.github.wdestroier.predictionevaluation.dtos.ThresholdPredictionRequest;
import com.github.wdestroier.predictionevaluation.models.EvaluationResult;
import com.github.wdestroier.predictionevaluation.models.Feed;
import com.github.wdestroier.predictionevaluation.services.prediction.strategies.AiPredictionStrategy;
import com.github.wdestroier.predictionevaluation.services.prediction.strategies.ThresholdPredictionStrategy;

public class PredictionServiceImpl implements PredictionService {

	private static final int WINDOW_SIZE = 20;

	private AiPredictionStrategy aiPrediction = new AiPredictionStrategy();
	private ThresholdPredictionStrategy thresholdPrediction = new ThresholdPredictionStrategy();

	@Override
	public EvaluationResult evaluate(List<Feed> feeds) {
		var requests = createRequests(feeds);

		// Track correct predictions.
		var aiCorrectPredictions = 0;
		var lowThresholdCorrectPredictions = 0;
		var moderateThresholdCorrectPredictions = 0;
		var severeThresholdCorrectPredictions = 0;
		var criticalThresholdCorrectPredictions = 0;

		for (Pair<AiPredictionRequestDto, Boolean> pair : requests) {
			var request = pair.getValue0();
			var actualAlert = pair.getValue1();

			// Predict with AI strategy.
			var aiPredictedAlert = aiPrediction.predict(request).alert();

			// Predict with threshold strategy.
			var lastMeasurement = request.measurements().getLast(); // The oldest measurement.
			var thresholdRequest = new ThresholdPredictionRequest(lastMeasurement);
			var thresholdResponse = thresholdPrediction.predict(thresholdRequest);

			// Compare AI predictions with actual alert.
			if (aiPredictedAlert == actualAlert) {
				aiCorrectPredictions++;
			}
			if (thresholdResponse.low() == actualAlert) {
				lowThresholdCorrectPredictions++;
			}
			if (thresholdResponse.moderate() == actualAlert) {
				moderateThresholdCorrectPredictions++;
			}
			if (thresholdResponse.severe() == actualAlert) {
				severeThresholdCorrectPredictions++;
			}
			if (thresholdResponse.critical() == actualAlert) {
				criticalThresholdCorrectPredictions++;
			}
		}

		// Calculate accuracy and error rate for predictions.
		var totalPredictions = requests.size();
		var aiAccuracy = (double) aiCorrectPredictions / totalPredictions;
		var lowThresholdAccuracy = (double) lowThresholdCorrectPredictions / totalPredictions;
		var moderateThresholdAccuracy = (double) moderateThresholdCorrectPredictions / totalPredictions;
		var severeThresholdAccuracy = (double) severeThresholdCorrectPredictions / totalPredictions;
		var criticalThresholdAccuracy = (double) criticalThresholdCorrectPredictions / totalPredictions;

		return new EvaluationResult(
				aiAccuracy,
				lowThresholdAccuracy,
				moderateThresholdAccuracy,
				severeThresholdAccuracy,
				criticalThresholdAccuracy
		);
	}

	private List<Pair<AiPredictionRequestDto, Boolean>> createRequests(List<Feed> feeds) {
		var requests = new ArrayList<Pair<AiPredictionRequestDto, Boolean>>();

		for (var i = 0; i <= feeds.size() - WINDOW_SIZE; i++) {
			requests.add(Pair.with(
					new AiPredictionRequestDto(createMeasurements(feeds, i)),
					hasAlert(feeds, i)
			));
		}

		return requests;
	}

	private List<MeasurementDto> createMeasurements(List<Feed> feeds, int startIndex) {
		var measurements = new ArrayList<MeasurementDto>();

		for (var i = startIndex; i < startIndex + WINDOW_SIZE; i++) {
			var feed = feeds.get(i);
			measurements.add(toMeasurement(feed));
		}

		return measurements;
	}

	private MeasurementDto toMeasurement(Feed feed) {
		return new MeasurementDto(
				tryParseFloat(feed.luminosityValue()),
				tryParseFloat(feed.vibrationValue()));
	}

	private float tryParseFloat(String value) {
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException | NullPointerException e) {
			return 0f;
		}
	}

	private boolean hasAlert(List<Feed> feeds, int startIndex) {
		var lastFeed = feeds.get(startIndex + WINDOW_SIZE - 1);
		return "true".equalsIgnoreCase(lastFeed.alert());
	}

}
