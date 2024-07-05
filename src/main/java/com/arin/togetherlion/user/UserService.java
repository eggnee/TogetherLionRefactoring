package com.arin.togetherlion.user;

import com.arin.togetherlion.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Transactional
    public void usePoint(User user, int cost) {
        user.getPoint().use(cost);
    }
}
