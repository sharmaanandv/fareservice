package com.sma.fareservice.util;

import com.sma.fareservice.contants.Line;
import com.sma.fareservice.domain.Fare;
import com.sma.fareservice.domain.FareCap;
import com.sma.fareservice.domain.UserRecord;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.FileSystemResourceLoader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CsvUtil {

    private static final String COMMA_DELIMITER = ",";

    private static final String RESOURCE_SAMPLE_INPUT = "csv/sample_input.csv";
    private static final String RESOURCE_FARES = "rules/fares.csv";

    private static final String RESOURCE_FARES_CAP = "rules/fares_cap.csv";

    public static List<Fare> getFares() {
        try {
            String fareCsvFile = new String(
                    new FileSystemResourceLoader().getClassLoader().getResource(RESOURCE_FARES).openStream()
                            .readAllBytes());
            return fareCsvFile.lines().map(line -> line.split(COMMA_DELIMITER)).filter(attr -> attr.length == 4).skip(1)
                    .map(CsvUtil::createFare).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Fare createFare(String[] values) {
        return Fare.builder().fromLine(Line.valueOf(values[0].trim())).toLine(Line.valueOf(values[1].trim()))
                .peak(values[2].trim()).nonPeak(values[3].trim()).build();
    }

    public static List<FareCap> getFareCaps() {
        List<FareCap> fareCaps = new ArrayList<>();
        try {
            String fareCapCsvFile = new String(
                    new FileSystemResourceLoader().getClassLoader().getResource(RESOURCE_FARES_CAP).openStream()
                            .readAllBytes());
            fareCaps = fareCapCsvFile.lines().map(line -> line.split(COMMA_DELIMITER)).filter(attr -> attr.length == 4)
                    .skip(1).map(CsvUtil::createFareCap).collect(Collectors.toList());
            for(int i=0;i<fareCaps.size();i++){
                fareCaps.get(i).setRouteId(i);
            }
            return fareCaps;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FareCap createFareCap(String[] values) {
        return FareCap.builder().fromLine(Line.valueOf(values[0].trim())).toLine(Line.valueOf(values[1].trim()))
                .dailyCap(Integer.parseInt(values[2].trim())).weeklyCap(Integer.parseInt(values[3].trim())).build();
    }

    public static List<UserRecord> getUserHistory() {
        try {
            String fareCapCsvFile = new String(
                    new FileSystemResourceLoader().getClassLoader().getResource(RESOURCE_SAMPLE_INPUT).openStream()
                            .readAllBytes());
            return fareCapCsvFile.lines().map(line -> line.split(COMMA_DELIMITER)).filter(attr -> attr.length == 3)
                    .map(CsvUtil::createUserHistory).collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private UserRecord createUserHistory(String[] values) {
        return UserRecord.builder().fromLine(Line.valueOf(values[0].trim())).toLine(Line.valueOf(values[1].trim()))
                .dateTime(LocalDateTime.parse(values[2].trim())).build();
    }
}
