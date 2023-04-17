package com.sma.fareservice.services.impl;

import com.sma.fareservice.contants.Line;
import com.sma.fareservice.domain.Fare;
import com.sma.fareservice.domain.FareCap;
import com.sma.fareservice.domain.UserRecord;
import com.sma.fareservice.services.DailyFareCalculationService;
import com.sma.fareservice.util.PeakHoursConfig;
import com.sma.fareservice.util.ResourceUtil;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DailyFareCalculationServiceImpl implements DailyFareCalculationService {

    private final List<Fare> fares;

    private final PeakHoursConfig peakHoursConfig;

    public DailyFareCalculationServiceImpl(PeakHoursConfig peakHoursConfig) {
        this.peakHoursConfig = peakHoursConfig;
        this.fares = ResourceUtil.getFares();
    }

    /**
     * This method will apply daily fare cap
     * @param singleWeekRecordsByDaysMap Map of day and list of records
     * @return map of <day,<routeId, fares >>
     * where routeId is unique key for each route, e.g 0-> Green- Green
     */
    @Override
    public Map<Integer, Map<Integer, Integer>> getWeeklyFareMap(
            Map<Integer, List<UserRecord>> singleWeekRecordsByDaysMap) {
        Map<Integer, Map<Integer, Integer>> singleWeekFareMap = new HashMap<>();
        for (Map.Entry<Integer, List<UserRecord>> dayRecordEntry : singleWeekRecordsByDaysMap.entrySet()) {
            Map<Integer, Integer> daywiseFareMap = new HashMap<>();
            List<FareCap> fareCaps = ResourceUtil.getFareCaps();
            for (FareCap fareCap : fareCaps) {
                int daywiseFare = 0;
                for (UserRecord userRecord : dayRecordEntry.getValue()) {
                    if (fareCap.getFromLine().equals(userRecord.getFromLine()) && fareCap.getToLine()
                            .equals(userRecord.getToLine())) {
                        boolean isPeakHour = isPeakHour(userRecord.getDateTime());
                        daywiseFare = daywiseFare + getApplicableFare(userRecord.getFromLine(), userRecord.getToLine(),
                                isPeakHour);
                        if (daywiseFare > fareCap.getDailyCap()) {
                            daywiseFare = fareCap.getDailyCap();
                            break;
                        }
                    }
                }
                if (daywiseFare != 0) {
                    daywiseFareMap.put(fareCap.getRouteId(), daywiseFare);
                }
            }
            singleWeekFareMap.put(dayRecordEntry.getKey(), daywiseFareMap);
        }
        return singleWeekFareMap;
    }

    private int getApplicableFare(Line fromLine, Line toLine, boolean isPeakHour) {
        Optional<Fare> fare = fares.stream()
                .filter(fareConfig -> fareConfig.getFromLine().equals(fromLine) && fareConfig.getToLine()
                        .equals(toLine)).findFirst();
        if (fare.isPresent()) {
            if (isPeakHour) {
                return getFareWithoutCurrency(fare.get().getPeak());
            } else {
                return getFareWithoutCurrency(fare.get().getNonPeak());
            }
        }
        return 0;
    }

    private boolean isPeakHour(LocalDateTime dateTime) {
        List<String> peakTimeSlabsOfDay = peakHoursConfig.getPeakHours().get(dateTime.getDayOfWeek().name());
        for (String peakTime : peakTimeSlabsOfDay) {
            String[] split = peakTime.split(",");
            String fromTime = getFormattedTime(split[0]);
            String toTime = getFormattedTime(split[1]);

            if (dateTime.toLocalTime().isAfter(LocalTime.parse(fromTime)) && dateTime.toLocalTime()
                    .isBefore(LocalTime.parse(toTime))) {
                return true;
            }
        }
        return false;
    }

    private String getFormattedTime(String time) {
        if (time.length() == 4) {
            return "0" + time;
        }
        return time;
    }

    private int getFareWithoutCurrency(String fare) {
        return Integer.parseInt(fare.substring(1));
    }

}
