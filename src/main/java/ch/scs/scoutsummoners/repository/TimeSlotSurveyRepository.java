package ch.scs.scoutsummoners.repository;

import ch.scs.scoutsummoners.entity.TimeSlotSurvey;
import ch.scs.scoutsummoners.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotSurveyRepository extends JpaRepository<TimeSlotSurvey, Long> {
    List<TimeSlotSurvey> findByCreatorOrderByCreatedAtDesc(User creator);
    List<TimeSlotSurvey> findByClosedFalseOrderByCreatedAtDesc();
    List<TimeSlotSurvey> findAllByOrderByCreatedAtDesc();
}
