package ch.scs.scoutsummoners.service;

import ch.scs.scoutsummoners.entity.Event;
import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getAllUserEvents(User user) {
        return eventRepository.findAllUserEvents(user);
    }

    public List<Event> getEventsVisibleToUser(User user) {
        List<Event> allEvents = eventRepository.findAll();
        return allEvents.stream()
                .filter(event -> event.isOpenToAll()
                        || event.getCreator().getId().equals(user.getId())
                        || event.getParticipants().contains(user)
                        || event.getInvitedUsers().contains(user))
                .toList();
    }

    public List<Event> getUserEventsBetween(User user, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findUserEventsBetween(user, start, end);
    }

    public List<Event> getEventsVisibleToUserBetween(User user, LocalDateTime start, LocalDateTime end) {
        List<Event> allEvents = eventRepository.findAll();
        return allEvents.stream()
                .filter(event ->
                    (event.isOpenToAll()
                        || event.getCreator().getId().equals(user.getId())
                        || event.getParticipants().contains(user)
                        || event.getInvitedUsers().contains(user))
                    && !event.getStartTime().isAfter(end)
                    && !event.getEndTime().isBefore(start))
                .toList();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }

    public void addParticipant(Event event, User user) {
        if (!event.getParticipants().contains(user)) {
            event.getParticipants().add(user);
            eventRepository.save(event);
        }
    }

    public void removeParticipant(Event event, User user) {
        event.getParticipants().remove(user);
        eventRepository.save(event);
    }
}
