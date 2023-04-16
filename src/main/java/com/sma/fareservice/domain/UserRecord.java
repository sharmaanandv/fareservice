package com.sma.fareservice.domain;

import com.sma.fareservice.contants.Line;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class UserRecord {

    private Line fromLine;

    private Line toLine;

    private LocalDateTime dateTime;

}
