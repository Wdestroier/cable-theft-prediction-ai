package com.github.wdestroier.cabletheftai.services;

import com.github.wdestroier.cabletheftai.dtos.CreatePredictionRequestDto;
import com.github.wdestroier.cabletheftai.dtos.CreatePredictionResponseDto;

public interface PredictionService {

	CreatePredictionResponseDto predict(CreatePredictionRequestDto request);

}
