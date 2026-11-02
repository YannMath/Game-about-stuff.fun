package game.Objects;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class DisplayTile extends Tile {
    private LuaValue interactionFunc;
    private String text;
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    
    public DisplayTile(String txt) {
        setPreferredSize(new Dimension(160, 160));
        setMinimumSize(new Dimension(100, 100));
        setBorder(new LineBorder(Color.GRAY));
        TOP_LAYER.setOpaque(false);
        TILE_LAYER.add(label);
        add(TILE_LAYER, JLayeredPane.DEFAULT_LAYER);
        add(TOP_LAYER, JLayeredPane.PALETTE_LAYER);
        addInteractionListener(TILE_LAYER);
        addInteractionListener(TOP_LAYER);
        addInteractionListener(label);
        setText(txt);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        Insets insets = TILE_LAYER.getInsets();
        label.setBounds(
            insets.left,
            insets.top,
            TILE_LAYER.getWidth() - insets.left - insets.right,
            TILE_LAYER.getHeight() - insets.top - insets.bottom
        );
    }

    @Override
    public void interact(Player p) {
        if (interactionFunc != null && !interactionFunc.isnil()) {
            LuaValue result = interactionFunc.call(CoerceJavaToLua.coerce(p));
            if (result.isstring()) {
                setText(result.tojstring());
            }
        }
    }

    public void setInteraction(LuaValue interactionFunc) {
        this.interactionFunc = interactionFunc;
    }

    public void setText(String text) {
        this.text = text;
        label.setText(text);
    }

    public String getText() {return text;}
}
