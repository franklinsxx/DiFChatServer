package chatAppServer;

import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedInputStream;

public class ChatAppCommunication implements Runnable, ChatAppSettings{

    Thread thread;
    Socket socket;
    DataInputStream inputStream;
    String RFC;
    ChatAppServer Parent;

    ChatAppCommunication(ChatAppServer chatServer, Socket clientSocket){

        Parent = chatServer;
        socket = clientSocket;
        try{
           inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
           System.out.println("|INFO| Received Data: " + socket.getInputStream());
        }catch(IOException _IOExc) { }
        thread = new Thread(this);
        thread.start();
        System.out.println("|INFO| ChatCommunication thread starts...");
    }

    private void QuitConnection(){
        thread.stop();
        thread = null;
        try {
            socket.close();
        }catch(IOException _IOExc){}
        socket = null;
    }

    public void run(){
        while(thread != null){
			try {
				RFC = inputStream.readLine();
				//RFC checking
				if(RFC.startsWith("HELO"))
				{
					Parent.AddUser(socket,RFC.substring(5));
                                        System.out.println("|INFO| User Added...");
				}

				if(RFC.startsWith("QUIT"))
				{
					Parent.RemoveUser(RFC.substring(5,RFC.indexOf("~")),RFC.substring(RFC.indexOf("~")+1),REMOVE_USER);
					QuitConnection();
				}

				if(RFC.startsWith("KICK"))
				{
					Parent.RemoveUser(RFC.substring(5,RFC.indexOf("~")),RFC.substring(RFC.indexOf("~")+1),KICK_USER);
					QuitConnection();
				}

				if(RFC.startsWith("CHGO"))
				{
					Parent.ChangeGroup(socket,RFC.substring(5,RFC.indexOf("~")),RFC.substring(RFC.indexOf("~")+1));
				}

				if(RFC.startsWith("MESS"))
				{
					Parent.SendGeneralMsg(socket,RFC.substring(RFC.indexOf(":")+1),RFC.substring(RFC.indexOf("~")+1,RFC.indexOf(":")),RFC.substring(5,RFC.indexOf("~")));
				}

				if(RFC.startsWith("PRIV"))
				{
					Parent.SendPrivateMsg(RFC.substring(RFC.indexOf("~")+1),RFC.substring(5,RFC.indexOf("~")));
				}

				if(RFC.startsWith("GOCO"))
				{
					Parent.GetUserCount(socket,RFC.substring(5));
				}
				if(RFC.startsWith("ACCE"))
				{
					Parent.SendUserIP(socket,RFC.substring(5,RFC.indexOf("~")),RFC.substring(RFC.indexOf("~")+1));
				}
				if(RFC.startsWith("REIP"))
				{
					Parent.GetRemoteUserAddress(socket,RFC.substring(5,RFC.indexOf("~")),RFC.substring(RFC.indexOf("~")+1));
				}
				if(RFC.startsWith("AEIP"))
				{
					Parent.SendRemoteUserAddress(socket,RFC.substring(5,RFC.indexOf("~")),RFC.substring(RFC.indexOf("~")+1));
				}
                                else{
                                    //Parent.AddUser(socket,"debugging");
                                    System.out.println("|INFO| RFC Checking failed...");
                                }


			}catch(Exception _Exc) { Parent.RemoveUserWhenException(socket);QuitConnection();}
		}
    }
}
