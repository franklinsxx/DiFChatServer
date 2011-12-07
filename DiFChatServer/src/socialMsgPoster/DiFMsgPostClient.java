package socialMsgPoster;

/**
 *
 * @author franklinsxx
 */

import java.net.*;
import java.io.*;

public class DiFMsgPostClient {

	ObjectInputStream Sinput;	// to read the socker
	ObjectOutputStream Soutput;	// towrite on the socket
	Socket socket;

	// Constructor connection receiving a socket number
	public DiFMsgPostClient(int port) {
		// we use "localhost" as host name, the server is on the same machine
		// but you can put the "real" server name or IP address like franklinsong.com: 1600
		try {
			socket = new Socket("localhost", port);
		}
		catch(Exception e) {
			System.out.println("Error connectiong to server:" + e);
			return;
		}
		System.out.println("Connection accepted " +
				socket.getInetAddress() + ":" +
				socket.getPort());

	}

        public void DiFMsgSender(String Msg){
            		/* Creating both Data Stream */
		try
		{
			Sinput  = new ObjectInputStream(socket.getInputStream());
			Soutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException e) {
			System.out.println("Exception creating new Input/output Streams: " + e);
			return;
		}
		// connection setup
		//String test = "GroupMsgPosterServerTest";
		// send the string to the server
		System.out.println("Client sending \"" + Msg + "\" to serveur");
		try {
			Soutput.writeObject(Msg);
			Soutput.flush();
		}
		catch(IOException e) {
			System.out.println("Error writting to the socket: " + e);
			return;
		}
		// read back the answer from the server
		String response;
		try {
			response = (String) Sinput.readObject();
			System.out.println("Read back from server: " + response);
		}
		catch(Exception e) {
			System.out.println("Problem reading back from server: " + e);
		}

		try{
			Sinput.close();
			Soutput.close();
		}
		catch(Exception e) {}
        }

        public void Close() throws IOException {
            this.socket.close();
        }

        /*
	public static void main(String[] arg) {
		new  DiFMsgPosterClient(1600);
	}*/
}
