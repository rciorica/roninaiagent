package com.ronin.users;

import com.ronin.ranking.RankEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserEntity getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/{id}/rank")
    public RankEntity getRank(@PathVariable Long id) {
        return userService.getRankForUser(id);
    }

    @GetMapping("/me/rank")
    public RankEntity getCurrentUserRank() {
        return userService.getCurrentUserRank();
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser() {
        return userService.getCurrentUserProfile();
    }
}
