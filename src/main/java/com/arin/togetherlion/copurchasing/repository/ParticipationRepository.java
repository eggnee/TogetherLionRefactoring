package com.arin.togetherlion.copurchasing.repository;

import com.arin.togetherlion.copurchasing.domain.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
}
