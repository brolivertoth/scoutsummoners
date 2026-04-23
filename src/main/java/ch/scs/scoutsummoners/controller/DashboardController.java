package ch.scs.scoutsummoners.controller;

import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.service.EventService;
import ch.scs.scoutsummoners.service.LargeEventPlanService;
import ch.scs.scoutsummoners.service.SurveyService;
import ch.scs.scoutsummoners.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
public class DashboardController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private LargeEventPlanService largeEventPlanService;

    @Autowired
    private SurveyService surveyService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());

        // Get upcoming events (within next 30 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthFromNow = now.plusMonths(1);
        model.addAttribute("upcomingEvents", eventService.getUpcomingEventsForUser(user, now, oneMonthFromNow));

        // Get open surveys visible to user
        model.addAttribute("openSurveys", surveyService.getOpenSurveysForUser(user));

        // Get large event plans user is part of
        model.addAttribute("largeEventPlans", largeEventPlanService.getPlansVisibleToUser(user));

        model.addAttribute("user", user);
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));
        return "dashboard";
    }
}
