package ch.bbw.m319.battleship;

import ch.bbw.m319.battleship.api.BattleshipArena;
import ch.bbw.m319.battleship.api.BattleshipField;
import ch.bbw.m319.battleship.api.BattleshipPlayer;
import ch.bbw.m319.battleship.api.ShipPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OliverPlayer implements BattleshipPlayer {

    public static void main(String[] args) {
        // let it play against itself
        BattleshipArena.playMultipleAndCount(new OliverPlayer(), new DumbPlayer(), 1000);
    }

    @Override
    public ShipPosition placeYourShip() {
        String shipPos1 = getRandomField();

        // Manual assignment to valid Positions for every Field
        String[] validShipPos2;
        if (shipPos1.equals("A1")) {
            validShipPos2 = new String[]{"A2", "B1"};
        } else if (shipPos1.equals("A2")) {
            validShipPos2 = new String[]{"A1", "A3", "B2"};
        } else if (shipPos1.equals("A3")) {
            validShipPos2 = new String[]{"A2", "B3"};
        } else if (shipPos1.equals("B1")) {
            validShipPos2 = new String[]{"A1", "B2", "C1"};
        } else if (shipPos1.equals("B2")) {
            validShipPos2 = new String[]{"A2", "B1", "B3", "C2"};
        } else if (shipPos1.equals("B3")) {
            validShipPos2 = new String[]{"A3", "B2", "C3"};
        } else if (shipPos1.equals("C1")) {
            validShipPos2 = new String[]{"B1", "C2"};
        } else if (shipPos1.equals("C2")) {
            validShipPos2 = new String[]{"B2", "C1", "C3"};
        } else {
            validShipPos2 = new String[]{"B3", "C2"};
        }

        // Selecting random Ship Position form valid Positions
        String shipPos2 = validShipPos2[(int) Math.floor(Math.random() * validShipPos2.length)];

        return new ShipPosition(BattleshipField.valueOf(shipPos1), BattleshipField.valueOf(shipPos2));
    }

    private String getRandomField() {
        double columnNum = Math.ceil(Math.random() * 3);
        double row = Math.ceil(Math.random() * 3); // Math.ceil means I don't have to add 1 like if I was using Math.floor
        int rowInt = (int) row;

        String columnLetter;

        if (columnNum == 1) {
            columnLetter = "A";
        } else if (columnNum == 2) {
            columnLetter = "B";
        } else {
            columnLetter = "C";
        }

        return columnLetter + rowInt;
    }

    private List<String> getFirstFields() {
        List<String> firstFields = new ArrayList<>();

        firstFields.add("A2");
        firstFields.add("B1");
        firstFields.add("B3");
        firstFields.add("C2");

        Collections.shuffle(firstFields);
        return firstFields;
    }

    private  List<String> getSecondFields() {
        List<String> secondFields = new ArrayList<>();

        secondFields.add("A1");
        secondFields.add("A3");
        secondFields.add("B2");
        secondFields.add("C1");
        secondFields.add("C3");

        Collections.shuffle(secondFields);
        return secondFields;
    }

    List<String> fieldsToAttack = new ArrayList<>();
    int attackIndex = 0;

    @Override
    public BattleshipField takeAim() {
        List<String> firstAttackFields = getFirstFields();
        List<String> secondAttackFields = getSecondFields();
        String attackField;

        if (fieldsToAttack.isEmpty()) {
            for (int i = 0; i < 9; i++) {
                do {
                    attackField = getRandomField();
                } while (fieldsToAttack.contains(attackField));
                fieldsToAttack.add(attackField);
            }
        }

        // Resets attackIndex after
        if (attackIndex >= fieldsToAttack.size()) {
            attackIndex = 0;
        }

        return BattleshipField.valueOf(fieldsToAttack.get(attackIndex++));
    }
}

// Note for future
// Only these Fields have to be attacked to find a ship:
//    B1
// A2    C2
//    B3