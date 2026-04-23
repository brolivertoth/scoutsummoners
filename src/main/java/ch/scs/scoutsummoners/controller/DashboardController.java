package ch.scs.scoutsummoners.controller;

import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.service.EventService;
import ch.scs.scoutsummoners.service.LargeEventPlanService;
import ch.scs.scoutsummoners.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private LargeEventPlanService largeEventPlanService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("events", eventService.getEventsVisibleToUser(user));
        model.addAttribute("userPlans", largeEventPlanService.getPlansVisibleToUser(user));
        return "dashboard";
    }
}
