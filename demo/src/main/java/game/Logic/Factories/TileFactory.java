package game.Logic.Factories;

import game.Objects.BoardTile;

public class TileFactory {
    public BoardTile create(String name, int price, int rent, String imageFileName, int pos, int id) {
        return new BoardTile(name, price, rent, imageFileName, pos, id);
    }
}