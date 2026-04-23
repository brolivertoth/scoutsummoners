package ch.scs.scoutsummoners.repository;

import ch.scs.scoutsummoners.entity.Attachment;
import ch.scs.scoutsummoners.entity.Comment;
import ch.scs.scoutsummoners.entity.LargeEventPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByLargeEventPlan(LargeEventPlan largeEventPlan);
    List<Attachment> findByComment(Comment comment);
}
