package com.sma.fareservice.util;

import com.sma.fareservice.contants.Line;
import com.sma.fareservice.domain.Fare;
import com.sma.fareservice.domain.FareCap;
import com.sma.fareservice.domain.UserRecord;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.FileSystemResourceLoader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ResourceUtil {

    private static final String COMMA_DELIMITER = ",";

    private static final String RESOURCE_SAMPLE_INPUT = "samples/sample_input.csv";
    private static final String RESOURCE_FARES = "rules/fares.csv";

    private static final String RESOURCE_FARES_CAP = "rules/fares_cap.csv";

    public static List<Fare> getFares() {
        return readResource(RESOURCE_FARES).lines().map(line -> line.split(COMMA_DELIMITER))
                .filter(attr -> attr.length == 4).skip(1).map(ResourceUtil::createFare).collect(Collectors.toList());

    }

    public static List<FareCap> getFareCaps() {
        List<FareCap> fareCaps = readResource(RESOURCE_FARES_CAP).lines().map(line -> line.split(COMMA_DELIMITER))
                .filter(attr -> attr.length == 4).skip(1).map(ResourceUtil::createFareCap).collect(Collectors.toList());
        for (int i = 0; i < fareCaps.size(); i++) {
            fareCaps.get(i).setRouteId(i);
        }
        return fareCaps;

    }

    public static List<UserRecord> getUserRecords() {
        return readResource(RESOURCE_SAMPLE_INPUT).lines().map(line -> line.split(COMMA_DELIMITER))
                .filter(attr -> attr.length == 3).map(ResourceUtil::createUserRecord).collect(Collectors.toList());
    }

    private static Fare createFare(String[] values) {
        return Fare.builder().fromLine(Line.valueOf(values[0].trim())).toLine(Line.valueOf(values[1].trim()))
                .peak(values[2].trim()).nonPeak(values[3].trim()).build();
    }

    private static FareCap createFareCap(String[] values) {
        return FareCap.builder().fromLine(Line.valueOf(values[0].trim())).toLine(Line.valueOf(values[1].trim()))
                .dailyCap(Integer.parseInt(values[2].trim())).weeklyCap(Integer.parseInt(values[3].trim())).build();
    }

    private UserRecord createUserRecord(String[] values) {
        return UserRecord.builder().fromLine(Line.valueOf(values[0].trim())).toLine(Line.valueOf(values[1].trim()))
                .dateTime(LocalDateTime.parse(values[2].trim())).build();
    }

    private static String readResource(String resource) {
        try {
            return new String(
                    new FileSystemResourceLoader().getClassLoader().getResource(resource).openStream().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read resource " + resource);
        }
    }

}
