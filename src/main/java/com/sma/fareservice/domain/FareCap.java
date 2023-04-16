package com.sma.fareservice.domain;

import com.sma.fareservice.contants.Line;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class FareCap {

    private int routeId;

    private Line fromLine;

    private Line toLine;

    private int dailyCap;

    private int weeklyCap;

}