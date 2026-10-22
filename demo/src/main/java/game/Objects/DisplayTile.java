package game.Objects;

import org.luaj.vm2.LuaValue;

public class DisplayTile {
    private LuaValue interactionFunc;
    
    public DisplayTile() {

    }

    public void interact(Player p) {
        if (interactionFunc != null && !interactionFunc.isnil()) {
            interactionFunc.call();
        }
    }

    public void setInteraction(LuaValue interactionFunc) {
        this.interactionFunc = interactionFunc;
    }
}
