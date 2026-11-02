package game.Objects;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import game.Main;

public class Tile extends JLayeredPane {
    public final JPanel TILE_LAYER = new JPanel(null);
    public final JPanel TOP_LAYER = new JPanel(null);
    private final MouseListener interactionListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            interact(Main.getCurrentPlayer());
        }
    };

    public Tile() {
        addMouseListener(interactionListener);
    }

    protected final void addInteractionListener(Component component) {
        component.addMouseListener(interactionListener);
    }

    public void interact(Player p) {

    }

    @Override
    public void doLayout() {
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;

        TILE_LAYER.setBounds(x, y, width, height);
        TOP_LAYER.setBounds(x, y, width, height);
    }
}
