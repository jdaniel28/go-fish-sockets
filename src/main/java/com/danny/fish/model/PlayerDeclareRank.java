package com.danny.fish.model;

import com.danny.fish.constants.CardRank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDeclareRank {
	private long gameId;
	private long playerId;
	private CardRank declaredRank;
}
