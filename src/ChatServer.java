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
    public void sendClientList(){
        String cl = "";
        for(ServerThread st : clientList){
            if(st.socket.isConnected()){
                cl = cl+st.name+";";
            }else{
                clientList.remove(st);
            }
        }
        updateList(clientList);
        sendToAllClients("-UPDATED CLIENTSLIST:" +cl.substring(0,cl.length()-1));
    }
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
        updateList(clientList);
    }

    public void run(){
        String received;
        try {

            name = inputStream.readUTF();

            sendToAllClients(name+" join to chat!");
        }catch (Exception e){
            e.printStackTrace();
        }

        while(true) {
            try {


                received = inputStream.readUTF();

                if(received.equals("Quit")){
                    System.out.println(name+" quit!");
                    sendToAllClients(name+": "+received);
                    socket.close();
                    sendClientList();
                    break;
                }
                if(received.contains("-UPDATE CLIENTSLIST")){
                    System.out.println("-UPDATE CLIENTSLIST");
                    sendClientList();
                }else {
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
                clientList.add(serverThread);
                serverThread.updateList(clientList);
                serverThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}