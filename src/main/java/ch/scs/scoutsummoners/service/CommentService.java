package ch.scs.scoutsummoners.service;

import ch.scs.scoutsummoners.entity.Comment;
import ch.scs.scoutsummoners.entity.LargeEventPlan;
import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment createComment(String title, String content, User author, LargeEventPlan plan, Comment parentComment) {
        Comment comment = new Comment(content, author, plan);
        comment.setTitle(title);
        comment.setParentComment(parentComment);
        return commentRepository.save(comment);
    }

    public List<Comment> getTopLevelComments(LargeEventPlan plan) {
        return commentRepository.findByLargeEventPlanAndParentCommentIsNullOrderByCreatedAtAsc(plan);
    }

    public List<Comment> getReplies(Comment parentComment) {
        return commentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public Comment updateComment(Comment comment) {
        return commentRepository.save(comment);
    }
}
