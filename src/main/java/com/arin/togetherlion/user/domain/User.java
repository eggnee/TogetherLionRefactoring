package com.arin.togetherlion.user.domain;

import com.arin.togetherlion.common.BaseTimeEntity;
import com.arin.togetherlion.point.domain.Point;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Embedded
    private Point point;

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.point = new Point(0);
    }

    public boolean isSameUser(User otherUser) {
        return this.id.equals(otherUser.getId());
    }

    public void pay(int paymentCost) {
        this.getPoint().use(paymentCost);
    }

    public void refund(int refundCost) {
        this.getPoint().add(refundCost);
    }
}
