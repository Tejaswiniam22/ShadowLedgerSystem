package com.shadowledger.eventservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shadowledger.eventservice.entity.EventEntity;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    Optional<EventEntity> findByEventId(String eventId);
}
