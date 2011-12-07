package chatAppServer;

import java.net.Socket;

public class ChatAppClientObject {

    Socket ClientSocket;
    String ClientUserName, ClientGroupName;

    ChatAppClientObject(Socket socket, String UserName, String GroupName){
        ClientSocket = socket;
        ClientUserName = UserName;
        ClientGroupName = GroupName;
    }

    public void setSocket(Socket socket){
        ClientSocket = socket;
    }

    public void setUserName(String UserName){
        ClientUserName = UserName;
    }

    public void setGroupName(String GroupName){
        ClientGroupName = GroupName;
    }

    public String getUserName() {
        return ClientUserName;
    }

    public String getGroupName() {
        return ClientGroupName;
    }

    public Socket getSocket() {
        return ClientSocket;
    }

}
