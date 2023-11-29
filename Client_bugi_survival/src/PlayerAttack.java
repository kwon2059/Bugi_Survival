

import javax.swing.*;
import java.awt.*;

public class PlayerAttack {
	Image image = new ImageIcon("src/images/원.png").getImage();
	public int x, y;
	int width = image.getWidth(null);
	int height = image.getHeight(null);
	int attack = 5;
	int speed = 15;
	int direction;
	int dx = 0, dy = 0;

	public PlayerAttack(int x, int y, int direction) {
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.dx = x - dx;
		this.dy = y - dy;
	}

	public void fire() {
		if (direction == 1 || dy == -speed) {
			dx = 0;
			dy = -speed;
		}
		if (direction == 2 || dy == speed) {
			dx = 0;
			dy = speed;
		}
		if (direction == 3 || dx == -speed) {
			dx = -speed;
			dy = 0;
		}
		if (direction == 4 || dx == speed) {
			dx = +speed;
			dy = 0;
		}
		this.x += dx;
		this.y += dy;
		

	}

}