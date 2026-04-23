package ch.scs.scoutsummoners.repository;

import ch.scs.scoutsummoners.entity.Event;
import ch.scs.scoutsummoners.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCreator(User creator);
    
    @Query("SELECT e FROM Event e WHERE e.creator = :user OR :user MEMBER OF e.participants")
    List<Event> findAllUserEvents(User user);
    
    List<Event> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT e FROM Event e WHERE (e.creator = :user OR :user MEMBER OF e.participants) AND e.startTime >= :start AND e.startTime <= :end ORDER BY e.startTime")
    List<Event> findUserEventsBetween(User user, LocalDateTime start, LocalDateTime end);
}
