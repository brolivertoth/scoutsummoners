package ch.scs.scoutsummoners.repository;

import ch.scs.scoutsummoners.entity.LargeEventPlan;
import ch.scs.scoutsummoners.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LargeEventPlanRepository extends JpaRepository<LargeEventPlan, Long> {
    List<LargeEventPlan> findByCreatorOrderByUpdatedAtDesc(User creator);
    List<LargeEventPlan> findAllByOrderByUpdatedAtDesc();
}
