package ch.bbw.m319.battleship;

import ch.bbw.m319.battleship.api.BattleshipArena;
import ch.bbw.m319.battleship.api.BattleshipField;
import ch.bbw.m319.battleship.api.BattleshipPlayer;
import ch.bbw.m319.battleship.api.ShipPosition;

public class MyPlayer implements BattleshipPlayer {

	public static void main(String[] args) {
		// let it play against itself
		BattleshipArena.playOnce(new MyPlayer(), new MyPlayer());
	}

	@Override
	public ShipPosition placeYourShip() {
		// TODO: replace this implementation: always top-left is not that good...
		return new ShipPosition(BattleshipField.A1, BattleshipField.A2);
	}

	@Override
	public BattleshipField takeAim() {
		// TODO: replace this implementation: always bottom-right is not that good...
		return BattleshipField.C3;
	}
}
