package com.github.wdestroier.predictionevaluation.dtos;

import java.util.List;

public record AiPredictionRequestDto(List<MeasurementDto> measurements) {}
