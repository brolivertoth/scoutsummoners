package ch.scs.scoutsummoners.controller;

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

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private LargeEventPlanService largeEventPlanService;

    @GetMapping
    public String showEvents(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("events", eventService.getUserEvents(user));
        model.addAttribute("user", user);
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));
        return "events";
    }

    @GetMapping("/new")
    public String showCreateEventForm(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("allUsers", userService.findAll());
        model.addAttribute("user", user);
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));
        return "event-form";
    }

    @PostMapping("/create")
    public String createEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String location,
            @RequestParam(value = "openToAll", defaultValue = "true") boolean openToAll,
            @RequestParam(value = "invitedUserIds", required = false) java.util.List<Long> invitedUserIds,
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

        eventService.createEvent(event);

        return "redirect:/events";
    }

    @GetMapping("/{id}")
    public String viewEvent(@PathVariable Long id, Model model, Authentication authentication) {
        Event event = eventService.getEventById(id);
        User user = userService.findByUsername(authentication.getName());

        if (event == null) {
            return "redirect:/events";
        }

        // Check if user has access to this event
        boolean hasAccess = event.isOpenToAll()
                || event.getCreator().getId().equals(user.getId())
                || event.getParticipants().contains(user)
                || event.getInvitedUsers().contains(user);

        if (!hasAccess) {
            return "redirect:/events";
        }

        model.addAttribute("event", event);
        model.addAttribute("currentUser", user);
        model.addAttribute("isCreator", event.getCreator().getId().equals(user.getId()));
        model.addAttribute("isParticipant", event.getParticipants().contains(user));
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));

        return "event-detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Event event = eventService.getEventById(id);
        User user = userService.findByUsername(authentication.getName());

        if (event == null || !event.getCreator().getId().equals(user.getId())) {
            return "redirect:/events";
        }

        model.addAttribute("event", event);
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));
        return "event-edit";
    }

    @PostMapping("/{id}/update")
    public String updateEvent(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String location,
            Authentication authentication) {

        Event event = eventService.getEventById(id);
        User user = userService.findByUsername(authentication.getName());

        if (event == null || !event.getCreator().getId().equals(user.getId())) {
            return "redirect:/events";
        }

        event.setTitle(title);
        event.setDescription(description);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setLocation(location);
        eventService.updateEvent(event);

        return "redirect:/events/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, Authentication authentication) {
        Event event = eventService.getEventById(id);
        User user = userService.findByUsername(authentication.getName());

        if (event != null && event.getCreator().getId().equals(user.getId())) {
            eventService.deleteEvent(id);
        }

        return "redirect:/events";
    }

    @PostMapping("/{id}/join")
    public String joinEvent(@PathVariable Long id, Authentication authentication) {
        Event event = eventService.getEventById(id);
        User user = userService.findByUsername(authentication.getName());

        if (event != null) {
            eventService.addParticipant(event, user);
        }

        return "redirect:/events/" + id;
    }

    @PostMapping("/{id}/leave")
    public String leaveEvent(@PathVariable Long id, Authentication authentication) {
        Event event = eventService.getEventById(id);
        User user = userService.findByUsername(authentication.getName());

        if (event != null) {
            eventService.removeParticipant(event, user);
        }

        return "redirect:/events/" + id;
    }
}
