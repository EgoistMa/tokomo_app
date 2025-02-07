package org.tokomoapp.tokomo_be.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tokomoapp.tokomo_be.repository.UserGameRepository;
import org.tokomoapp.tokomo_be.model.Game;
import org.tokomoapp.tokomo_be.model.User;
import org.tokomoapp.tokomo_be.model.UserGame;
import org.tokomoapp.tokomo_be.service.UserGameService;

@Service
public class UserGameServiceImpl implements UserGameService {
    @Autowired
    private UserGameRepository userGameRepository;

    @Override
    public boolean existsByUserIdAndGameId(Long userId, Long gameId) {
        return userGameRepository.existsByUserIdAndGameId(userId, gameId);
    }
    

    @Override
    public UserGame save(User user, Game game) {
        UserGame userGame = new UserGame(user, game);
        return userGameRepository.save(userGame);
    }

    @Override
    public List<UserGame> getPurchaseHistory(Long userId) {
        return userGameRepository.findByUserId(userId);
    }
}
