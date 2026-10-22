package game.Objects;

import java.awt.Insets;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class Tile extends JLayeredPane {
    public final JPanel TILE_LAYER = new JPanel(null);
    public final JPanel TOP_LAYER = new JPanel(null);

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
