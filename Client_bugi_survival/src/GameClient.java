import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class GameClient extends JFrame {
	//더블버퍼링 위함
	private Image bufferImage;
	private Graphics screenGraphic;
	
	private Image background = new ImageIcon("src/images/게임초기화면.png").getImage();
	private Image lobbyScreen = new ImageIcon("src/images/GameScreen.png").getImage();
	private Image gameScreen = new ImageIcon("src/images/GameScreen.png").getImage();

	private ImageIcon startButtonEnteredImage = new ImageIcon("src/images/게임시작눌림.png");
	private ImageIcon startButtonBasicImage = new ImageIcon("src/images/게임시작.png");
	private ImageIcon quitButtonEnteredImage = new ImageIcon("src/images/게임종료눌림.png");
	private ImageIcon quitButtonBasicImage = new ImageIcon("src/images/게임종료.png");
	private ImageIcon pauseButtonImage = new ImageIcon("src/images/일시정지.png");
	
	private JButton startButton = new JButton(startButtonBasicImage);
	private JButton quitButton = new JButton(quitButtonBasicImage);
	private JButton pauseButton = new JButton(pauseButtonImage);

	//화면전환 위해 boolean값
	private boolean isMainScreen, isLobbyScreen, isGameScreen;
	
	private String serverAddress;
	private String uid;
	private int serverPort = 54321;
	
	private JTextPane t_display, u_display;
	private JButton b_connect,b_disconnect,b_exit,b_send, b_select;
	private JTextField t_input, t_userID, t_hostAddr, t_portNUM;
	private DefaultStyledDocument document;
	JPanel chatPanel = createChatPanel();
	JPanel userListPanel = createUserListPanel();
	JPanel controlPanel = createControlPanel();
	JPanel infoPanel = createInfoPanel();
    JPanel inputPanel = createInputPanel();
	
	private Socket socket;
	private ObjectOutputStream out;
	private Thread receiveThread = null;

	public Play play = new Play();

	public GameClient(String serverAddress, int serverPort) {
		setTitle("Shooting Game");
		setSize(1280, 700);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		init();
		
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		setVisible(true);
	}

	private void init() {
		isMainScreen = true;
		isGameScreen = false;
		addKeyListener(new KeyListener());
		
		inputPanel.setBounds(0, Main.SCREEN_HEIGHT - 80, Main.SCREEN_WIDTH - 16, 40);
	    add(inputPanel);

	    infoPanel.setBounds(0, Main.SCREEN_HEIGHT - 115, Main.SCREEN_WIDTH - 16 , 30);
	    add(infoPanel);

	    controlPanel.setBounds(0, Main.SCREEN_HEIGHT - 150, Main.SCREEN_WIDTH - 16, 30);
	    add(controlPanel);
	}

	private void gameStart(){
		isMainScreen = false;
		isLobbyScreen = false;
		isGameScreen = true;
		chatPanel.setVisible(true);
		setFocusable(true);   //게임 키보드 입력
		requestFocus();      //게임 키보드 입력
		
		play.start();
		
	}

	//더블버퍼링 paint
	public void paint(Graphics g) {
		bufferImage = createImage(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		screenGraphic = bufferImage.getGraphics();
		screenDraw(screenGraphic);
		g.drawImage(bufferImage, 0, 0, null);
	}

	public void screenDraw(Graphics g) {
		if (isMainScreen) {
			paintComponents(g);
			g.drawImage(background, 0, 30, 1274, 550,null);
			
		}
		if (isLobbyScreen) {
			paintComponents(g);
			g.drawImage(lobbyScreen, 0, 0, 1070, 650, null);

			infoPanel.setVisible(false);
			controlPanel.setVisible(false);
		}

		if (isGameScreen) {
			paintComponents(g);
			g.drawImage(gameScreen, 0, 0, 1070, 650, null);
			
			play.playerDraw(g);
			
			startButton.setVisible(true);
	        quitButton.setVisible(true);
	        pauseButton.setVisible(true);

		}
		this.repaint();
	}
	
	
	
	
	private void intoLobby() {
		isMainScreen = false;
	    isLobbyScreen = true;
	    isGameScreen = false;
	    
	    userListPanel.setBounds(1063, 150 , 200 , 100);
	    chatPanel.setBounds(1063, 250 , 200 , 369);
	    
	    add(userListPanel);
	    add(chatPanel);

		setGameScreenButton();
	}
	
	private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel chatList = new JLabel("채팅리스트");
        document = new DefaultStyledDocument();
        t_display = new JTextPane(document);
        t_display.setEditable(false);
        
        panel.add(chatList, BorderLayout.NORTH);
        panel.add(new JScrollPane(t_display), BorderLayout.CENTER);
        
        return panel;
	}
	
	private JPanel createUserListPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JLabel userList = new JLabel("접속자 명단");
        
        u_display = new JTextPane();
        u_display.setEditable(false);
        /////////////////////////////////////////////////////////
        //접속자 가져오는 메소드 써야함 (서버에서 현재 참가자수 와 함께 id 보내야될듯)
        panel.add(userList, BorderLayout.NORTH);
        panel.add(new JScrollPane(u_display), BorderLayout.CENTER);
        
        return panel;
		
	}

	private JPanel createInputPanel() {
	      JPanel panel = new JPanel(new BorderLayout());
	      
	  t_input = new JTextField(30);
	  b_send = new JButton("보내기");
	  
	  panel.add(t_input,BorderLayout.CENTER);
	  
	  b_send.addActionListener(new ActionListener(){
	      @Override
	      public void actionPerformed(ActionEvent e) {
	         sendMessage();
	      }
	  });
	  
	  t_input.addActionListener(new ActionListener(){
	      @Override
	      public void actionPerformed(ActionEvent e) {
	         sendMessage();
	      }
	  });
	  b_select = new JButton("선택하기");
	  b_select.addActionListener(new ActionListener() {
	     JFileChooser chooser = new JFileChooser();
	     
	     @Override
	     public void actionPerformed(ActionEvent e) {
	        FileNameExtensionFilter filter = new FileNameExtensionFilter(
	              "JPG & GIF & PNG Images",
	              "jpg", "gif", "png");
	        
	        chooser.setFileFilter(filter);
	        
	        int ret = chooser.showOpenDialog(GameClient.this);
	        if(ret != JFileChooser.APPROVE_OPTION) {
	           JOptionPane.showMessageDialog(GameClient.this, "파일을 선택하지 않음");
	           return;
	        }
	        t_input.setText(chooser.getSelectedFile().getAbsolutePath());
	        sendImage();
	     }
	  });
	  
	  JPanel p_button = new JPanel(new GridLayout(1,0));
	  p_button.add(b_select);
	  p_button.add(b_send);
	  panel.add(p_button, BorderLayout.EAST);
	  
	  t_input.setEnabled(false);
	  b_send.setEnabled(false);
	  b_select.setEnabled(false);
	  
	  return panel;
	}

	private JPanel createControlPanel() {
	     JPanel panel = new JPanel(new GridLayout(0,3));
	  b_connect = new JButton("접속하기");
	  b_disconnect = new JButton("접속 끊기");
	  b_exit = new JButton("종료하기");
	  
	  panel.add(b_connect);
	  panel.add(b_disconnect);
	  panel.add(b_exit);
	  
	  b_disconnect.setEnabled(false);
	  
	  
	  b_connect.addActionListener(new ActionListener(){
	  @Override
	  public void actionPerformed(ActionEvent e) {
		GameClient.this.serverAddress = t_hostAddr.getText();
	    GameClient.this.serverPort = Integer.parseInt(t_portNUM.getText());
	     
	     try {
	         connectToServer();
	         sendUserID();
	         intoLobby();
	      }
	      catch (UnknownHostException e1){
	         printDisplay("서버 주소와 포트번호를 확인하세요 :" + e1.getMessage()+ "\n");
	         return;
	      }
	      catch(IOException e1) {
	         printDisplay("서버와의 연결오류 :" + e1.getMessage()+ "\n");
	         return;
	      }

	   b_select.setEnabled(true);
	   b_connect.setEnabled(false);
	    b_disconnect.setEnabled(true);
	    t_input.setEnabled(true);
	    b_send.setEnabled(true);
	    b_exit.setEnabled(true);
	  }});
	  
	  b_disconnect.addActionListener(new ActionListener() {
	  @Override
	  public void actionPerformed(ActionEvent e) {
	     disconnect();
	     
	     b_connect.setEnabled(true);
	     b_disconnect.setEnabled(false);
	     t_input.setEnabled(false);
	     b_send.setEnabled(false);
	     b_exit.setEnabled(true);
	  }
	  });
	  
	  
	  b_exit.addActionListener(new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	         System.exit(0);
	      }
	  }); 
	  return panel;
	}

	private JPanel createInfoPanel() {
	   JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	   JLabel idLabel = new JLabel("아이디 : ");
	   JLabel idServer = new JLabel("서버주소 : ");
	   JLabel idPort = new JLabel("포트번호 : ");
	   t_userID = new JTextField(7);
	   t_hostAddr = new JTextField(12);
	   t_portNUM = new JTextField(5);
	   
	   t_userID.setText("guset" + getLocalAddr().split("\\.")[3]);
	   t_hostAddr.setText("localhost"); // 서버 주소 기본값 설정
	   t_portNUM.setText(String.valueOf(this.serverPort));

	   panel.add(idLabel);
	   panel.add(t_userID);
	   panel.add(idServer);
	   panel.add(t_hostAddr);
	   panel.add(idPort);
	   panel.add(t_portNUM);

	   return panel;
	}
	
	private void printDisplay(String msg) {
		   int len = t_display.getDocument().getLength();
		   
		   try {
		      document.insertString(len, msg + "\n", null);
		   } catch(BadLocationException e) {
		      e.printStackTrace();
		   }
		   t_display.setCaretPosition(len);
		}
		private void printDisplay(ImageIcon icon) {
		   t_display.setCaretPosition(t_display.getDocument().getLength());
		   
		   if(icon.getIconWidth() > 400) {
		      Image img = icon.getImage();
		      Image changeImg = img.getScaledInstance(400, -1, Image.SCALE_SMOOTH);
		      icon = new ImageIcon(changeImg);
		   }
		   t_display.insertIcon(icon);
		   printDisplay("");
		   t_input.setText("");
		}
	
	private String getLocalAddr() {
	      InetAddress local = null;
	      String addr = "";
	      try {
	         local = InetAddress.getLocalHost();
	         addr = local.getHostAddress();
	         System.out.println(addr);
	      }
	      catch (java.net.UnknownHostException e) {
	         e.printStackTrace();
	      }
	      return addr;
	   }
	
	private void connectToServer() throws UnknownHostException, IOException{
	     socket = new Socket();
	     SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
	     socket.connect(sa,3000);
	      out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

	     receiveThread = new Thread(new Runnable() {
	        
	        private ObjectInputStream in;
	        
	        private void receiveMessage() {
	           try {
	              ChatMsg inMsg = (ChatMsg) in.readObject();
	              if(inMsg == null) {
	                 disconnect();
	                 printDisplay("서버 연결 끊김");
	                 return ;
	              }
	              
	              switch(inMsg.mode) {
	              case ChatMsg.MODE_TX_STRING :
	                 printDisplay(inMsg.userID + ":" + inMsg.message);
	                 break;
	              case ChatMsg.MODE_TX_IMAGE :
	                 printDisplay(inMsg.userID + ": " + inMsg.message);
	                 printDisplay(inMsg.image);
	                 break;
	                 }
	              
	           } catch(IOException e) {
	                 printDisplay("연결을 종료했습니다.");
	           
	              } catch(ClassNotFoundException e) {
	                 printDisplay("잘못된 객체가 전달 되었습니다.");
	              }
	              
	           }
	      @Override
	      public void run() {
	             try {
	                in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
	             } catch (IOException e) {
	                printDisplay("입력 스트림이 열리지 않음");
	             }
	             while(receiveThread == Thread.currentThread()) {
	                receiveMessage();
	             }
	      }   
	     });
	     receiveThread.start();
	  }
	  

	private void disconnect() {
	   send(new ChatMsg(uid, ChatMsg.MODE_LOGOUT));
	   
	  try {
	     receiveThread = null;
	        socket.close();
	  } 
	  catch (IOException e) {
	     System.err.println("클라이언트 닫기 오류>" + e.getMessage());
	     System.exit(-1);
	  }
	}

	private void send(ChatMsg msg) {
	   try {
	      out.writeObject(msg);
	      out.flush();
	   } catch(IOException e) {
	      System.err.println("클라이언트 일반 전송 오류>" + e.getMessage());
	   }
	}

	private void sendMessage() {
	   String message = t_input.getText();
	   if(message.isEmpty()) return;
	   
	   send(new ChatMsg(uid, ChatMsg.MODE_TX_STRING, message));
	   
	   t_input.setText("");
	}

	private void sendUserID() {
	    uid = t_userID.getText();
	    send(new ChatMsg(uid, ChatMsg.MODE_LOGIN));
	}

	private void sendImage() {
	   String filename = t_input.getText().strip();
	   if(filename.isEmpty()) return;
	   
	   File file = new File(filename);
	   if(!file.exists()) {
	      printDisplay(">> 파일이 존재하지 않습니다: " + filename);
	      return;
	   }
	   ImageIcon icon = new ImageIcon(filename);
	   send(new ChatMsg(uid, ChatMsg.MODE_TX_IMAGE, file.getName(), icon));
	   
	   t_input.setText("");
	}
	
	private void setGameScreenButton(){
		setStartButton();
		setQuitButton();
	}
	
	private void setStartButton() {
		startButton.setBounds(1063, 0, 200, 75);
		startButton.setBorderPainted(false);
		startButton.setContentAreaFilled(false);
		startButton.setFocusPainted(false);
		startButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				startButton.setIcon(startButtonEnteredImage);
				startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				startButton.setIcon(startButtonBasicImage);
				startButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// 게임시작이벤트		
				gameStart();
			}
		});
		add(startButton);
	}
	
	private void setQuitButton() {
		quitButton.setBounds(1063, 75, 200, 75);
		quitButton.setBorderPainted(false);
		quitButton.setContentAreaFilled(false);
		quitButton.setFocusPainted(false);
		quitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				quitButton.setIcon(quitButtonEnteredImage);
				quitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				quitButton.setIcon(quitButtonBasicImage);
				quitButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// 게임시작이벤트
				System.exit(0);
				// TODO : exit(0) 말고 방리스트, 게임채널채팅방 있는 곳으로 변경해야함
			}
		});
		add(quitButton);
	}
	
	class KeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				play.setShooting(true);
				play.setUp(true);
				play.setDown(false);
				play.setLeft(false);
				play.setRight(false);
				break;
			case KeyEvent.VK_DOWN:
				play.setShooting(true);
				play.setUp(false);
				play.setDown(true);
				play.setLeft(false);
				play.setRight(false);
				break;
			case KeyEvent.VK_LEFT:
				play.setShooting(true);
				play.setUp(false);
				play.setDown(false);
				play.setLeft(true);
				play.setRight(false);
				break;
			case KeyEvent.VK_RIGHT:
				play.setShooting(true);
				play.setUp(false);
				play.setDown(false);
				play.setLeft(false);
				play.setRight(true);
				break;

			case KeyEvent.VK_P:
				play.ContinuePlay();
				break;
			case KeyEvent.VK_R:
				play.reset();
				play.isOver();
				break;
			case KeyEvent.VK_ESCAPE:
				play.pause();
				break;
			}
		}
		public void keyReleased(KeyEvent e) {
	        // 키를 떼었을 때 플레이어의 움직임을 초기화하는 코드 추가
	        switch (e.getKeyCode()) {
	            case KeyEvent.VK_UP:
	                play.setUp(false);
	                break;
	            case KeyEvent.VK_DOWN:
	                play.setDown(false);
	                break;
	            case KeyEvent.VK_LEFT:
	                play.setLeft(false);
	                break;
	            case KeyEvent.VK_RIGHT:
	                play.setRight(false);
	                break;
	        }
	    }
	}
	
}
