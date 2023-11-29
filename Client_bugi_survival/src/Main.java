

public class Main {
	public static final int SCREEN_WIDTH = 1280;
	public static final int SCREEN_HEIGHT = 700;

	public static void main(String[] args) {
		String serverAddress = "localhost";
		int serverPort = 54321;
		new GameClient(serverAddress, serverPort);
	}
}