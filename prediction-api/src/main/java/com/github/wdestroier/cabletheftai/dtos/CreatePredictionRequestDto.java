package com.github.wdestroier.cabletheftai.dtos;

import java.util.List;

public record CreatePredictionRequestDto(List<MeasurementDto> measurements) {}
