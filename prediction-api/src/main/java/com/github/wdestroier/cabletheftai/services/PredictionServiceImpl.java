package com.github.wdestroier.cabletheftai.services;

import com.github.wdestroier.cabletheftai.dtos.MeasurementDto;
import org.springframework.stereotype.Service;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.types.TFloat32;
import com.github.wdestroier.cabletheftai.dtos.CreatePredictionRequestDto;
import com.github.wdestroier.cabletheftai.dtos.CreatePredictionResponseDto;

import java.util.List;

@Service
public record PredictionServiceImpl(SavedModelBundle model) implements PredictionService {

	private static final String MODEL_INPUT_NODE = "serving_default_input_layer_11:0";
	private static final String MODEL_OUTPUT_NODE = "StatefulPartitionedCall_1:0";

	private static final float CLASSIFICATION_THRESHOLD = 0.3f;

	@Override
	public CreatePredictionResponseDto predict(CreatePredictionRequestDto request) {
		var rawOutput = predictRawOutput(getPredictionInput(request));
		var predicted = rawOutput < CLASSIFICATION_THRESHOLD;

		return new CreatePredictionResponseDto(predicted);
	}

	private float predictRawOutput(float[][][] input) {
		try (
			// Prepare the input tensor (TFloat32 from the new TF Java).
			var inputTensor = TFloat32.tensorOf(StdArrays.ndCopyOf(input));

			// Run the model. In TF 2.x we just get a Tensor back and cast.
			var result = model.session()
					.runner()
					.feed(MODEL_INPUT_NODE, inputTensor)
					.fetch(MODEL_OUTPUT_NODE)
					.run();

			// Cast to TFloat32 (or TDouble, TInt32, etc., depending on the model's output).
			var outputTensor = (TFloat32) result.get(0);
		) {
			// Extract the result.
			return outputTensor.get(0).getFloat(0);
		}
	}

	// The model was trained with 20 input parameters grouped by pairs. Therefore, 40
	// input values are required to predict the output.
	private float[][][] getPredictionInput(CreatePredictionRequestDto request) {
		var measurements = request.measurements();

		if (measurements == null || measurements.size() != 20) {
			throw new IllegalArgumentException("Prediction input must have 20 values.");
		}

		// Create the 2D float array with the same size as the input list.
		var result = new float[measurements.size()][2];

		// Populate the 2D array with luminosity and vibration values from the measurement.
		for (var i = 0; i < measurements.size(); i++) {
			var measurement = measurements.get(i);

			result[i][0] = measurement.luminosity();
			result[i][1] = measurement.vibration();
		}

		return new float[][][] { result };
	}

}
