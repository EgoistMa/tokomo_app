package org.tokomoapp.tokomo_be.dto;

import java.time.LocalDateTime;
import org.tokomoapp.tokomo_be.model.Game;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGameDTO {
    private Long id;
    private Long userId;
    private Game game;
    private LocalDateTime purchaseDate;
}
