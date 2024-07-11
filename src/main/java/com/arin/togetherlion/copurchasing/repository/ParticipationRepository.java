package com.arin.togetherlion.copurchasing.repository;

import com.arin.togetherlion.copurchasing.domain.Participation;
import com.arin.togetherlion.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByParticipant(User participant);
}
