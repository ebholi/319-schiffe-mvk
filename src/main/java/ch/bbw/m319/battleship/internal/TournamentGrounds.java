package ch.bbw.m319.battleship.internal;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import ch.bbw.m319.battleship.api.BattleshipArena.GameResult;
import ch.bbw.m319.battleship.api.BattleshipField;
import ch.bbw.m319.battleship.api.BattleshipPlayer;
import ch.bbw.m319.battleship.api.ShipPosition;

/**
 * Do NOT look at this implementation. Use {@link ch.bbw.m319.battleship.api.BattleshipArena} instead.
 */
public record TournamentGrounds(BattleshipPlayer player1, BattleshipPlayer player2) {

	public static final int MAX_ROUNDS_PER_GAME = 100;

	private static final Duration MAX_TURN_DURATION = Duration.ofMillis(100);

	/**
	 * Tournament mode, with both players going first once.
	 */
	public GameResult playTurnamentMode() throws GameRulesViolatedException {
		var game1 = playInternal(true, MAX_TURN_DURATION);
		var game2 = playInternal(false, MAX_TURN_DURATION);
		return game1 == game2 ? game1 : GameResult.DRAW;
	}

	public GameResult playDebugMode() {
		return playInternal(true, null);
	}

	private GameResult playInternal(boolean startWithPlayer1, Duration timeout) {
		var p1 = new PlayerState(player1, timeout);
		var p2 = new PlayerState(player2, timeout);
		try {
			return playInternal(startWithPlayer1, p1, p2);
		} finally {
			p1.executor.shutdownNow();
			p2.executor.shutdownNow();
		}
	}

	private GameResult playInternal(boolean startWithPlayer1, PlayerState p1, PlayerState p2) {
		var active = startWithPlayer1 ? p1 : p2;
		var opponent = startWithPlayer1 ? p2 : p1;
		for (int i = 0; i < MAX_ROUNDS_PER_GAME; i++) {
			var target = active.takeAim();
			active.lastWasHit = opponent.ship.contains(target);
			active.outcomeOfYourTurn(target, active.lastWasHit);
			opponent.outcomeOfOpponentsTurn(target, active.lastWasHit);
			if (active.lastWasHit) {
				if (active.lastHit != null && active.lastHit != target) { // we have a winner!
					active.gameFinished(opponent.ship, true);
					opponent.gameFinished(active.ship, false);
					return active == p1 ? GameResult.WIN : GameResult.LOSS;
				}
				active.lastHit = target;
			}
			// swap players
			var tmp = active;
			active = opponent;
			opponent = tmp;
		}
		// that took too long
		return GameResult.DRAW;
	}

	public static class GameRulesViolatedException extends RuntimeException {

		public final transient BattleshipPlayer offender;

		public GameRulesViolatedException(BattleshipPlayer offender, String message, Throwable cause) {
			super(message, cause);
			this.offender = offender;
		}
	}

	private static class PlayerState implements BattleshipPlayer {

		private static final ThreadFactory deamons = x -> {
			var t = new Thread(x);
			t.setDaemon(true);
			return t;
		};

		private final ExecutorService executor = new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(1, true), deamons, new ThreadPoolExecutor.CallerRunsPolicy());

		private final BattleshipPlayer player;

		private final Duration timeout;

		private final ShipPosition ship;

		private BattleshipField lastHit;

		private boolean lastWasHit;

		private PlayerState(BattleshipPlayer player, Duration timeout) {
			this.player = player;
			this.timeout = timeout;
			this.ship = placeYourShip();
		}

		/**
		 * prevent misbehaving players from ruining a tournament.
		 */
		private <T> T timed(Supplier<T> exec, String opsName) {
			if (timeout == null) {
				return exec.get(); // no wrapping requested
			}
			var future = executor.submit(exec::get);
			try {
				return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				future.cancel(true);
				throw new GameRulesViolatedException(player, opsName + " operation took too long (>" + timeout + ")", e);
			} catch (ExecutionException e) {
				throw new GameRulesViolatedException(player, opsName + " operation crashed: " + e.getMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("unexpected interrupt", e);
			}
		}

		@Override
		public ShipPosition placeYourShip() {
			return timed(() -> Objects.requireNonNull(player.placeYourShip()), "placeYourShip");
		}

		@Override
		public BattleshipField takeAim() {
			return timed(() -> Objects.requireNonNull(player.takeAim()), "takeAim");
		}

		@Override
		public void outcomeOfYourTurn(BattleshipField targetedField, boolean isHit) {
			timed(() -> {
				player.outcomeOfYourTurn(targetedField, isHit);
				return null;
			}, "outcomeOfYourTurn");
		}

		@Override
		public void outcomeOfOpponentsTurn(BattleshipField targetedField, boolean isHit) {
			timed(() -> {
				player.outcomeOfOpponentsTurn(targetedField, isHit);
				return null;
			}, "outcomeOfOpponentsTurn");
		}

		@Override
		public void gameFinished(ShipPosition ship, boolean youHaveWon) {
			timed(() -> {
				player.gameFinished(ship, youHaveWon);
				return null;
			}, "gameFinished");
		}
	}
}
