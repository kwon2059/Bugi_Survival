

import javax.swing.*;
import java.awt.*;

public class Enemy{
	Image image1 = new ImageIcon("src/images/moster.png").getImage();
	int x, y;
	int width1 = image1.getWidth(null);
	int height1 = image1.getHeight(null);
	int hp = 10;
	int EnemyAttack = 10;
	int dx, dy;
	public Enemy(int x, int y) {
		if (x > 1050) { //x축 몬스터나옴
            x = 1050;
        }
		if(y > 595) {
			y = 595; //y축 몬스터나옴
		}
		
		this.x = x;
		this.y = y;
	}
	public void move(int playerX, int playerY) {
		if(playerX > x && playerY > y) {
			dx = 2;
			dy = 2;
		}
		else if(playerX > x && playerY == y) {
			dx = 2;
			dy = 0;
		}
		else if(playerX  == x && playerY > y) {
			dx = 0;
			dy = +2;
		}
		else if(playerX < x && playerY > y) {
			dx = -2;
			dy = 2;
		}
		else if(playerX  < x && playerY == y) {
			dx = -2;
			dy = 0;
		}
		else if(playerX  == x && playerY > y) {
			dx = 0;
			dy = 2;
		}
		else if(playerX > x && playerY < y) {
			dx = 2;
			dy = -2;
		}
		else if(playerX == x && playerY < y) {
			dx = 0;
			dy = -2;
		}
		else if(playerX < x && playerY < y) {
			dx = -2;
			dy = -2;
		}
		else {
			dx = 0;
			dy = 0;
		}
		this.x += dx;
		this.y += dy;
	}
}
