package io.pivotal.pal.tracker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/time-entries")
public class TimeEntryController {
    private TimeEntryRepository timeEntryRepository;
    private DistributionSummary timeEntrySummary;
    private final Counter actionCounter;

    public TimeEntryController(TimeEntryRepository timeEntryRepository,
                               MeterRegistry meterRegistry) {
        this.timeEntryRepository = timeEntryRepository;
        timeEntrySummary = meterRegistry.summary("timeEntry.summary");
        actionCounter = meterRegistry.counter("timeEntry.actionCounter");
    }

    @PostMapping
    public ResponseEntity<TimeEntry> create(@RequestBody TimeEntry timeEntry) {
        TimeEntry createdTimeEntry = timeEntryRepository.create(timeEntry);
        actionCounter.increment();
        timeEntrySummary.record(timeEntryRepository.list().size());

        return new ResponseEntity<>(createdTimeEntry, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<TimeEntry> read(@PathVariable("id") long timeEntryId) {
        TimeEntry foundTimeEntry = timeEntryRepository.find(timeEntryId);
        if (foundTimeEntry == null) {
            return ResponseEntity.notFound().build();
        }

        actionCounter.increment();
        return ResponseEntity.ok(foundTimeEntry);
    }

    @GetMapping
    public ResponseEntity<List<TimeEntry>> list() {
        actionCounter.increment();
        List<TimeEntry> timeEntryList = timeEntryRepository.list();
        return ResponseEntity.ok(timeEntryList);
    }

    @PutMapping("{id}")
    public ResponseEntity<TimeEntry> update(@PathVariable("id") long timeEntryId, @RequestBody TimeEntry expected) {
        TimeEntry newTimeEntry = timeEntryRepository.update(timeEntryId, expected);
        if (newTimeEntry == null) {
            return ResponseEntity.notFound().build();
        }

        actionCounter.increment();
        return ResponseEntity.ok(newTimeEntry);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable("id") long timeEntryId) {
        timeEntryRepository.delete(timeEntryId);
        actionCounter.increment();
        return ResponseEntity.noContent().build();
    }
}
