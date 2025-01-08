package com.bbook.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewStatsDto {
	private Map<Integer, Double> ratingStats;
	private double avgRating;
	private Map<String, Double> tagStats;
	private String mostCommonTag;
}
