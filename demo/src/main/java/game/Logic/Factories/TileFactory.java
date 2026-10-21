package game.Logic.Factories;

import org.luaj.vm2.LuaValue;

import game.Objects.Tile;

public class TileFactory {
    public Tile create(String name, int price, String imageFileName, int pos) {
        return new Tile(name, price, imageFileName, pos);
    }

    public Tile parseTileLuaToTile(LuaValue luaTile) {
        String text = luaTile.get("text").tojstring();
        int price = luaTile.get("price").toint();
        String imageLink = luaTile.get("imageLink").tojstring();
        int pos = luaTile.get("pos").toint();

        Tile returnTile = new Tile(text, price, imageLink, pos);
        returnTile.setOnEnter(luaTile.get("onEnter"));

        return returnTile;
    }
}
