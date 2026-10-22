package game.Logic;

import game.Main;
import game.Objects.Player;

public class Turn {
    public static void doTurn(Player p) {
        int roll = Main.rollDice();
        p.move(roll);
        p.buy();
    }
}