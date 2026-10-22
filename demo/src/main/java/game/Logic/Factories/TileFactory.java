package game.Logic.Factories;

import game.Objects.BoardTile;

public class TileFactory {
    public BoardTile create(String name, int price, String imageFileName, int pos) {
        return new BoardTile(name, price, imageFileName, pos);
    }
}
