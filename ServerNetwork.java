import java.util.Queue;
import java.util.LinkedList;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Runnable;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.PrintStream;
import java.io.IOException;

public class ServerNetwork implements Runnable{

    private static ServerNetwork server;
    private ServerSocket socket;
    private volatile Queue<String> toSend = new LinkedList();
    private volatile Queue<String> recieved = new LinkedList();
    private volatile Socket client;
    private volatile boolean running = true;
    private static final int DEFAULT_PORT = 2222;

    //The following finals are all for testing. Please do not
    //change them!
    private static final boolean TEST_MODE = true;
    public static final int TEST_SEND_MSG_QUEUE = 0;
    public static final int TEST_RECIEVE_MSG_QUEUE = 1;

    //The only constructor is private to keep there
    //From being more than just one NetworkServer
    private ServerNetwork(int port){
	try{
	    System.out.println("In the constructor!");
	    socket = new ServerSocket(port);
	    client = new Socket();
	    new Thread(this).start();
	}catch(Exception e){

	}
    }

    /**
     * Returns a satitically created instance of ServerNetwork. This 
     * Prevents there from being multiple servers. If the instance 
     * of ServerNetwork does not exist, then this method will create
     * one. Note: the values can only be set on the first call of 
     * this method.
     *
     * @param  port     the port number that this server is running on. 
     * @param  players  the max number of players. 
     * @return          A static instance of the ServerNetwork class.
     */
    public static ServerNetwork serverFactory(int port){
	if(server == null){
	    server = new ServerNetwork(port);
	}
	return server;
    }

    /**
     * Returns a satitically created instance of ServerNetwork. This 
     * Prevents there from being multiple servers. If the instance 
     * of ServerNetwork does not exist, then this method will create
     * one using the default port.
     *
     * @return          A static instance of the ServerNetwork class.
     */
    public static ServerNetwork serverFactory(){
	return ServerNetwork.serverFactory(DEFAULT_PORT);
    }

    /**
     * Adds a string literal to the message queue.
     * This method provides no error-checking.
     *
     * @param  msg   The String literal that is to be sent to the 
     *               specified client.
     * @return       A boolean value to for whether or not msg was 
     *               added to the queue successfully.
     */
    public boolean sendMessage(String msg){
	return toSend.add(msg);
    }

    /**
     * A non-destructive method to see if there is any data ready 
     * to be read from the input queue
     *
     * @return True if there is data ready to be read, false otherwise.
     */
    public boolean hasMessage(){
	return recieved.peek() != null;
    }

    /**
     * Pulls the top message off of the input stack. If there is 
     * nothing on the input stack, it will return null. The 
     * messages are formatted as follows: "client number, message"
     *
     * @return The top message of the input stack, or null.
     */
    public String getMessage(){
	if(hasMessage()){
	    return recieved.remove();
	}else{
	    return null;
	}
    }

    private boolean hasToSend(){
	return toSend.peek() != null;	
    }

    public void run(){
       	try{
	    client = socket.accept();
	    PrintStream writer = new PrintStream(client.getOutputStream());
	    Scanner reader = new Scanner(client.getInputStream());   
	    while(running){
		if(hasToSend()){
		    writer.println(toSend.remove());
		}
		if(reader.hasNextLine()){
		    recieved.add(reader.nextLine());
		}
	}
	}catch(IOException e){
	    e.printStackTrace(); //TODO: make this meaningful
	}
    }


    /**
     * Tester class for JUnit functionality. Should only ever be 
     * called by JUnit testing. NOTE: if the class is not in 
     * test mode, this method will always return null.
     *
     * @param  testType the type of test to be specified by the int
     * @return specific information back to the tester, depending
     *         on the type of test.
     */
    public String test(int testType){
	String toReturn = null;
	if(TEST_MODE){
	    switch(testType){
	    case TEST_SEND_MSG_QUEUE:
		toReturn = (String)toSend.remove();
	    case TEST_RECIEVE_MSG_QUEUE:
		for(int i = 0 ; i < 1000000000 && toReturn == null ; i++){
		    toReturn = (String)recieved.poll();
		}
	    }
	}
	return toReturn;
    }
}