package com.kings.model.phases;

import java.util.List;

import com.kings.http.GameMessage;
import com.kings.model.GameState;
import com.kings.model.Player;

public class RecruitThingsPhase extends Phase {

	public RecruitThingsPhase(GameState gameState,
			List<Player> playersInOrderOfTurn) {
		super(gameState, playersInOrderOfTurn);
		setPhaseId("recruitThings");
	}

	@Override
	public void handleStart() {
		// Get all the possible things the player is able to to recruit and tell the client
		// Then wait for the client to tell us which of these things they actually recruited
		

	}

	@Override
	public void setupNextPhase() {
		setNextPhase(new MovementPhase(getGameState(), getPlayersInOrderOfTurn()));
	}

	@Override
	public GameMessage getPhaseOverMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GameMessage getPhaseStartedMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
