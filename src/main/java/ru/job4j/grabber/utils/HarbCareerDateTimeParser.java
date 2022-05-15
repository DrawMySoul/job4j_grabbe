package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HarbCareerDateTimeParser implements DateTimeParser {
    @Override
    public LocalDateTime parse(String parse) {
        List<Integer> date = Arrays.stream(parse.split("\\+")[0].split("\\D"))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
        return LocalDateTime.of(date.get(0), date.get(1), date.get(2), date.get(3), date.get(4), date.get(5));
    }
}
