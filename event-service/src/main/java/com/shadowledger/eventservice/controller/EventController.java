package com.shadowledger.eventservice.controller;

import com.shadowledger.eventservice.model.EventRequest;
import com.shadowledger.eventservice.service.EventProducerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventProducerService producerService;

    public EventController(EventProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void createEvent(@Valid @RequestBody EventRequest request) {
        producerService.processEvent(request);
    }
}
