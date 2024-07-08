package com.arin.togetherlion.copurchasing.domain;

import com.arin.togetherlion.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participations {

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Participation> participations = new ArrayList<>();

    public void add(Participation participation) {
        participations.add(participation);
    }

    public boolean isParticipant(User participant) {
        return participations.stream()
                .anyMatch(participation -> participation.isParticipant(participant));
    }

    public int getTotalProductNumber() {
        return participations.stream()
                .mapToInt(Participation::getPurchaseNumber)
                .sum();
    }
}
