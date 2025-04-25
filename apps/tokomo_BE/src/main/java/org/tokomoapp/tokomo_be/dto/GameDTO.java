package org.tokomoapp.tokomo_be.dto;
import org.tokomoapp.tokomo_be.model.Game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDTO {
    private String downloadUrl;
    private String extractPassword;
    private String gameName;
    private String gameType;
    private String note;
    private String password;

    public Game toGame(){
        Game game = new Game();
        game.setDownloadUrl(downloadUrl);
        game.setExtractPassword(extractPassword);
        game.setGameName(gameName);
        game.setGameType(gameType);
        game.setNote(note);
        game.setPassword(password);
        return game;
    }


}
