package ch.scs.scoutsummoners.service;

import ch.scs.scoutsummoners.entity.Event;
import ch.scs.scoutsummoners.entity.TimeSlot;
import ch.scs.scoutsummoners.entity.TimeSlotSurvey;
import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.repository.TimeSlotRepository;
import ch.scs.scoutsummoners.repository.TimeSlotSurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SurveyService {

    @Autowired
    private TimeSlotSurveyRepository surveyRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private EventService eventService;

    @Transactional
    public TimeSlotSurvey createSurvey(TimeSlotSurvey survey) {
        return surveyRepository.save(survey);
    }

    public List<TimeSlotSurvey> getAllSurveys() {
        return surveyRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<TimeSlotSurvey> getSurveysVisibleToUser(User user) {
        List<TimeSlotSurvey> allSurveys = surveyRepository.findAllByOrderByCreatedAtDesc();
        return allSurveys.stream()
                .filter(survey -> survey.isOpenToAll()
                        || survey.getCreator().getId().equals(user.getId())
                        || survey.getInvitedUsers().contains(user))
                .toList();
    }

    public List<TimeSlotSurvey> getActiveSurveys() {
        return surveyRepository.findByClosedFalseOrderByCreatedAtDesc();
    }

    public TimeSlotSurvey getSurveyById(Long id) {
        return surveyRepository.findById(id).orElse(null);
    }

    @Transactional
    public void voteForTimeSlot(Long timeSlotId, User user) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId).orElse(null);
        if (timeSlot != null && !timeSlot.getVoters().contains(user)) {
            timeSlot.getVoters().add(user);
            timeSlotRepository.save(timeSlot);
        }
    }

    @Transactional
    public void removeVoteForTimeSlot(Long timeSlotId, User user) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId).orElse(null);
        if (timeSlot != null) {
            timeSlot.getVoters().remove(user);
            timeSlotRepository.save(timeSlot);
        }
    }

    @Transactional
    public Event createEventFromSurvey(Long surveyId, Long timeSlotId, String location) {
        TimeSlotSurvey survey = getSurveyById(surveyId);
        if (survey == null || survey.isClosed()) {
            return null;
        }

        TimeSlot selectedTimeSlot = survey.getTimeSlots().stream()
                .filter(ts -> ts.getId().equals(timeSlotId))
                .findFirst()
                .orElse(null);

        if (selectedTimeSlot == null) {
            return null;
        }

        // Create event
        // If no end time is set, default to 2 hours after start time
        LocalDateTime endTime = selectedTimeSlot.getEndTime();
        if (endTime == null) {
            endTime = selectedTimeSlot.getStartTime().plusHours(2);
        }

        Event event = new Event(
                survey.getTitle(),
                survey.getDescription(),
                selectedTimeSlot.getStartTime(),
                endTime,
                survey.getCreator()
        );
        event.setLocation(location);

        // Link event to large event plan if survey was created from one
        if (survey.getLargeEventPlan() != null) {
            event.setLargeEventPlan(survey.getLargeEventPlan());
            // Inherit visibility settings from large event plan
            event.setOpenToAll(survey.getLargeEventPlan().isOpenToAll());
            if (!survey.getLargeEventPlan().isOpenToAll()) {
                event.getInvitedUsers().addAll(survey.getLargeEventPlan().getInvitedUsers());
            }
        } else {
            // Otherwise inherit from survey
            event.setOpenToAll(survey.isOpenToAll());
            if (!survey.isOpenToAll()) {
                event.getInvitedUsers().addAll(survey.getInvitedUsers());
            }
        }

        // Add all voters as participants
        for (User voter : selectedTimeSlot.getVoters()) {
            if (!voter.getId().equals(survey.getCreator().getId())) {
                event.getParticipants().add(voter);
            }
        }

        Event savedEvent = eventService.createEvent(event);

        // Close survey and link event
        survey.setClosed(true);
        survey.setClosedAt(LocalDateTime.now());
        survey.setCreatedEvent(savedEvent);
        surveyRepository.save(survey);

        return savedEvent;
    }

    @Transactional
    public void closeSurvey(Long surveyId) {
        TimeSlotSurvey survey = getSurveyById(surveyId);
        if (survey != null && !survey.isClosed()) {
            survey.setClosed(true);
            survey.setClosedAt(LocalDateTime.now());
            surveyRepository.save(survey);
        }
    }

    @Transactional
    public void deleteSurvey(Long id) {
        surveyRepository.deleteById(id);
    }
}
