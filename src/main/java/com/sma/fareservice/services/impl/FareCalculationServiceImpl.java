package com.sma.fareservice.services.impl;

import com.sma.fareservice.domain.UserRecord;
import com.sma.fareservice.services.DailyFareCalculationService;
import com.sma.fareservice.services.FareCalculationService;
import com.sma.fareservice.services.WeeklyFareCalculationService;
import com.sma.fareservice.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FareCalculationServiceImpl implements FareCalculationService {

    private List<UserRecord> userRecords;

    private final DailyFareCalculationService dailyFareCalculationService;

    private final WeeklyFareCalculationService weeklyFareCalculationService;

    public FareCalculationServiceImpl(final DailyFareCalculationService dailyFareCalculationService,
            final WeeklyFareCalculationService weeklyFareCalculationService) {
        this.dailyFareCalculationService = dailyFareCalculationService;
        this.weeklyFareCalculationService = weeklyFareCalculationService;
        this.userRecords = ResourceUtil.getUserRecords();
    }

    @Override
    public int calculateFare() {
        int totalFare = 0;
        Map<Integer, List<UserRecord>> allWeeklyRecordsMap = userRecords.stream()
                .collect(Collectors.groupingBy(record -> getWeekNumber(record.getDateTime())));

        for (Map.Entry<Integer, List<UserRecord>> singleWeekRecordEntry : allWeeklyRecordsMap.entrySet()) {

            Map<Integer, List<UserRecord>> singleWeekRecordsByDays = singleWeekRecordEntry.getValue().stream()
                    .collect(Collectors.groupingBy(record -> Integer.valueOf(record.getDateTime().getDayOfMonth())));

            Map<Integer, Map<Integer, Integer>> weeklyFareMap = dailyFareCalculationService.getWeeklyFareMap(
                    singleWeekRecordsByDays);

            totalFare = totalFare + weeklyFareCalculationService.getWeeklyFares(weeklyFareMap);
        }
        log.info("Total fare applied: {}", totalFare);
        return totalFare;
    }

    private int getWeekNumber(LocalDateTime localDateTime) {
        return localDateTime.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }

}
