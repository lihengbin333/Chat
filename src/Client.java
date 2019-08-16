import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
class ChatWindow {
    JFrame loginframe, chatFrame;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    String name = "";
    JPanel chat_panel = new JPanel();

    public ChatWindow(DataOutputStream dataOutputStream, DataInputStream dataInputStream){
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        windows();
    }
    public void windows(){
        loginframe = new JFrame("Login");
        loginframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginframe.add(loginPanel(),BorderLayout.CENTER);
        loginframe.setSize(500,250);
        loginframe.setLocationRelativeTo(null);
        loginframe.setVisible(true);
    }
    public JPanel loginPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(null);
        chat_panel.setLayout(null);
        JLabel label = new JLabel("Please Enter Your Name: ");
        label.setBounds(145,50,200,10);
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label);
        JTextField textField = new JTextField();
        textField.setBounds(120,70,250,25);
        panel.add(textField);
        JButton submit = new JButton("Join");
        submit.setBounds(185,125,100,25);
        panel.add(submit);
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    name = textField.getText();
                    dataOutputStream.writeUTF(name);
                    loginframe.dispose();
                    chatFrame = new JFrame(name + "'s Chatroom");


                    Thread sendMsg = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            JTextField tf = new JTextField();
                            tf.setBounds(50,350,500,50);
                            chat_panel.add(tf);
                            JButton send = new JButton("Send");
                            send.setBounds(575,350,100,50);
                            chat_panel.add(send);

                            send.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    String toSend = tf.getText();
                                    try {
                                        dataOutputStream.writeUTF(toSend);
                                        tf.setText("");
                                        //System.out.println(toSend);
                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    dataOutputStream.writeUTF("-UPDATE CLIENTSLIST");

                    Thread rcvdMsg = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            JList cls,chat;
                            JScrollPane client_pane,chat_pane;
                            DefaultListModel chatHistory = new DefaultListModel();
                            TitledBorder chatBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),"Messages");
                            TitledBorder clientBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),"Clients");



                            while (true){
                                try {
                                    String received = dataInputStream.readUTF();
                                    if(received.contains("-UPDATED CLIENTSLIST:")){
                                        String[] arr1 = received.split(":");
                                        String[] clientsList = arr1[1].split(";");
                                        DefaultListModel listModel = new DefaultListModel();
                                        //System.out.println(clientsList[0]);
                                        for(String s : clientsList){
                                            listModel.addElement(s);
                                        }
                                        cls = new JList(listModel);
                                        client_pane = new JScrollPane(cls);
                                        client_pane.setBounds(50,50,100,275);
                                        client_pane.setBorder(clientBorder);
                                        //sp.setViewportView(ls);
                                        chat_panel.add(client_pane);
                                    }else {
                                        chatHistory.addElement(received);
                                        chat = new JList(chatHistory);
                                        chat_pane = new JScrollPane(chat);
                                        chat_pane.setBounds(200,50,450,275);
                                        chat_pane.setBorder(chatBorder);
                                        chat_panel.add(chat_pane);

                                        System.out.println(received);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
                    sendMsg.start();

                    rcvdMsg.start();


                    chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    chatFrame.add(chat_panel, BorderLayout.CENTER);
                    chatFrame.setSize(750, 500);
                    chatFrame.setLocationRelativeTo(null);
                    chatFrame.setVisible(true);
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });
        return panel;
    }

}

public class Client
{
    private static final int PORT = 1234;
    public static void main(String[] args) throws Exception
    {
        Scanner in = new Scanner(System.in);
        InetAddress ip = InetAddress.getByName("localhost");
        Socket socket = new Socket(ip,PORT);
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());


        ChatWindow chatWindow = new ChatWindow(dataOutputStream,dataInputStream);

    }
} 