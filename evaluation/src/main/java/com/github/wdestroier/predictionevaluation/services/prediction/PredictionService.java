package com.github.wdestroier.predictionevaluation.services.prediction;

import com.github.wdestroier.predictionevaluation.models.EvaluationResult;
import com.github.wdestroier.predictionevaluation.models.Feed;

import java.io.IOException;
import java.util.List;

public interface PredictionService {

	EvaluationResult evaluate(List<Feed> feeds) throws IOException;

}
