package ch.scs.scoutsummoners.controller;

import ch.scs.scoutsummoners.dto.EventDTO;
import ch.scs.scoutsummoners.entity.Event;
import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.service.EventService;
import ch.scs.scoutsummoners.service.LargeEventPlanService;
import ch.scs.scoutsummoners.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private LargeEventPlanService largeEventPlanService;

    @GetMapping
    public String showCalendar(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("allUsers", userService.findAll());
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));
        return "calendar";
    }

    @GetMapping("/events")
    @ResponseBody
    public List<EventDTO> getEventsForMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        return eventService.getEventsVisibleToUserBetween(user, start, end)
                .stream()
                .map(EventDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/events/quick-create")
    @ResponseBody
    public EventDTO quickCreateEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String location,
            @RequestParam(value = "openToAll", defaultValue = "true") boolean openToAll,
            @RequestParam(value = "invitedUserIds", required = false) List<Long> invitedUserIds,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        Event event = new Event(title, description, startTime, endTime, user);
        event.setLocation(location);
        event.setOpenToAll(openToAll);

        // Add invited users if not open to all
        if (!openToAll && invitedUserIds != null && !invitedUserIds.isEmpty()) {
            for (Long userId : invitedUserIds) {
                User invitedUser = userService.findById(userId);
                if (invitedUser != null) {
                    event.getInvitedUsers().add(invitedUser);
                }
            }
        }

        Event savedEvent = eventService.createEvent(event);
        return new EventDTO(savedEvent);
    }
}
