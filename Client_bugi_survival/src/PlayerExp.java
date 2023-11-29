

import javax.swing.*;
import java.awt.*;

public class PlayerExp {
	Image image = new ImageIcon("src/images/경험치.png").getImage();
	int x, y;
	int width = image.getWidth(null);
	int height = image.getHeight(null);

	public PlayerExp(int x, int y) {
		this.x = x;
		this.y = y;
	}
}