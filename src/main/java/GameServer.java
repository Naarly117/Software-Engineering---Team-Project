import java.io.*;
import java.net.*;
import java.util.*;

/**
 * 
 * @author jed_lechner
 * Main server to listen for connects. Handles all incoming game messages for 
 * Quoridor. 
 */

public class GameServer extends Server {

    private int playerNum; // player Number given to this server by the client
    private RandomAI AI; // The AI to get a move from.
    private String name; // The name of the player. 
    
	
    /**
     * 
     * @param port: Port to connect to.
     * @param name: Name of the server.
     * Constructor
     */
    public GameServer(int port, String name) {
        super(port);
	this.name = name;
        System.out.println("In the constructor");
		
    }	

    /**
     * 
     * @param socket: The socket the client connects to.
     * @throws IOException 
     * Handles incoming IO. Listens for a message then passes to handle message.
     */
    @Override 
    public void handle(Socket socket) throws IOException {
	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

	String msg = "";
        System.out.println("In handle");
	while(true) {
            msg = in.readLine();
            System.out.println("Message from client " + msg);
            handleMessage(msg, out, socket);
	}

    }

    /**
     * 
     * @param msg: The message from the client.
     * @param out: The printwriter to send messages back to the client. 
     * @param socket: The socket to close upon a certain message.
     * Handle the specific protocol messages. Denoted by capital words. 
     */
    private void handleMessage(String msg, PrintWriter out, Socket socket) {
	Scanner sc = new Scanner(System.in);
        msg = Parser.stripBrackets(msg); // strip the brackets from the message
	String [] s = msg.split(" "); // splits the string by spaces
	
        if(msg.startsWith("HELLO")) { // handle hello
            System.out.println("Sending IAM " + name + " to client");
            out.println("IAM " + name);
	} else if(msg.startsWith("GAME")) { // get the game message from client
            if(s.length == 4) {
		playerNum = Integer.parseInt(s[1]);
                AI = new RandomAI(2, playerNum); // set the random AI
            } else {
                playerNum = Integer.parseInt(s[1]);
                AI = new RandomAI(4, playerNum);
            }
            return;
        } else if(msg.startsWith("MYOUSHU")) { // get a move
            try{
                Thread.sleep(2000); // here temporarily for move
            } catch(Exception e) {
		e.printStackTrace();
            }
            String move = AI.getRandomMove(); // get a random move from the AI
            System.out.println("Sending TESUJI " + move);
            out.println("TESUJI " + move);
	} else if(msg.startsWith("ATARI")) {
            // only for reading move moves will not handle wall placement
            msg = Parser.parse(msg);
            Scanner temp = new Scanner(msg);
            int player = temp.nextInt();
            String move = temp.next() +" "+ temp.next();
            AI.update(player,move);
            System.out.println("Saw ATARI");
	} else if(msg.startsWith("KIKASHI")) { // game is over guy won
            try {
		out.close();
		socket.close();
		AI = null;
            }catch(IOException e) {
		e.printStackTrace();
            }
	} else if(msg.startsWith("GOTE")) {
            // update AI with kicked player
            System.out.println("Person kicked");
	} else {
            return;
	}
    }

	public static void main(String[] args) {
		int port = 6969;
		String name = "";
		Scanner sc = new Scanner(System.in);
		name = sc.next();
		for(int i = 1; i < args.length; i++) {
			if(args[i-1].equals("--port")){
				port = Integer.parseInt(args[i]);
			}
		}
                
		GameServer s = new GameServer(port, name);
		s.connect();
	}
}
