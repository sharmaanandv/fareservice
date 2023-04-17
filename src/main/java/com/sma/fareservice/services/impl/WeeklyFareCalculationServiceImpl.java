package com.sma.fareservice.services.impl;

import com.sma.fareservice.domain.FareCap;
import com.sma.fareservice.services.WeeklyFareCalculationService;
import com.sma.fareservice.util.ResourceUtil;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeeklyFareCalculationServiceImpl implements WeeklyFareCalculationService {
    private final List<FareCap> fareCaps;

    public WeeklyFareCalculationServiceImpl() {
        fareCaps = ResourceUtil.getFareCaps();
    }

    /**
     * This method will apply weekly fare cap
     * @param weeklyFareMap map of <day,<routeId, fares >>
     * where routeId is unique key for each route, e.g 0-> Green- Green
     * @return weeklyFares
     */
    @Override
    public int getWeeklyFares(Map<Integer, Map<Integer, Integer>> weeklyFareMap) {
        int weekwisefare = 0;
        Map<Integer, Integer> faresByRouteId = weeklyFareMap.entrySet().stream()
                .flatMap(x -> x.getValue().entrySet().stream().filter(y -> y.getValue() != 0))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue(), (f, s) -> f + s));
        for (Map.Entry<Integer, Integer> faresByRouteIdEntry : faresByRouteId.entrySet()) {
            FareCap fareCap = fareCaps.stream().filter(x -> x.getRouteId() == faresByRouteIdEntry.getKey().intValue())
                    .findFirst().get();
            if (faresByRouteIdEntry.getValue() > fareCap.getWeeklyCap()) {
                weekwisefare = weekwisefare + fareCap.getWeeklyCap();
            } else {
                weekwisefare = weekwisefare + faresByRouteIdEntry.getValue();
            }
        }
        return weekwisefare;
    }
}
