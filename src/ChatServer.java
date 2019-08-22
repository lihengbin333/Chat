/***************************************

 Hengbin Li
 Chat
 --chat server
 Creating server for chatting program allow
 to accept multiple connections/users.

****************************************/
import java.io.*;
import java.util.*;
import java.net.*;
class ServerThread extends Thread{
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private ArrayList<ServerThread> clientList = new ArrayList<ServerThread>();
    private String name;
    public ServerThread(Socket socket, DataInputStream inputStream, DataOutputStream outputStream){
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
    //send connected clients list to all connected clients
    public void sendClientList(){
        String cl = "";
        for(ServerThread st : clientList){
            if(st.socket.isConnected()){
                cl = cl+st.name+";";
            }else{
                //delete offline client
                clientList.remove(st);
            }
        }
        //send updated clients list to all connected clients
        sendToAllClients("-UPDATED CLIENTSLIST:" +cl.substring(0,cl.length()-1));
    }

    //send messages to all connected clients
    public void sendToAllClients(String msg){
        System.out.println(msg);
        for(ServerThread st : clientList){
            if(st.socket.isConnected()){
                try {
                    st.outputStream.writeUTF(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                clientList.remove(st);
            }

        }

    }

    public void run(){
        String received;
        try {
            //receive first msg from user which is user's name
            name = inputStream.readUTF();
            //notify all connected user new user is connected
            sendToAllClients(name+" join to chat!");
        }catch (Exception e){
            e.printStackTrace();
        }
        //continuously receive messages from users
        while(true) {
            try {

                received = inputStream.readUTF();

                //disconnect user if it says "Quit"
                if(received.equals("Quit")){
                    System.out.println(name+" quit!");
                    sendToAllClients(name+": "+received);
                    socket.close();
                    sendClientList();
                    break;
                }
                //request to update clients list
                if(received.contains("-UPDATE CLIENTSLIST")){
                    System.out.println("-UPDATE CLIENTSLIST");
                    sendClientList();
                }else {
                    //send users' messages to all users
                    sendToAllClients(name + ": " + received);
                }



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try
        {
            // closing
            this.inputStream.close();
            this.outputStream.close();

        }catch(IOException e){
            e.printStackTrace();
        }

    }
    public void updateList(ArrayList<ServerThread> clientList){
        this.clientList = clientList;
    }
}
public class ChatServer {
    private static final int PORT = 1234;

    public static void main(String[] args) {

        try {
            //initialization ServerSocket and socket for waiting user connection
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started");
            System.out.println("Waiting for connection ...");
            Socket socket = null;
            DataOutputStream outputStream = null;
            DataInputStream inputStream = null;
            ArrayList<ServerThread> clientList = new ArrayList<ServerThread>();
            while (true) {

                socket = serverSocket.accept();
                System.out.println("Accepted");
                outputStream = new DataOutputStream(socket.getOutputStream());
                inputStream = new DataInputStream(socket.getInputStream());
                ServerThread serverThread = new ServerThread(socket, inputStream, outputStream);

                //add new user to clients list
                clientList.add(serverThread);
                serverThread.updateList(clientList);
                serverThread.start();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
