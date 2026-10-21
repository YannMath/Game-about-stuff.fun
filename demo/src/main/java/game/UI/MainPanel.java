package game.UI;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    GamePanel gamePanel = new GamePanel();
    MenuPanel menuPanel = new MenuPanel();
    CardLayout cards = new CardLayout();

    public MainPanel() {
        setLayout(cards);

        add(gamePanel, "game");
        add(menuPanel, "menu");
        cards.show(this, "game");
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}