package com.sma.fareservice.services;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public interface WeeklyFareCalculationService {

    int getWeeklyFares(Map<Integer, Map<Integer, Integer>> weeklyFareMap);
}
