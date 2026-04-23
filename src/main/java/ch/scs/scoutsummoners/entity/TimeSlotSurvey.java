package ch.scs.scoutsummoners.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "time_slot_surveys")
public class TimeSlotSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"createdEvents", "participatingEvents", "password"})
    private User creator;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeSlot> timeSlots = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime closedAt;

    private boolean closed = false;

    @OneToOne
    @JoinColumn(name = "created_event_id")
    @JsonIgnoreProperties({"creator", "participants"})
    private Event createdEvent;

    @Column(nullable = false)
    private boolean openToAll = true;

    @ManyToMany
    @JoinTable(
        name = "survey_invitations",
        joinColumns = @JoinColumn(name = "survey_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"createdEvents", "participatingEvents", "password"})
    private List<User> invitedUsers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "large_event_plan_id")
    private LargeEventPlan largeEventPlan;

    public TimeSlotSurvey() {
    }
    
    public TimeSlotSurvey(String title, String description, User creator) {
        this.title = title;
        this.description = description;
        this.creator = creator;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }
    
    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getClosedAt() {
        return closedAt;
    }
    
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
    
    public boolean isClosed() {
        return closed;
    }
    
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    public Event getCreatedEvent() {
        return createdEvent;
    }

    public void setCreatedEvent(Event createdEvent) {
        this.createdEvent = createdEvent;
    }

    public boolean isOpenToAll() {
        return openToAll;
    }

    public void setOpenToAll(boolean openToAll) {
        this.openToAll = openToAll;
    }

    public List<User> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(List<User> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }

    public LargeEventPlan getLargeEventPlan() {
        return largeEventPlan;
    }

    public void setLargeEventPlan(LargeEventPlan largeEventPlan) {
        this.largeEventPlan = largeEventPlan;
    }
}
