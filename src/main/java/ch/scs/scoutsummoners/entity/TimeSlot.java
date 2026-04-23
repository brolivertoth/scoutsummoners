package ch.scs.scoutsummoners.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "survey_id", nullable = false)
    @JsonIgnore
    private TimeSlotSurvey survey;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = true)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean hasEndTime = true;

    @ManyToMany
    @JoinTable(
        name = "time_slot_votes",
        joinColumns = @JoinColumn(name = "time_slot_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"createdEvents", "participatingEvents", "password"})
    private List<User> voters = new ArrayList<>();
    
    public TimeSlot() {
    }
    
    public TimeSlot(TimeSlotSurvey survey, LocalDateTime startTime, LocalDateTime endTime) {
        this.survey = survey;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TimeSlotSurvey getSurvey() {
        return survey;
    }
    
    public void setSurvey(TimeSlotSurvey survey) {
        this.survey = survey;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public List<User> getVoters() {
        return voters;
    }

    public void setVoters(List<User> voters) {
        this.voters = voters;
    }

    public boolean isHasEndTime() {
        return hasEndTime;
    }

    public void setHasEndTime(boolean hasEndTime) {
        this.hasEndTime = hasEndTime;
    }
}
