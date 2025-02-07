package org.tokomoapp.tokomo_be.service;


import java.util.List;

import org.tokomoapp.tokomo_be.model.Game;
import org.tokomoapp.tokomo_be.model.User;
import org.tokomoapp.tokomo_be.model.UserGame;

public interface UserGameService {
        
    boolean existsByUserIdAndGameId(Long userId, Long gameId);

    UserGame save(User user, Game game);

    List<UserGame> getPurchaseHistory(Long userId);
}
