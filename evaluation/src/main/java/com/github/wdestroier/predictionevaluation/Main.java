package com.github.wdestroier.predictionevaluation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import com.github.wdestroier.predictionevaluation.models.Feed;
import com.github.wdestroier.predictionevaluation.services.prediction.PredictionServiceImpl;

public class Main {

	public static void main(String... args) throws IOException {
		System.out.println(new PredictionServiceImpl().evaluate(readFeeds()));
		// EvaluationResult{aiAccuracy=100,00%, lowThresholdAccuracy=0,00%, moderateThresholdAccuracy=37,55%, severeThresholdAccuracy=53,55%, criticalThresholdAccuracy=94,95%}
	}

	private static List<Feed> readFeeds() throws IOException {
		var resourceFileName = "feeds.csv";

		var stream = Main.class.getClassLoader().getResourceAsStream(resourceFileName);
		if (stream == null) {
			throw new IllegalArgumentException("Resource not found: " + resourceFileName);
		}

		var feeds = new ArrayList<Feed>();

		try (var reader = new InputStreamReader(stream)) {
			var records = CSVFormat.DEFAULT
					.withHeader("device_id", "created_at", "feed_entry_id", "luminosity_value", "vibration_value", "alert")
					.withSkipHeaderRecord(true)
					.parse(reader);

			for (var record : records) {
				var feed = new Feed(
						record.get("device_id"),
						record.get("created_at"),
						record.get("feed_entry_id"),
						record.get("luminosity_value"),
						record.get("vibration_value"),
						record.get("alert")
				);
				feeds.add(feed);
			}
		}

		return feeds;
	}

}
