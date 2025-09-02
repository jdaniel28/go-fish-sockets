package com.danny.fish.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerHand {
	private String playerId;
	private List<Card> playerCards;
	private boolean currentPlayer;
}
