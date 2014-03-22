package com.kings.test.phases;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.kings.http.HttpResponseMessage;
import com.kings.model.Game;
import com.kings.model.GamePiece;
import com.kings.model.GameState;
import com.kings.model.HexLocation;
import com.kings.model.Player;
import com.kings.model.SentMessage;
import com.kings.model.SpecialCharacter;
import com.kings.model.Stack;
import com.kings.model.User;
import com.kings.model.phases.CombatPhase;
import com.kings.model.phases.GoldCollectionPhase;
import com.kings.model.phases.MovementPhase;
import com.kings.model.phases.PlacementPhase;
import com.kings.model.phases.RecruitCharactersPhase;
import com.kings.model.phases.RecruitThingsPhase;
import com.kings.model.phases.SetupPhase;
import com.kings.model.phases.battle.CombatBattle;
import com.kings.model.phases.exceptions.NotYourTurnException;

public class RunThroughPhasesTest {
	private Game game;
	private User u1;
	private User u2;
	private User u3;
	private User u4;
	
	public GameState getNewGameState() throws Exception{
		game = new Game();
		game.setDemo(true);
		game.setActive(true);
		game.setGameId("1");
		game.setStartedDate(new Date());
		Set<User> users = new HashSet<User>();
		u1=new User("1");
		u1.setPort(3004);
		u1.setHostName("localhost");
		u1.setUsername("devin");
		u2=new User("2");
		u2.setUsername("john");
		u3=new User("3");
		u3.setUsername("devin");
		u4=new User("4");
		u4.setUsername("richard");
		users.add(u1);
		users.add(u2);
		users.add(u3);
		users.add(u4);
		game.setUsers(users);
		game.startAsTest();
		
		return game.getGameState();
	}
	
	@Test
	public void testRunThroughGame() throws Exception{
		GameState gs = getNewGameState();
		
		/** 
		 * Setup Phase
		 */
		
		assertEquals("setup", gs.getCurrentPhase().getPhaseId());
		
		// Go through setup phase
		SetupPhase sPhase = (SetupPhase)gs.getCurrentPhase();
		sPhase.playerIsReadyForPlacement("1");
		sPhase.playerIsReadyForPlacement("3");
		sPhase.playerIsReadyForPlacement("4");
		
		assertEquals("setup", gs.getCurrentPhase().getPhaseId());
		sPhase.playerIsReadyForPlacement("2");
		assertEquals("placement", gs.getCurrentPhase().getPhaseId());
		
		/**
		 * Placement Phase
		 */
		
		// Now do placement phase
		PlacementPhase pPhase = (PlacementPhase) gs.getCurrentPhase();
		pPhase.didPlaceControlMarkers("player1", "hexLocation_1", "hexLocation_2", "hexLocation_3");
		pPhase.didPlaceControlMarkers("player2", "hexLocation_4", "hexLocation_5", "hexLocation_6");
		pPhase.didPlaceControlMarkers("player3", "hexLocation_7", "hexLocation_8", "hexLocation_9");
		pPhase.didPlaceControlMarkers("player4", "hexLocation_10", "hexLocation_11", "hexLocation_12");
		
		assertEquals("player1", gs.getHexLocationsById("hexLocation_1").getOwner().getPlayerId());
		assertEquals("player2", gs.getHexLocationsById("hexLocation_5").getOwner().getPlayerId());
		assertEquals("player3", gs.getHexLocationsById("hexLocation_9").getOwner().getPlayerId());
		assertEquals("player4", gs.getHexLocationsById("hexLocation_12").getOwner().getPlayerId());
		
		Player p1 = gs.getPlayerByPlayerId("player1");
		GamePiece fort1 = (GamePiece) p1.getFortPieces().toArray()[0];
		assertEquals("Tower", fort1.getName());
		pPhase.didPlaceFort("player1", fort1.getId(), "hexLocation_1");
		assertEquals("hexLocation_1", fort1.getLocation().getId());
		
		// Try placing fort out of order
		boolean didGetNotTurnException = false;
		try{
			pPhase.didPlaceFort("player3", "", "");
		} catch(NotYourTurnException e) {
			didGetNotTurnException = true;
		}
		assertTrue(didGetNotTurnException);
		
		Player p2 = gs.getPlayerByPlayerId("player2");
		GamePiece fort2 = (GamePiece) p2.getFortPieces().toArray()[0];
		assertEquals("Tower", fort2.getName());
		pPhase.didPlaceFort("player2", fort2.getId(), "hexLocation_4");
		assertEquals("hexLocation_4", fort2.getLocation().getId());
		
		Player p3 = gs.getPlayerByPlayerId("player3");
		GamePiece fort3 = (GamePiece) p3.getFortPieces().toArray()[0];
		assertEquals("Tower", fort3.getName());
		pPhase.didPlaceFort("player3", fort3.getId(), "hexLocation_7");
		assertEquals("hexLocation_7", fort3.getLocation().getId());
		
		Player p4 = gs.getPlayerByPlayerId("player4");
		GamePiece fort4 = (GamePiece) p4.getFortPieces().toArray()[0];
		assertEquals("Tower", fort4.getName());
		pPhase.didPlaceFort("player4", fort4.getId(), "hexLocation_10");
		assertEquals("hexLocation_10", fort4.getLocation().getId());
		
		
		/**
		 * Movement Phase
		 */
		
		MovementPhase mPhase1 = (MovementPhase) gs.getCurrentPhase();
		mPhase1.playerIsDoneMakingMoves("player1");
		mPhase1.playerIsDoneMakingMoves("player2");
		mPhase1.playerIsDoneMakingMoves("player3");
		mPhase1.playerIsDoneMakingMoves("player4");
		
		/**
		 * Gold Collection Phase
		 */
		
		// Gold phase should be started and handled automatically, players should now have gold assigned
		assertEquals("gold", gs.getCurrentPhase().getPhaseId());
		
		assertEquals(new Integer(14), p1.getGold());
		assertEquals(new Integer(14), p2.getGold());
		assertEquals(new Integer(14), p3.getGold());
		assertEquals(new Integer(14), p4.getGold());
		
		GoldCollectionPhase cPhase = (GoldCollectionPhase) gs.getCurrentPhase();
		
		cPhase.playerIsReadyForNextPhase("player1");
		cPhase.playerIsReadyForNextPhase("player2");
		cPhase.playerIsReadyForNextPhase("player3");
		assertEquals("gold", gs.getCurrentPhase().getPhaseId());
		cPhase.playerIsReadyForNextPhase("player4");
		
		
		/**
		 * Special Character Recruitment
		 */
		assertEquals("recruitCharacters", gs.getCurrentPhase().getPhaseId());
		RecruitCharactersPhase rcPhase = (RecruitCharactersPhase)gs.getCurrentPhase(); 
		
		
		SpecialCharacter p1RCSPC = (SpecialCharacter)gs.getSideLocation().getGamePieceById("specialcharacter_01");
		SpecialCharacter p2RCSPC = (SpecialCharacter)gs.getSideLocation().getGamePieceById("specialcharacter_02");
		SpecialCharacter p3RCSPC = (SpecialCharacter)gs.getSideLocation().getGamePieceById("specialcharacter_03");
		SpecialCharacter p4RCSPC = (SpecialCharacter)gs.getSideLocation().getGamePieceById("specialcharacter_04");
		
		// Player 1 Special Characters - Did not recruit, no pre or post rolls
		assertEquals(rcPhase.getActualCurrentRound().getPlayer().getPlayerId(), p1.getPlayerId());
		gs.setDiceRollForTest(7);
		rcPhase.makeRollForPlayer(p1.getPlayerId(), p1RCSPC.getId(), 0);
		rcPhase.postRoll(p1.getPlayerId(), 0);
		assertFalse(rcPhase.getRoundForPlayer(p1.getPlayerId()).isDidRecruit());
		assertTrue(rcPhase.getRoundForPlayer(p1.getPlayerId()).isRoundOver());
		
		// Player 2 Special Characters - Recruited on first roll, no pre or post rolls
		assertEquals(rcPhase.getActualCurrentRound().getPlayer().getPlayerId(), p2.getPlayerId());
		gs.setDiceRollForTest(10);
		rcPhase.makeRollForPlayer(p2.getPlayerId(), p2RCSPC.getId(), 0);
		assertTrue(rcPhase.getRoundForPlayer(p2.getPlayerId()).isDidRecruit());
		assertTrue(rcPhase.getRoundForPlayer(p2.getPlayerId()).isRoundOver());
		
		// Player 3 Special Characters - Paid for 1 pre roll and did recruit
		assertEquals(rcPhase.getActualCurrentRound().getPlayer().getPlayerId(), p3.getPlayerId());
		gs.setDiceRollForTest(11);
		rcPhase.makeRollForPlayer(p3.getPlayerId(), p3RCSPC.getId(), 1);
		assertTrue(rcPhase.getRoundForPlayer(p3.getPlayerId()).isDidRecruit());
		assertTrue(rcPhase.getRoundForPlayer(p3.getPlayerId()).isRoundOver());
		
		// Player 4 Special Characters - Did not recruit on first roll, paid for 1 post roll
		assertEquals(rcPhase.getActualCurrentRound().getPlayer().getPlayerId(), p4.getPlayerId());
		gs.setDiceRollForTest(9);
		rcPhase.makeRollForPlayer(p4.getPlayerId(), p4RCSPC.getId(), 0);
		rcPhase.postRoll(p4.getPlayerId(), 1);
		assertTrue(rcPhase.getRoundForPlayer(p4.getPlayerId()).isDidRecruit());
		assertTrue(rcPhase.getRoundForPlayer(p4.getPlayerId()).isRoundOver());

		
		/**
		 * Recruit Things
		 */
		
		assertEquals("recruitThings", gs.getCurrentPhase().getPhaseId());
		RecruitThingsPhase rtPhase = (RecruitThingsPhase)gs.getCurrentPhase(); 
		rtPhase.didRecruitAndPlaceThing("player1", "T_Mountains_050-01", "player1_rack1", true);
		rtPhase.didRecruitAndPlaceThing("player1", "T_Mountains_034-01", "player1_rack2", false);
		rtPhase.didRecruitAndPlaceThing("player1", "T_Mountains_038-01", "player1_rack2", false);
		//assertEquals(1, p1.getRack1().getGamePieces().size());
		//assertEquals(2, p1.getRack2().getGamePieces().size());
		assertEquals(9, (int)p1.getGold());
		
		rtPhase.playerIsReadyForNextPhase("player1");
		rtPhase.playerIsReadyForNextPhase("player3");
		rtPhase.playerIsReadyForNextPhase("player4");
		rtPhase.playerIsReadyForNextPhase("player2");
		assertEquals("movement", gs.getCurrentPhase().getPhaseId());
		
		/**
		 * Movement
		 */
		
		MovementPhase mPhase = (MovementPhase)gs.getCurrentPhase();
		HexLocation p1HexForMov1 = p1.getOwnedLocationsAsList().get(0);
		HexLocation p1HexForMov2 = p1.getOwnedLocationsAsList().get(1);
		mPhase.didMoveGamePiece(p1.getPlayerId(), p1HexForMov1.getId(), "T_Mountains_050-01");
		assertEquals(p1HexForMov1.getId(), p1.getGamePieceById("T_Mountains_050-01").getLocation().getId());
		mPhase.didMoveGamePiece(p1.getPlayerId(), p1.getRack1().getId(), "T_Mountains_050-01");
		assertNotEquals(p1HexForMov1.getId(), p1.getGamePieceById("T_Mountains_050-01").getLocation().getId());
		assertEquals(p1.getRack1().getId(), p1.getGamePieceById("T_Mountains_050-01").getLocation().getId());
		
		List<String> p1MoveStackCreateList = new ArrayList<String>();
		p1MoveStackCreateList.add("T_Mountains_038-01");
		mPhase.didCreateStack(p1.getPlayerId(),  p1HexForMov1.getId(), p1MoveStackCreateList);
		assertEquals("Stack", p1.getGamePieceById("T_Mountains_038-01").getLocation().getName());
		List<Stack> p1StacksHex1 = new ArrayList<Stack>(p1HexForMov1.getStacks());
		assertEquals(1, p1StacksHex1.get(0).getGamePieces().size());
		List<String> p1MoveStackAddList = new ArrayList<String>();
		p1MoveStackAddList.add("T_Mountains_034-01");
		mPhase.didAddPiecesToStack(p1.getPlayerId(), p1StacksHex1.get(0).getId(), p1MoveStackAddList);
		assertEquals(2, p1StacksHex1.get(0).getGamePieces().size());
		
		mPhase.didMoveStack(p1.getPlayerId(), p1HexForMov2.getId(), p1StacksHex1.get(0).getId());
		assertEquals(0, p1HexForMov1.getStacks().size());
		assertEquals(1, p1HexForMov2.getStacks().size());
		
		mPhase.playerIsDoneMakingMoves(p1.getPlayerId());
		
		//play 2 moves
		p2.assignGamePieceToPlayer(gs.getGamePiece("T_Swamp_074-01"));
		List<String> p2ListOfPiecesForMove = new ArrayList<String>();
		p2ListOfPiecesForMove.add("T_Swamp_074-01");
		mPhase.didCreateStack(p2.getPlayerId(), p1HexForMov2.getId(), p2ListOfPiecesForMove);
		assertEquals(2, p1HexForMov2.getPlayersWhoAreOnMe().size());
		
		mPhase.playerIsDoneMakingMoves(p2.getPlayerId());
		mPhase.playerIsDoneMakingMoves(p3.getPlayerId());
		mPhase.playerIsDoneMakingMoves(p4.getPlayerId());
		
		/**
		 * Combat
		 */
		
		CombatPhase cp = (CombatPhase)gs.getCurrentPhase();
		CombatBattle battle = cp.getCombatBattles().get(0);
		
		int attackerHitCount = battle.getAttackerHitCount();
		int defenderHitCount = battle.getDefenderHitCount();
		
		assertEquals(1, attackerHitCount);
		assertEquals(2, defenderHitCount);
		
		battle.defenderDidRoll();
		battle.attackerDidRoll();
		
		
		// Send chat message
		HttpResponseMessage chatMsg = game.sendChatMessage(u1, "Hi Chat!", null);
		System.out.println("Sent chat message: [" + chatMsg.toJson() + "]");
		
		
		for(SentMessage msg : gs.getSentMessages()){
			System.out.println(msg.getJson());
		}
	}
}
