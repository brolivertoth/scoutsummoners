package ch.scs.scoutsummoners.controller;

import ch.scs.scoutsummoners.entity.Event;
import ch.scs.scoutsummoners.entity.TimeSlot;
import ch.scs.scoutsummoners.entity.TimeSlotSurvey;
import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.service.LargeEventPlanService;
import ch.scs.scoutsummoners.service.SurveyService;
import ch.scs.scoutsummoners.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/surveys")
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private UserService userService;

    @Autowired
    private LargeEventPlanService largeEventPlanService;

    @GetMapping
    public String listSurveys(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("surveys", surveyService.getSurveysVisibleToUser(user));
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));
        return "surveys";
    }

    @GetMapping("/new")
    public String showCreateSurveyForm(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        List<User> allUsers = userService.findAll();
        model.addAttribute("user", user);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));
        return "survey-form";
    }

    @PostMapping("/create")
    public String createSurvey(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam("startTimes") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) List<LocalDateTime> startTimes,
            @RequestParam(value = "endTimes", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) List<LocalDateTime> endTimes,
            @RequestParam(value = "hasEndTimes", required = false) List<String> hasEndTimes,
            @RequestParam(value = "openToAll", defaultValue = "true") boolean openToAll,
            @RequestParam(value = "invitedUserIds", required = false) List<Long> invitedUserIds,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        TimeSlotSurvey survey = new TimeSlotSurvey(title, description, user);
        survey.setOpenToAll(openToAll);

        // Add invited users if not open to all
        if (!openToAll && invitedUserIds != null && !invitedUserIds.isEmpty()) {
            for (Long userId : invitedUserIds) {
                User invitedUser = userService.findById(userId);
                if (invitedUser != null) {
                    survey.getInvitedUsers().add(invitedUser);
                }
            }
        }

        // Create time slots
        for (int i = 0; i < startTimes.size(); i++) {
            boolean hasEnd = hasEndTimes != null && i < hasEndTimes.size() && "true".equals(hasEndTimes.get(i));
            LocalDateTime endTime = hasEnd && endTimes != null && i < endTimes.size() ? endTimes.get(i) : null;

            TimeSlot timeSlot = new TimeSlot(survey, startTimes.get(i), endTime);
            timeSlot.setHasEndTime(hasEnd);
            survey.getTimeSlots().add(timeSlot);
        }

        surveyService.createSurvey(survey);
        return "redirect:/surveys";
    }

    @GetMapping("/{id}")
    public String viewSurvey(@PathVariable Long id, Authentication authentication, Model model) {
        TimeSlotSurvey survey = surveyService.getSurveyById(id);
        User user = userService.findByUsername(authentication.getName());

        if (survey == null) {
            return "redirect:/surveys";
        }

        // Check if user has access to this survey
        boolean hasAccess = survey.isOpenToAll()
                || survey.getCreator().getId().equals(user.getId())
                || survey.getInvitedUsers().contains(user);

        if (!hasAccess) {
            return "redirect:/surveys";
        }

        model.addAttribute("survey", survey);
        model.addAttribute("currentUser", user);
        model.addAttribute("isCreator", survey.getCreator().getId().equals(user.getId()));
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));

        return "survey-detail";
    }

    @PostMapping("/{surveyId}/vote/{timeSlotId}")
    public String voteForTimeSlot(
            @PathVariable Long surveyId,
            @PathVariable Long timeSlotId,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        surveyService.voteForTimeSlot(timeSlotId, user);

        return "redirect:/surveys/" + surveyId;
    }

    @PostMapping("/{surveyId}/unvote/{timeSlotId}")
    public String removeVoteForTimeSlot(
            @PathVariable Long surveyId,
            @PathVariable Long timeSlotId,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        surveyService.removeVoteForTimeSlot(timeSlotId, user);

        return "redirect:/surveys/" + surveyId;
    }

    @PostMapping("/{surveyId}/create-event")
    public String createEventFromSurvey(
            @PathVariable Long surveyId,
            @RequestParam Long timeSlotId,
            @RequestParam(required = false) String location,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        TimeSlotSurvey survey = surveyService.getSurveyById(surveyId);

        if (survey == null || !survey.getCreator().getId().equals(user.getId())) {
            return "redirect:/surveys/" + surveyId;
        }

        Event event = surveyService.createEventFromSurvey(surveyId, timeSlotId, location);

        if (event != null) {
            return "redirect:/events/" + event.getId();
        }

        return "redirect:/surveys/" + surveyId;
    }

    @PostMapping("/{id}/close")
    public String closeSurvey(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        TimeSlotSurvey survey = surveyService.getSurveyById(id);

        if (survey != null && survey.getCreator().getId().equals(user.getId())) {
            surveyService.closeSurvey(id);
        }

        return "redirect:/surveys/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteSurvey(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        TimeSlotSurvey survey = surveyService.getSurveyById(id);

        if (survey != null && survey.getCreator().getId().equals(user.getId())) {
            surveyService.deleteSurvey(id);
        }

        return "redirect:/surveys";
    }
}
