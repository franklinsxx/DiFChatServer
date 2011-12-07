package chatAppServer;

import socialMsgPoster.DiFMsgPostClient;
import java.awt.Button;
import java.awt.Panel;
import java.awt.Frame;
import java.awt.Color;
import java.awt.Label;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.BorderLayout;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Properties;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.io.InputStream;
import java.util.ArrayList;


public class ChatAppServer extends Frame implements Serializable, ActionListener, Runnable, ChatAppSettings {

    //Global Variable Declarations
    Properties DBProperties;
    Button cmdStart, cmdStop;
    ServerSocket serversocket;
    Socket socket;
    Color BkColor;
    ArrayList userArrayList, msgArrayList;
    Thread thread;
    String groupList;
    int gLoop;
    private ChatAppCommunication ChatAppCommunication;
    private DataOutputStream dataOutputStream;
    ChatAppClientObject clientObject;



    public ChatAppServer() {
         //Intialization
         this.setTitle("DiF App Chat Server");
         this.setResizable(false);
         this.setBackground(Color.WHITE);
         this.setLayout(new BorderLayout());

         /*Interface Design*/
         //Top Panel
         Panel topPanel = new Panel(new BorderLayout());
         BkColor = new Color(59, 89, 152);
         topPanel.setBackground(BkColor);
         Label labelTitle = new Label("DiF_App_Chat_Server v1.0", 1);
         labelTitle.setFont(new Font("Helvitica", Font.BOLD, 20));
         labelTitle.setForeground(Color.WHITE);
         topPanel.add("Center", labelTitle);
         add("North", topPanel);

         //Center Panel
         Panel centerPanel = new Panel(null);
         cmdStart = new Button("START SERVER");
         cmdStart.setBounds(125, 10, 150, 30);
         cmdStart.addActionListener(this);
         centerPanel.add(cmdStart);
         cmdStop = new Button("STOP SERVER");
         cmdStop.setBounds(125, 50, 150, 30);
         cmdStop.setEnabled(false);
         cmdStop.addActionListener(this);
         centerPanel.add(cmdStop);
         add("Center", centerPanel);

         setSize(400, 150);
         show();

         //Exit
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e){
                 ExitServer();
                 dispose();
                 System.exit(0);
             }
         }
         );
    }

    //Exit
    private void ExitServer(){
        if(thread != null){
            thread.stop();
            thread = null;
        }
       try{
           if(serversocket != null){
               serversocket.close();
               serversocket = null;
           }
       }
       catch(IOException _IOExc){
        }
        userArrayList = null;
        msgArrayList = null;
        cmdStop.setEnabled(false);
        cmdStart.setEnabled(true);
       }


    private Properties GetDBProperties(){
        Properties DBProperties = new Properties();

        try
        {
            InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream("chatAppServer.conf");
            DBProperties.load(inputstream);
            inputstream.close();
        }
        catch(IOException _IOExc){
        }
        finally{
            return DBProperties;
        }
    }

    //Action listener
    public void actionPerformed(ActionEvent evt){
        if(evt.getActionCommand().equalsIgnoreCase("Start Server")){
        DBProperties = GetDBProperties();
        // Server socket
        try {
            groupList = "";
            if(DBProperties.getProperty("grouplist")!= null){
                groupList = DBProperties.getProperty("grouplist");
            }
        else{
             groupList = "Common;My FB friends";
        }
            int chatPortNumber = 1436;
            if(DBProperties.getProperty("portnumber")!= null)
                chatPortNumber =Integer.parseInt(DBProperties.getProperty("portnumber"));
            serversocket = new ServerSocket(chatPortNumber);
        }
        catch(IOException _IOExc){
        }

        userArrayList = new ArrayList();
        msgArrayList = new ArrayList();

        thread = new Thread(this);
        thread.start();

        cmdStart.setEnabled(false);
        cmdStop.setEnabled(true);
        System.out.println("|INFO| Server starts...");

        }

        if(evt.getActionCommand().equalsIgnoreCase("Stop Server")){
            ExitServer();
            cmdStop.setEnabled(false);
            cmdStart.setEnabled(true);
        }

    }

    public void run(){

        while(thread != null){
            try {
                System.out.println("|INFO| " + serversocket.getInetAddress() + " " + serversocket.getLocalPort());
                socket = serversocket.accept();
                ChatAppCommunication = new ChatAppCommunication(this, socket);
                System.out.println("|INFO| Thread starts..." + socket.getInetAddress() + " " + socket.getPort());
                thread.sleep(THREAD_SLEEP_TIME);
            }
            catch(InterruptedException _INExc) 	{ ExitServer(); }
            catch(IOException _IOExc) 			{ ExitServer();	}
        }
    }

    //Message sending
    private void SendMsgToClient(Socket clientSocket, String msg){
        try{
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            dataOutputStream.writeBytes(msg + "\r\n");
            System.out.println("|INFO| Sent " + msg + " " + clientSocket.getInetAddress() + " " + clientSocket.getPort());
        }
        catch(IOException _IOExc){
        }
    }

    //Get object from given user name
    //Should be revised if users share same name
    private ChatAppClientObject GetClientObject(String UserName){
        ChatAppClientObject returnClientObject = null;
        ChatAppClientObject TempClientObject = null;
        int userListSize = userArrayList.size();
        for(gLoop = 0; gLoop < userListSize; gLoop++){
            TempClientObject = (ChatAppClientObject) userArrayList.get(gLoop);
            if(TempClientObject.getUserName().equalsIgnoreCase(UserName)){
                returnClientObject = TempClientObject;
                break;
            }
        }
        return returnClientObject;
    }

    private boolean IsUserExists(String UserName){
        if(GetClientObject(UserName) != null)
            return true;
        else
            return false;
    }

    private int GetIndex(String UserName){
        int userListSize = userArrayList.size();
        for(gLoop = 0; gLoop < userListSize; gLoop ++){
            clientObject = (ChatAppClientObject) userArrayList.get(gLoop);
            if(clientObject.getUserName().equalsIgnoreCase(UserName))
                return gLoop;
        }
        return -1;
    }

    protected void AddUser (Socket ClientSocket, String UserName){
        //Check user exists or not
        if(IsUserExists(UserName)){
            SendMsgToClient(ClientSocket, "EXIS");
            return;
        }

        //Send group list
        SendMsgToClient(ClientSocket, "GROUP" + groupList);

        //Synchronize incoming user with current users in same chat group
        int userListSize = userArrayList.size();
        String addRFC = "ADD  " + UserName;

        System.out.println(UserName);

        StringBuffer stringBuffer = new StringBuffer("LIST ");
        for(gLoop = 0; gLoop < userListSize; gLoop++){
            clientObject = (ChatAppClientObject) userArrayList.get(gLoop);
            //Group name check
            if(clientObject.getGroupName().equals(GROUP_NAME)){
                SendMsgToClient(clientObject.getSocket(), addRFC);
                stringBuffer.append(clientObject.getUserName());
                stringBuffer.append(";");
            }
        }

        //Add user into arraylist
        clientObject = new ChatAppClientObject(ClientSocket, UserName, GROUP_NAME);
        userArrayList.add(clientObject);

        stringBuffer.append(UserName);
        stringBuffer.append(";");
        SendMsgToClient(ClientSocket, stringBuffer.toString());
    }

    //Remove user from server
    //Should be revised to fix same uid problem
    public void RemoveUser(String UserName, String GroupName, int RemoveType){
        ChatAppClientObject removeClientObject = GetClientObject(UserName);
        System.out.println("|INFO|Test Remo_1");
        if(removeClientObject != null){
            userArrayList.remove(removeClientObject);
            userArrayList.trimToSize();
            int userListSize = userArrayList.size();
            String removeRFC = null;
            if(RemoveType == REMOVE_USER)
                removeRFC = "REMO " + UserName;
            if(RemoveType == KICK_USER)
                removeRFC = "INKI " + UserName;
            //Synchronize with this remove action to all other online users
            for(gLoop = 0; gLoop < userListSize; gLoop++){
                clientObject = (ChatAppClientObject) userArrayList.get(gLoop);
                //Same chat group
                if(clientObject.getGroupName().equals(GroupName));
                    SendMsgToClient(clientObject.getSocket(), removeRFC);
            }

        }

    }

    //Remove user when exception occurs
    protected void RemoveUserWhenException(Socket clientSocket){
        int userListSize = userArrayList.size();
        ChatAppClientObject removeClientObject;
        for(gLoop = 0; gLoop < userListSize; gLoop++){
            removeClientObject = (ChatAppClientObject) userArrayList.get(gLoop);
            if(removeClientObject.getSocket().equals(clientSocket)){
                String removeUserName = removeClientObject.getUserName();
                String removeGroupName = removeClientObject.getGroupName();
                userArrayList.remove(removeClientObject);
                userArrayList.trimToSize();
                userListSize = userArrayList.size();
                String removeRFC = "REMO " + removeUserName;

                //Synchronize with all online users in the same group
                for(int loop = 0; loop < userListSize; loop++){
                    clientObject = (ChatAppClientObject) userArrayList.get(loop);
                    if(clientObject.getGroupName().equals(removeGroupName)){
                        SendMsgToClient(clientObject.getSocket(), removeRFC);
                    }
                    return;
                }
            }
        }
    }

    //Change chat group
    public void ChangeGroup(Socket ClientSocket, String UserName, String NewGroupName){
        int clientIndex = GetIndex(UserName);
        if(clientIndex >= 0){
            //Update old group to new group and send RFC
            ChatAppClientObject tempClientObject = (ChatAppClientObject) userArrayList.get(clientIndex);
            String oldGroupName = tempClientObject.getGroupName();
            tempClientObject.setGroupName(NewGroupName);
            userArrayList.set(clientIndex, tempClientObject);
            SendMsgToClient(ClientSocket, "CHGO " + NewGroupName);

            //Synchronize all related online users
            int userListSize = userArrayList.size();
            StringBuffer stringBuffer = new StringBuffer("LIST ");
            for(gLoop = 0; gLoop < userListSize; gLoop++){
                clientObject = (ChatAppClientObject) userArrayList.get(gLoop);
                if(clientObject.getGroupName().equals(NewGroupName)){
                    stringBuffer.append(clientObject.getUserName());
                    stringBuffer.append(";");
                }
            }
            SendMsgToClient(ClientSocket, stringBuffer.toString());

            //System info for old and new group online users
            String oldGroupRFC = "LEGO " + UserName + "~" +NewGroupName;
            String newGroupRFC = "JOGO " + UserName;
            for(gLoop = 0; gLoop < userListSize; gLoop ++){
                clientObject = (ChatAppClientObject) userArrayList.get(gLoop);
                if(clientObject.getGroupName().equals(oldGroupName))
                    SendMsgToClient(clientObject.getSocket(), oldGroupRFC);
                if((clientObject.getGroupName().equals(NewGroupName)) && (!(clientObject.getUserName().equals(UserName))))
                    SendMsgToClient(clientObject.getSocket(), newGroupRFC);
            }
        }
    }

    //Send general msg
    protected void SendGeneralMsg(Socket ClientSocket, String Msg, String UserName, String GroupName) throws IOException{
        boolean flooDiFlag = false;
        msgArrayList.add(UserName);
        //MAX Check
        if(msgArrayList.size() > MAX_MSG){
            msgArrayList.remove(0);
            msgArrayList.trimToSize();

            //Flooding check
            String firstMsg = (String ) msgArrayList.get(0);
            int msgListSize = msgArrayList.size();
            for(gLoop = 1; gLoop < msgListSize; gLoop++){
                if(msgArrayList.get(gLoop).equals(firstMsg)){
                    flooDiFlag = true;
                }
                else{
                    flooDiFlag = false;
                    break;
                }
            }
        }

        //Sending general msg to all online users
        int userListSize = userArrayList.size();
        String msgRFC = "MESS " + UserName + ":" + Msg;
        for(gLoop = 0; gLoop < userListSize; gLoop ++){
            clientObject = (ChatAppClientObject) userArrayList.get(gLoop);
            if((clientObject.getGroupName().equals(GroupName)) && (!(clientObject.getUserName().equals(UserName)))){
                SendMsgToClient(clientObject.getSocket(), msgRFC);
                DiFMsgPostClient DiFMsgSender = new DiFMsgPostClient(1600);
                DiFMsgSender.DiFMsgSender(msgRFC);
                DiFMsgSender.Close();
                      System.out.println("|INFO|Server Send Msg: " + msgRFC);

            }
        }

        //Kick off the user flooding msg
        if(flooDiFlag){
            SendMsgToClient(ClientSocket, "KICK ");
            msgArrayList.clear();
        }
    }

    //client to client msg sending
    protected void SendPrivateMsg(String Msg, String ToUserName){
       clientObject = GetClientObject(ToUserName);
       if(clientObject != null){
           SendMsgToClient(clientObject.getSocket(), "PRIV " + Msg);
       }
    }

    protected void SendUserIP(Socket ClientSocket, String FromUserName, String ToUserName){
        clientObject = GetClientObject(ToUserName);
        if(ClientSocket != null){
            SendMsgToClient(clientObject.getSocket(), "ADD "+ GetClientObject(FromUserName).getSocket().getInetAddress().getHostAddress() + "~" + FromUserName);
        }
    }

    protected void GetRemoteUserAddress(Socket ClientSocket, String ToUserName, String FromUserName){
        clientObject = GetClientObject(ToUserName);
        if(clientObject != null){
            SendMsgToClient(clientObject.getSocket(), "REIP " + FromUserName + "~" + ClientSocket.getInetAddress().getHostAddress());
        }
    }

    protected void SendRemoteUserAddress(Socket ClientSocket, String ToUserName, String FromUserName){
        clientObject = GetClientObject(FromUserName);
        if(clientObject != null){
            SendMsgToClient(clientObject.getSocket(), "AEIP " + ToUserName + "~" + ClientSocket.getInetAddress().getHostAddress());
        }
    }

    protected void GetUserCount(Socket ClientSocket, String GroupName){
        int userListSize = userArrayList.size();
        int userCount = 0;
        for(gLoop = 0; gLoop < userListSize; gLoop++){
            clientObject = (ChatAppClientObject) userArrayList.get(gLoop);
            if(clientObject.getGroupName().equals(GroupName))
                userCount ++;
        }
        SendMsgToClient(ClientSocket, "GOCO " + GroupName + "~" + userCount);
    }



    public static void main(String[] args) {
        ChatAppServer mainFrame = new ChatAppServer();
        mainFrame.setVisible(true);
    }

}
