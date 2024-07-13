package com.arin.togetherlion.copurchasing.repository;

import com.arin.togetherlion.copurchasing.domain.Copurchasing;
import com.arin.togetherlion.copurchasing.domain.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CopurchasingRepository extends JpaRepository<Copurchasing, Long> {

    @Query("SELECT c FROM Copurchasing c JOIN c.participations.participations p WHERE p = :participation")
    Optional<Copurchasing> findByParticipation(@Param("participation") Participation participation);

}
