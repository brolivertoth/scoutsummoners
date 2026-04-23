package ch.scs.scoutsummoners.repository;

import ch.scs.scoutsummoners.entity.Comment;
import ch.scs.scoutsummoners.entity.LargeEventPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByLargeEventPlanAndParentCommentIsNullOrderByCreatedAtAsc(LargeEventPlan largeEventPlan);
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);
}
