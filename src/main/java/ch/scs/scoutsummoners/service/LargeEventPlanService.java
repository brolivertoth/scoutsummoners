package ch.scs.scoutsummoners.service;

import ch.scs.scoutsummoners.entity.LargeEventPlan;
import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.repository.LargeEventPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LargeEventPlanService {

    @Autowired
    private LargeEventPlanRepository largeEventPlanRepository;

    public LargeEventPlan createPlan(LargeEventPlan plan) {
        return largeEventPlanRepository.save(plan);
    }

    public List<LargeEventPlan> getUserPlans(User user) {
        return largeEventPlanRepository.findByCreatorOrderByUpdatedAtDesc(user);
    }

    public List<LargeEventPlan> getPlansVisibleToUser(User user) {
        List<LargeEventPlan> allPlans = largeEventPlanRepository.findAllByOrderByUpdatedAtDesc();
        return allPlans.stream()
                .filter(plan -> {
                    // If open to all, everyone can see it
                    if (plan.isOpenToAll()) {
                        return true;
                    }
                    // If user is the creator, they can see it
                    if (plan.getCreator().getId().equals(user.getId())) {
                        return true;
                    }
                    // Check if user is in invited users list
                    return plan.getInvitedUsers().stream()
                            .anyMatch(invitedUser -> invitedUser.getId().equals(user.getId()));
                })
                .toList();
    }

    public List<LargeEventPlan> getAllPlans() {
        return largeEventPlanRepository.findAllByOrderByUpdatedAtDesc();
    }

    public boolean hasAccess(LargeEventPlan plan, User user) {
        return plan.isOpenToAll()
                || plan.getCreator().getId().equals(user.getId())
                || plan.getInvitedUsers().contains(user);
    }

    public LargeEventPlan getPlanById(Long id) {
        return largeEventPlanRepository.findById(id).orElse(null);
    }

    public LargeEventPlan updatePlan(LargeEventPlan plan) {
        plan.setUpdatedAt(LocalDateTime.now());
        return largeEventPlanRepository.save(plan);
    }

    public void deletePlan(Long id) {
        largeEventPlanRepository.deleteById(id);
    }
}
