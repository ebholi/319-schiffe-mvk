package ch.bbw.m319.battleship.api;

import static ch.bbw.m319.battleship.api.BattleshipArena.GameResult.*;

import ch.bbw.m319.battleship.internal.TournamentGrounds;

public final class BattleshipArena {
	private BattleshipArena(){}

	public enum GameResult {
		WIN, DRAW, LOSS
	}

	/**
	 * Einfaches Spiel ohne Rückrunde.
	 * @param startingPlayer der Spieler der zuerst zielen darf
	 * @param secondPlayer der Gegner
	 * @return {@link GameResult#WIN}, falls der Startspieler gewonnen hat.
	 */
	public static GameResult playOnce(BattleshipPlayer startingPlayer, BattleshipPlayer secondPlayer) {
		var result = new TournamentGrounds(startingPlayer, secondPlayer).playDebugMode();
		System.out.println("outcome (from starting-players view): " + result);
		return result;
	}

	/**
	 * Turniermodus mit mehreren Hin- und Rückspielen.
	 * Beide Spieler haben ein Zeitlimit und Crashes werden als LOSS gewertet.
	 * @param player1
	 * @param player2
	 * @param rounds anzal Hin- und Rückrunden
	 * @return {@link GameResult#WIN}, falls {@code player1} öfters gewonnen hat.
	 */
	public static GameResult playMultiple(BattleshipPlayer player1, BattleshipPlayer player2, int rounds) {
		var player1WinCounter = playMultipleAndCount(player1, player2, rounds);
		return player1WinCounter == 0 ? GameResult.DRAW : (player1WinCounter > 0 ? WIN : GameResult.LOSS);
	}

	public static int playMultipleAndCount(BattleshipPlayer player1, BattleshipPlayer player2, int rounds) {
		var tournament = new TournamentGrounds(player1, player2);
		var player1WinCounter = 0;
		var i = 0;
		try {
			for (; i < rounds; i++) {
				player1WinCounter += switch (tournament.playTurnamentMode()) {
					case WIN -> 1;
					case DRAW -> 0;
					case LOSS -> -1;
				};
			}
		} catch (TournamentGrounds.GameRulesViolatedException e) {
			System.err.println("round " + i + ": player " + e.offender.getClass().getSimpleName() + " was misbehaving: " + e.getMessage());
			//e.printStackTrace();
			return e.offender == player1 ? -rounds : rounds;
		}
		System.out.println("outcome (from player 1 pov) after " + rounds + ": it won " + player1WinCounter + " more often");
		return player1WinCounter;
	}
}
