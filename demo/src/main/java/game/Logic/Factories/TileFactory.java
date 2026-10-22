package game.Logic.Factories;

import org.luaj.vm2.LuaValue;

import game.Objects.BoardTile;

public class TileFactory {
    public BoardTile create(String name, int price, String imageFileName, int pos) {
        return new BoardTile(name, price, imageFileName, pos);
    }

    public BoardTile parseTileLuaToTile(LuaValue luaTile) {
        String text = luaTile.get("text").tojstring();
        int price = luaTile.get("price").toint();
        String imageLink = luaTile.get("imageLink").tojstring();
        int pos = luaTile.get("pos").toint();

        BoardTile returnTile = new BoardTile(text, price, imageLink, pos);
        returnTile.setOnEnter(luaTile.get("onEnter"));

        return returnTile;
    }
}
