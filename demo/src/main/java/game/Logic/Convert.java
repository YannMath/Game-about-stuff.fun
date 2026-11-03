package game.Logic;

import java.awt.Color;
import java.util.Map;
import org.luaj.vm2.LuaValue;
import game.Objects.BoardTile;
import game.Objects.DisplayTile;
import game.Objects.Player;

public class Convert {
    private static final Map<Color, String> COLOR_TO_IMAGE = Map.of(
        Color.CYAN, "default_player_cyan.png",
        Color.MAGENTA, "default_player_magenta.png",
        Color.ORANGE, "default_player_orange.png",
        Color.GREEN, "default_player_green.png"
    );

    public static BoardTile parseBoardTileLuaToJava(LuaValue luaTile) {
        String text = luaTile.get("text").tojstring();
        int price = luaTile.get("price").toint();
        String imageLink = luaTile.get("imageLink").tojstring();
        int pos = luaTile.get("pos").toint();
        boolean buyable = luaTile.get("buyable").toboolean();

        if (luaTile.get("buyable").isnil()) buyable = true;
        BoardTile returnTile = new BoardTile(text, price, imageLink, pos, buyable);
        returnTile.setOnEnter(luaTile.get("onEnter"));
        returnTile.setOnPass(luaTile.get("onPass"));

        return returnTile;
    }

    public static DisplayTile parseDisplayTileLuaToJava(LuaValue luaTile) {
        String text = luaTile.get("buttonText").tojstring();
        
        DisplayTile returnTile = new DisplayTile(text);
        returnTile.setInteraction(luaTile.get("interaction"));

        return returnTile;
    }

    public static Player parsePlayerLuaToJava(LuaValue luaPlayer) {
        if (!luaPlayer.istable()) {
            throw new IllegalArgumentException("[ERROR] Player configuration must be a Lua table");
        }

        LuaValue nameValue = luaPlayer.get("name");
        if (nameValue.isnil() || !nameValue.isstring()) {
            throw new IllegalArgumentException("[ERROR] Player configuration must include a string 'name'");
        }

        LuaValue colorValue = luaPlayer.get("spriteColor");
        if (colorValue.isnil() || !colorValue.isuserdata(Color.class)) {
            throw new IllegalArgumentException(
                "[ERROR] Player configuration 'spriteColor' must be an AWT Color (for example, color.MAGENTA)"
            );
        }
        Color color = Color.class.cast(colorValue.checkuserdata(Color.class));

        LuaValue autoImageValue = luaPlayer.get("autoImageLink");
        String imageFileName;
        if (autoImageValue.toboolean()) {
            imageFileName = COLOR_TO_IMAGE.get(color);
            if (imageFileName == null) {
                throw new IllegalArgumentException(
                    "[ERROR] No default player image is configured for color " + color
                );
            }
        } else {
            LuaValue imageValue = luaPlayer.get("imageLink");
            if (imageValue.isnil() || !imageValue.isstring() || imageValue.tojstring().isBlank()) {
                throw new IllegalArgumentException(
                    "[ERROR] Player configuration must include a non-empty 'imageLink' when autoImageLink is false"
                );
            }
            imageFileName = imageValue.tojstring();
        }
        return new Player(nameValue.tojstring(), imageFileName, color);
    }
}
