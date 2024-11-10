package org.sopt.app.application.calendar;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.sopt.app.domain.cache.CachedAllCalendarResponse;
import org.sopt.app.interfaces.postgres.CalendarRepository;
import org.sopt.app.interfaces.postgres.redis.CachedCalendarRepository;
import org.sopt.app.presentation.calendar.AllCalendarResponse;
import org.sopt.app.presentation.calendar.CalendarResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final CalendarRepository calendarRepository;
    private final CachedCalendarRepository cachedCalendarRepository;

    @Value("${sopt.current.generation}")
    private Integer currentGeneration;

    @Override
    @Transactional
    public AllCalendarResponse getAllCurrentGenerationCalendar() {

        Optional<CachedAllCalendarResponse> cachedCalendar = cachedCalendarRepository.findById(currentGeneration);

        return cachedCalendar.orElseGet(this::cacheAllCalendarResponse).getCalendars();
    }

    private CachedAllCalendarResponse cacheAllCalendarResponse() {
        AllCalendarResponse allCalendarResponse = new AllCalendarResponse(
                calendarRepository.findAllByGenerationOrderByStartDate(currentGeneration)
                        .stream().map(CalendarResponse::of)
                        .toList()
        );

        return cachedCalendarRepository.save(
                new CachedAllCalendarResponse(currentGeneration, allCalendarResponse)
        );
    }
}
