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
public class PlayerRequestCard {
	private long gameId;
	private long requestingPlayerId;
	private long requestedPlayerId;
	private CardRank askedRank;
}
