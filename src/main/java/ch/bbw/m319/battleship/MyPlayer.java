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

    private String getRandomField() {
        double columnNum = Math.floor(Math.random() * 3);
        double row = Math.floor(Math.random() * 3) + 1;
        int rowInt = (int) row;

        String columnLetter;

        if (columnNum == 0) {
            columnLetter = "A";
        } else if (columnNum == 1) {
            columnLetter = "B";
        } else {
            columnLetter = "C";
        }

        String attackField = columnLetter + rowInt;
        System.out.println(attackField);
        return attackField;
    }

    @Override
    public BattleshipField takeAim() {
        return BattleshipField.valueOf(getRandomField());
    }
}
