package com.sma.fareservice.services;

import com.sma.fareservice.domain.UserRecord;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public interface DailyFareCalculationService {

    Map<Integer, Map<Integer, Integer>> getWeeklyFareMap(Map<Integer, List<UserRecord>> singleWeekRecordsByDaysMap);

}
