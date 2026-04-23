package ch.scs.scoutsummoners.controller;

import ch.scs.scoutsummoners.entity.*;
import ch.scs.scoutsummoners.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/large-event-planning")
public class LargeEventPlanController {

    @Autowired
    private LargeEventPlanService largeEventPlanService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private SurveyService surveyService;

    @GetMapping
    public String showLargeEventPlanning(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("plans", largeEventPlanService.getPlansVisibleToUser(user));
        model.addAttribute("allUsers", userService.findAll());
        return "large-event-planning";
    }

    @PostMapping("/create")
    public String createPlan(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(value = "openToAll", defaultValue = "true") boolean openToAll,
            @RequestParam(value = "invitedUserIds", required = false) java.util.List<Long> invitedUserIds,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        LargeEventPlan plan = new LargeEventPlan(name, description, user);
        plan.setOpenToAll(openToAll);

        // Add invited users if not open to all
        if (!openToAll && invitedUserIds != null && !invitedUserIds.isEmpty()) {
            for (Long userId : invitedUserIds) {
                User invitedUser = userService.findById(userId);
                if (invitedUser != null) {
                    plan.getInvitedUsers().add(invitedUser);
                }
            }
        }

        LargeEventPlan savedPlan = largeEventPlanService.createPlan(plan);

        return "redirect:/large-event-planning/" + savedPlan.getId();
    }

    @GetMapping("/{id}")
    public String viewPlan(@PathVariable Long id, Model model, Authentication authentication) {
        LargeEventPlan plan = largeEventPlanService.getPlanById(id);
        User user = userService.findByUsername(authentication.getName());

        if (plan == null) {
            return "redirect:/large-event-planning";
        }

        // Check if user has access
        if (!largeEventPlanService.hasAccess(plan, user)) {
            return "redirect:/large-event-planning";
        }

        model.addAttribute("plan", plan);
        model.addAttribute("user", user);
        model.addAttribute("allUsers", userService.findAll());
        model.addAttribute("isCreator", plan.getCreator().getId().equals(user.getId()));
        model.addAttribute("plans", largeEventPlanService.getPlansVisibleToUser(user));

        // Load survey data if it exists
        if (plan.getEventSurvey() != null) {
            TimeSlotSurvey survey = surveyService.getSurveyById(plan.getEventSurvey().getId());
            model.addAttribute("survey", survey);
        }

        return "large-event-plan-detail";
    }

    @PostMapping("/{id}/update")
    public String updatePlan(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(value = "openToAll", defaultValue = "false") boolean openToAll,
            @RequestParam(value = "invitedUserIds", required = false) java.util.List<Long> invitedUserIds,
            Authentication authentication) {

        LargeEventPlan plan = largeEventPlanService.getPlanById(id);
        User user = userService.findByUsername(authentication.getName());

        if (plan == null || !plan.getCreator().getId().equals(user.getId())) {
            return "redirect:/large-event-planning";
        }

        plan.setName(name);
        plan.setDescription(description);
        plan.setOpenToAll(openToAll);
        plan.getInvitedUsers().clear();

        // Add invited users if not open to all
        if (!openToAll && invitedUserIds != null && !invitedUserIds.isEmpty()) {
            for (Long userId : invitedUserIds) {
                User invitedUser = userService.findById(userId);
                if (invitedUser != null) {
                    plan.getInvitedUsers().add(invitedUser);
                }
            }
        }

        largeEventPlanService.updatePlan(plan);

        return "redirect:/large-event-planning/" + id;
    }

    @PostMapping("/{id}/update-visibility")
    public String updateVisibility(
            @PathVariable Long id,
            @RequestParam(value = "openToAll", defaultValue = "false") boolean openToAll,
            @RequestParam(value = "invitedUserIds", required = false) java.util.List<Long> invitedUserIds,
            Authentication authentication) {

        LargeEventPlan plan = largeEventPlanService.getPlanById(id);
        User user = userService.findByUsername(authentication.getName());

        if (plan == null || !plan.getCreator().getId().equals(user.getId())) {
            return "redirect:/large-event-planning";
        }

        plan.setOpenToAll(openToAll);
        plan.getInvitedUsers().clear();

        // Add invited users if not open to all
        if (!openToAll && invitedUserIds != null && !invitedUserIds.isEmpty()) {
            for (Long userId : invitedUserIds) {
                User invitedUser = userService.findById(userId);
                if (invitedUser != null) {
                    plan.getInvitedUsers().add(invitedUser);
                }
            }
        }

        largeEventPlanService.updatePlan(plan);

        return "redirect:/large-event-planning/" + id;
    }

    @PostMapping("/{id}/update-content")
    public String updateContent(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication authentication) {

        LargeEventPlan plan = largeEventPlanService.getPlanById(id);
        User user = userService.findByUsername(authentication.getName());

        if (plan == null || !plan.getCreator().getId().equals(user.getId())) {
            return "redirect:/large-event-planning";
        }

        plan.setContent(content);
        largeEventPlanService.updatePlan(plan);

        return "redirect:/large-event-planning/" + id;
    }

    @PostMapping("/{id}/add-comment")
    public String addComment(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam String content,
            @RequestParam(required = false) Long parentCommentId,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
            Authentication authentication) throws IOException {

        LargeEventPlan plan = largeEventPlanService.getPlanById(id);
        User user = userService.findByUsername(authentication.getName());

        if (plan == null) {
            return "redirect:/large-event-planning";
        }

        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentService.getCommentById(parentCommentId);
        }

        Comment comment = commentService.createComment(title, content, user, plan, parentComment);

        // Handle attachments
        if (attachments != null) {
            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    attachmentService.saveAttachment(file, user, null, comment);
                }
            }
        }

        return "redirect:/large-event-planning/" + id;
    }

    @PostMapping("/{planId}/upload-attachment")
    public String uploadAttachment(
            @PathVariable Long planId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        LargeEventPlan plan = largeEventPlanService.getPlanById(planId);
        User user = userService.findByUsername(authentication.getName());

        if (plan == null || !plan.getCreator().getId().equals(user.getId())) {
            return "redirect:/large-event-planning";
        }

        if (!file.isEmpty()) {
            attachmentService.saveAttachment(file, user, plan, null);
        }

        return "redirect:/large-event-planning/" + planId;
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) throws IOException {
        Attachment attachment = attachmentService.getAttachmentById(id);

        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(attachment.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, Authentication authentication) {
        Comment comment = commentService.getCommentById(id);
        User user = userService.findByUsername(authentication.getName());

        if (comment != null && comment.getAuthor().getId().equals(user.getId())) {
            Long planId = comment.getLargeEventPlan().getId();
            commentService.deleteComment(id);
            return "redirect:/large-event-planning/" + planId;
        }

        return "redirect:/large-event-planning";
    }

    @PostMapping("/attachments/{id}/delete")
    public String deleteAttachment(@PathVariable Long id, Authentication authentication) throws IOException {
        Attachment attachment = attachmentService.getAttachmentById(id);
        User user = userService.findByUsername(authentication.getName());

        if (attachment != null && attachment.getUploadedBy().getId().equals(user.getId())) {
            Long planId = attachment.getLargeEventPlan() != null ?
                         attachment.getLargeEventPlan().getId() :
                         attachment.getComment().getLargeEventPlan().getId();
            attachmentService.deleteAttachment(id);
            return "redirect:/large-event-planning/" + planId;
        }

        return "redirect:/large-event-planning";
    }

    @PostMapping("/{id}/delete")
    public String deletePlan(@PathVariable Long id, Authentication authentication) {
        LargeEventPlan plan = largeEventPlanService.getPlanById(id);
        User user = userService.findByUsername(authentication.getName());

        if (plan != null && plan.getCreator().getId().equals(user.getId())) {
            largeEventPlanService.deletePlan(id);
        }

        return "redirect:/large-event-planning";
    }

    @PostMapping("/{id}/create-survey")
    public String createSurvey(
            @PathVariable Long id,
            @RequestParam String surveyTitle,
            @RequestParam String surveyDescription,
            @RequestParam("startTimes") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.util.List<java.time.LocalDateTime> startTimes,
            @RequestParam(value = "endTimes", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.util.List<java.time.LocalDateTime> endTimes,
            @RequestParam(value = "hasEndTimes", required = false) java.util.List<String> hasEndTimes,
            Authentication authentication) {

        LargeEventPlan plan = largeEventPlanService.getPlanById(id);
        User user = userService.findByUsername(authentication.getName());

        if (plan == null || !plan.getCreator().getId().equals(user.getId())) {
            return "redirect:/large-event-planning/" + id;
        }

        // Create the survey
        TimeSlotSurvey survey = new TimeSlotSurvey(surveyTitle, surveyDescription, user);
        survey.setOpenToAll(plan.isOpenToAll());

        // Copy invited users from plan to survey
        if (!plan.isOpenToAll()) {
            for (User invitedUser : plan.getInvitedUsers()) {
                survey.getInvitedUsers().add(invitedUser);
            }
        }

        // Create time slots
        for (int i = 0; i < startTimes.size(); i++) {
            boolean hasEnd = hasEndTimes != null && i < hasEndTimes.size() && "true".equals(hasEndTimes.get(i));
            java.time.LocalDateTime endTime = hasEnd && endTimes != null && i < endTimes.size() ? endTimes.get(i) : null;

            TimeSlot timeSlot = new TimeSlot(survey, startTimes.get(i), endTime);
            timeSlot.setHasEndTime(hasEnd);
            survey.getTimeSlots().add(timeSlot);
        }

        TimeSlotSurvey savedSurvey = surveyService.createSurvey(survey);

        // Link survey to plan
        plan.setEventSurvey(savedSurvey);
        largeEventPlanService.updatePlan(plan);

        return "redirect:/large-event-planning/" + id;
    }
}
