package com.sma.fareservice.services;

import com.sma.fareservice.contants.Line;
import com.sma.fareservice.domain.Fare;
import com.sma.fareservice.domain.FareCap;
import com.sma.fareservice.domain.UserRecord;
import com.sma.fareservice.util.CsvUtil;
import com.sma.fareservice.util.PeakHoursConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FareCalculationServiceImpl implements FareCalculationService {

    private List<Fare> fares;
    private List<FareCap> fareCaps;
    private List<UserRecord> userRecords;

    private final PeakHoursConfig peakHoursConfig;

    public FareCalculationServiceImpl(final PeakHoursConfig peakHoursConfig) {
        this.peakHoursConfig = peakHoursConfig;
        fares = CsvUtil.getFares();
        fareCaps = CsvUtil.getFareCaps();
        userRecords = CsvUtil.getUserHistory();
    }

    @Override
    public void calculateFare() {

        int totalFare = 0;

        Map<Integer, List<UserRecord>> allWeeklyRecordsMap = userRecords.stream()
                .collect(Collectors.groupingBy(record -> getWeekNumber(record.getDateTime())));

        for (Map.Entry<Integer, List<UserRecord>> singleWeekRecordEntry : allWeeklyRecordsMap.entrySet()) {
            List<UserRecord> singleWeekRecords = singleWeekRecordEntry.getValue();
            Map<Integer, Integer> weekwiseFareMap = new HashMap<>();
            int weekwisefare = 0;
            Map<Integer, List<UserRecord>> singleWeekRecordsByDaysMap = singleWeekRecords.stream()
                    .collect(Collectors.groupingBy(record -> Integer.valueOf(record.getDateTime().getDayOfMonth())));
            for (Map.Entry<Integer, List<UserRecord>> alldayRecordsEntry : singleWeekRecordsByDaysMap.entrySet()) {
                Map<Integer, List<UserRecord>> adayRecordsMap = alldayRecordsEntry.getValue()
                        .stream()//all single day records
                        .collect(
                                Collectors.groupingBy(record -> Integer.valueOf(record.getDateTime().getDayOfMonth())));
                Map<Integer, Integer> daywiseFareMap = new HashMap<>();
                for (Map.Entry<Integer, List<UserRecord>> dayRecordEntry : adayRecordsMap.entrySet()) {
                    for (FareCap fareCap : fareCaps) {
                        int daywiseFare = 0;

                        for (UserRecord userRecord : dayRecordEntry.getValue()) {

                            if (fareCap.getFromLine().equals(userRecord.getFromLine()) && fareCap.getToLine()
                                    .equals(userRecord.getToLine())) {
                                boolean isPeakHour = isPeakHour(userRecord.getDateTime());
                                daywiseFare = daywiseFare + getApplicableFare(userRecord.getFromLine(),
                                        userRecord.getToLine(), isPeakHour);
                                if (daywiseFare > fareCap.getDailyCap()) {
                                    daywiseFare = fareCap.getDailyCap();
                                    break;
                                }
                            }
                        }
                        daywiseFareMap.put(fareCap.getRouteId(), daywiseFare);
                        //totalFare = totalFare + daywiseFare;
                    }
                    if (totalFare > 0) {

                    }
                }
            }
        }

        //userRecords.stream().collect(Collectors.groupingBy(record-> record.getDateTime().get));

        log.info("Total fare applied: {}", totalFare);
    }

    private int getWeekNumber(LocalDateTime localDateTime) {
        return localDateTime.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
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

    private int getFareWithoutCurrency(String fare) {
        return Integer.parseInt(fare.substring(1));
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

}
