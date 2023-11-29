

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class Play extends Thread {
	Font f;
	public int score = 0;
	private boolean ContinuePlay;
	private boolean Pause;
	private boolean isOver;
	public int SHOOT;
	public int gen = 100;
	private int delay = 20;
	private long pretime;
	private long starttime;
	public int stoptime;
	private long starttime2;
	private long stoptime2;
	private int pausetime;
	private int time;
	private int cnt;
	public int direction;
	private int playerHp = 100;
	private int Exp = 10;
	public int ExpX, ExpY;
	public int Attack;
	public int totaltime;
	public double totalscore;
	private Audio backgroundMusic;
	private Audio hitSound;
	private Audio expSound;

	private int playerEXP = 0; // 1level마다 먹어야 하는 경험치 5씩 증가
	public int playerLevel = 1;
	private Image fail = new ImageIcon("src/images/fail.png").getImage();
	private Image player = new ImageIcon("src/images/부기.png").getImage();
	private Image pause = new ImageIcon("src/images/일시정지.jpg").getImage();
	private Image clearScreen = new ImageIcon("src/images/클리어화면.png").getImage();

	public void playerDraw(Graphics g) {
		enemyDraw(g);
		expDraw(g);
		g.setColor(Color.RED);
		Font f = new Font("맑은 고딕", Font.BOLD, 20); // 폰트의 객체를 생성함
		g.setFont(f);
		g.drawString("HP ", 20, 55);  //초기 10,35
		g.fillRect(58, 40, playerHp * 2, 20);
		g.setColor(Color.yellow);
		g.fillRect(58, 60, playerEXP * 2, 20);
		g.setFont(f); // 폰트 지정
		g.drawString("Exp ", 20, 75);
		g.drawImage(player, playerX, playerY, null);
		g.setFont(f);
		
		g.drawString("LEV : " + playerLevel, 20, 115);
		g.drawString("SCORE: " + totalscore, 20, 135);
		totaltime = time - pausetime;
		
		g.setColor(Color.white);
		totalscore = score - (totaltime * 20);
		g.drawString("생존시간: " + totaltime + "초", 20, 175);
		g.setColor(Color.white); 
		if (playerHp <= 0) {
			g.drawImage(fail, 0, 0, null);
		}
		if (isOver == true && playerHp <= 0) {
			g.drawImage(fail, 0, 0, null);
		} else if (isOver == true) {
			g.drawImage(pause, 70, 38, null);
		}
		if(playerLevel >=15) {
			isOver = true;
			g.drawImage(clearScreen, 0, 0, null);
			g.setFont(new Font("궁서", Font.BOLD,40));
			g.drawString("totalscore : " + totalscore, 460,550);
		}

		for (int i = 0; i < playerAttackList.size(); i++) {
			playerAttack = playerAttackList.get(i);
			g.drawImage(playerAttack.image, playerAttack.x, playerAttack.y, null);
		}
	}

	public int playerX, playerY;
	private int playerWidth = player.getWidth(null);
	private int playerHeight = player.getHeight(null);
	private int playerSpeed = 5;

	private boolean up, down, left, right, shooting;

	private Enemy enemy;
	private PlayerExp playerExp;
	private ArrayList<PlayerExp> PlayerExpList = new ArrayList<PlayerExp>();
	private ArrayList<Enemy> enemyList = new ArrayList<Enemy>();
	private ArrayList<PlayerAttack> playerAttackList = new ArrayList<PlayerAttack>();
	private PlayerAttack playerAttack;

	@Override
	public void run() {
		backgroundMusic = new Audio("src/bgm/배경음악.wav", true);
		hitSound = new Audio("src/bgm/hitSound.wav", false);
		expSound = new Audio("src/bgm/expSound.wav", false);
		starttime = System.currentTimeMillis();
		cnt = 0;
		playerX = (Main.SCREEN_HEIGHT - playerHeight) / 2;
		playerY = (Main.SCREEN_WIDTH - playerWidth) / 2;
		reset();
		
		while (true) {
			while (!isOver) {
				pretime = System.currentTimeMillis();
				time = (int) ((pretime - starttime) * (0.001));
				if (System.currentTimeMillis() - pretime < delay) { // 정확한 주기를 위해 현재 시간 - cnt 증가 전 시간 < delay인 경우
					try { // 그 차이만큼 Thread에 sleep줌
						Thread.sleep(delay - System.currentTimeMillis() + pretime);
						keyProcess();
						ExpProcess();
						Character();
						playerAttackProcess();
						enemyAppearProcess();
						enemyMoveProcess();
						getExpProcess();
						cnt++;
					} catch (InterruptedException e) { // Thread.sleep의 경우 다음과 같이 예외처리를 해주어야 함
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();

			}
		}
	}

	public void reset() {
		cnt = 0;
		playerHp = 100;
		playerLevel = 1;
		playerEXP = 0;
		SHOOT = 30;
		gen = 70;
		Exp = 50;
		Attack = 5;
		playerSpeed = 5;
		score = 0;
		playerX = (Main.SCREEN_WIDTH - playerWidth) / 2;
		playerY = (Main.SCREEN_HEIGHT - playerHeight) / 2;
		totaltime = 0;
		totalscore = 0;
		PlayerExpList.clear();
		playerAttackList.clear();
		enemyList.clear();
		backgroundMusic.start();
		pretime = 0;
		starttime = System.currentTimeMillis();
		isOver = false;
	}

	public void pause() {
		isOver = true;
		stoptime2 = System.currentTimeMillis();
	}

	public void ContinuePlay() {
		isOver = false;
		starttime2 = System.currentTimeMillis();
		pausetime = (int) ((starttime2 - stoptime2) * 0.001);
		stoptime += pausetime;
	}

	private void playerAttackProcess() {
		for (int i = 0; i < playerAttackList.size(); i++) {
			playerAttack = playerAttackList.get(i);
			playerAttack.fire();
			for (int j = 0; j < enemyList.size(); j++) {
				enemy = enemyList.get(j);
				if (enemy.x - enemy.width1 / 2 - 2 <= playerAttack.x && playerAttack.x <= enemy.x + enemy.width1 / 2 + 2
						&& enemy.y - enemy.height1 / 2 - 2 <= playerAttack.y
						&& playerAttack.y <= enemy.y + enemy.height1 / 2 + 2) {
					enemy.hp -= Attack;
					playerAttackList.remove(playerAttack);
					hitSound.start();

				}
				if (enemy.x - enemy.width1 / 2 <= playerX && playerX <= enemy.x + enemy.width1 / 2
						&& enemy.y - enemy.height1 / 2 <= playerY && playerY <= enemy.y + enemy.height1 / 2) {
					playerHp -= enemy.EnemyAttack;
					enemyList.remove(enemy);

				}
				if (enemy.hp <= 0) {
					score += 100;
					ExpX = this.enemy.x;
					ExpY = this.enemy.y;
					enemyList.remove(enemy);
					playerExp = new PlayerExp(ExpX, ExpY);
					PlayerExpList.add(playerExp);
					ExpProcess();
				}

				if (playerHp <= 0) {
					isOver = true;
				}
				if (playerHp >= 100) {
					playerHp = 100;
				}
			}
		}
	}

	private void Character() {
		if (playerEXP >= 100) {
			playerLevel += 1;// 경험치가 100 넘어가면 플레이어 레벨 1이 오름
			playerEXP = 0;
			playerHp += 20; // 레벨이 오를 때마다 피가 20 참
		}
		if (1 <= playerLevel && playerLevel < 3) {
			SHOOT = 30;
			gen = 70;
			Exp = 15;
			Attack = 5;
			playerSpeed = 5;
		}
		if (3 <= playerLevel && playerLevel < 5) {
			SHOOT = 25;
			gen = 60;
			Exp = 13;
			Attack = 5;
			playerSpeed = 6;
		}
		if (5 <= playerLevel && playerLevel < 7) {
			SHOOT = 20;
			gen = 50;
			Exp = 11;
			Attack = 5;
			playerSpeed = 7;
		}
		if (7 <= playerLevel && playerLevel < 9) {
			SHOOT = 15;
			gen = 40;
			Exp = 10;
			Attack = 8;
			playerSpeed = 8;
		}
		if (9 <= playerLevel && playerLevel < 11) {
			SHOOT = 10;
			gen = 20;
			Exp = 9;
			Attack = 5;
			playerSpeed = 9;
		}
		if (11 <= playerLevel && playerLevel < 13) {
			SHOOT = 13;
			gen = 30;
			Exp = 8;
			Attack = 5;
			playerSpeed = 10;
		}
		if (13 <= playerLevel && playerLevel < 15) {
			SHOOT = 11;
			gen = 15;
			Exp = 6;
			Attack = 5;
			playerSpeed = 11;
		}

	}

	private void getExpProcess() {
		for (int i = 0; i < PlayerExpList.size(); i++) {
			playerExp = PlayerExpList.get(i);
			if (playerExp.x - playerExp.width / 2 < playerX && playerX < playerExp.x + playerExp.width / 2
					&& playerExp.y - playerExp.width / 2 < playerY && playerY < playerExp.y + playerExp.height / 2) {
				PlayerExpList.remove(playerExp);
				playerEXP += Exp;
				score += 30;
				expSound.start();
			}
		}
	}

	private void keyProcess() {
		if (up && playerY - playerSpeed > 28) {
			playerY -= playerSpeed;
			direction = 1;
		}
		if (down && playerY + playerHeight + playerSpeed < 650 ) { //패널 아래로 못움직이게 함
			playerY += playerSpeed;
			direction = 2;
		}
		if (left && playerX - playerSpeed > 0) {
			playerX -= playerSpeed;
			direction = 3;
		}
		if (right && playerX + playerWidth + playerSpeed < 1070) { //패널 오른쪽으로 못움직이게 함
			playerX += playerSpeed;
			direction = 4;
		}
		if (shooting && cnt % SHOOT == 0) {
			playerAttack = new PlayerAttack(playerX, playerY + 2, direction);
			playerAttackList.add(playerAttack);
		}
		
		// playerAttack이 화면 밖으로 벗어나면 삭제 
	    for (int i = 0; i < playerAttackList.size(); i++) {
	        playerAttack = playerAttackList.get(i);
	        if (playerAttack.x < 0 || playerAttack.x > 1040 || playerAttack.y < 0 || playerAttack.y > 620) {
	            playerAttackList.remove(playerAttack);
	            i--;  // 리스트에서 요소를 삭제하면서 인덱스를 다시 검사하도록 i를 감소
	        }
	    }
		/* 치트키
		 if (playerX >= 0 && playerX <= 1080 && playerY >= 0 && playerY <= 660) {
            playerAttack = new PlayerAttack(playerX, playerY + 2, direction);
            playerAttackList.add(playerAttack);
        }
		 */
	}

	private void enemyAppearProcess() {
		if (cnt % gen == 0) {
			int rand = (int) (Math.random() * 4);
			if (rand == 0) {
				enemy = new Enemy((int) (Math.random() * Main.SCREEN_WIDTH / 2) * 2, 10);
				enemyList.add(enemy);
			}
			if (rand == 1) {
				enemy = new Enemy((int) (Math.random() * Main.SCREEN_WIDTH / 2) * 2, Main.SCREEN_HEIGHT - 10);
				enemyList.add(enemy);
			}
			if (rand == 2) {
				enemy = new Enemy(10, (int) (Math.random() * Main.SCREEN_HEIGHT / 2) * 2 - 10);
				enemyList.add(enemy);
			}
			if (rand == 3) {
				enemy = new Enemy(Main.SCREEN_WIDTH - 10, (int) (Math.random() * Main.SCREEN_HEIGHT / 2) * 2 - 10);
				enemyList.add(enemy);
			}
		}
	}
	
	public void expDraw(Graphics g) {
		for (int i = 0; i < PlayerExpList.size(); i++) {
			playerExp = PlayerExpList.get(i);
			g.drawImage(playerExp.image, playerExp.x, playerExp.y, null);
		}
	}

	private void ExpProcess() {
		for (int i = 0; i < PlayerExpList.size(); i++) {
			playerExp = PlayerExpList.get(i);
		}
	}

	private void enemyMoveProcess() {
		for (int i = 0; i < enemyList.size(); i++) {
			enemy = enemyList.get(i);
			enemy.move(playerX, playerY);
		}
	}

	public void enemyDraw(Graphics g) {
		for (int i = 0; i < enemyList.size(); i++) {
			enemy = enemyList.get(i);
			g.drawImage(enemy.image1, enemy.x, enemy.y, null);
		}
	}

	public boolean continuePlay() {
		return ContinuePlay;
	}

	public boolean Pause() {
		return Pause;
	}

	public boolean isOver() {
		return isOver;
	}

	public void setUp(boolean up) {
		this.up = up;
	}

	public void setDown(boolean down) {
		this.down = down;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public void setShooting(boolean shooting) {
		this.shooting = shooting;
	}
}
