package mail_server;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;

public class MailServer extends JFrame {

    private JPanel contentPane;
    private JTextField textFieldIP;
    private JTextArea textArea;
    private DatagramSocket socket;
    private JTextField textFieldPort;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MailServer frame = new MailServer();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MailServer() throws UnknownHostException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 450); // Resize the window
        contentPane = new JPanel();
        contentPane.setBackground(new Color(173, 216, 230)); // Light blue background
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblTitle = new JLabel("Mail UDP Server");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 20)); // Bold title
        lblTitle.setBounds(240, 10, 200, 30); // Centered title
        contentPane.add(lblTitle);

        JLabel lblIP = new JLabel("Server IP:");
        lblIP.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblIP.setBounds(40, 60, 80, 25);
        contentPane.add(lblIP);

        textFieldIP = new JTextField();
        textFieldIP.setBounds(130, 60, 150, 25);
        contentPane.add(textFieldIP);
        textFieldIP.setColumns(10);
        textFieldIP.setText(InetAddress.getLocalHost().getHostAddress());

        JLabel lblPort = new JLabel("Port:");
        lblPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblPort.setBounds(40, 100, 80, 25);
        contentPane.add(lblPort);

        textFieldPort = new JTextField();
        textFieldPort.setBounds(130, 100, 150, 25);
        contentPane.add(textFieldPort);
        textFieldPort.setColumns(10);
        textFieldPort.setText("8888");

        JButton btnStartServer = new JButton("Start Server");
        btnStartServer.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnStartServer.setBounds(300, 80, 150, 30);
        contentPane.add(btnStartServer);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(40, 150, 550, 200);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false); // Make it read-only
        scrollPane.setViewportView(textArea);

        JLabel lblLogs = new JLabel("Server Logs:");
        lblLogs.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblLogs.setBounds(40, 130, 100, 20);
        contentPane.add(lblLogs);

        btnStartServer.addActionListener(e -> {
            String ipAddress = textFieldIP.getText();
            int port = Integer.parseInt(textFieldPort.getText());
            startServer(ipAddress, port);
        });
    }

    private void startServer(String ipAddress, int port) {
        try {
            socket = new DatagramSocket(port, InetAddress.getByName(ipAddress));
            textArea.append("Server started at " + ipAddress + " on port " + port + "\n");

            new Thread(() -> {
                try {
                    byte[] receiveData = new byte[1024];
                    while (true) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        if (socket != null) {
                            socket.receive(receivePacket);
                        }

                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        InetAddress clientAddress = receivePacket.getAddress();
                        int clientPort = receivePacket.getPort();
                        String response = processClientRequest(message, clientAddress, clientPort);
                        byte[] sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                        if (socket != null) {
                            socket.send(sendPacket);
                        }

                        // Log the message and response in the text area
                        textArea.append(response + "\n");
                    }
                } catch (Exception ex) {
                    textArea.append("Server error: " + ex.getMessage() + "\n");
                }
            }).start();

        } catch (Exception ex) {
            textArea.append("Error starting server: " + ex.getMessage() + "\n");
            socket = null;
        }
    }

    private String processClientRequest(String message, InetAddress clientAddress, int clientPort) {
        String[] parts = message.split(" ");
        String command = parts[0];
        String accountName = parts.length > 1 ? parts[1] : "";
        String password = parts.length > 2 ? parts[2] : ""; 
        String ipAddress = parts.length > 3 ? parts[3] : "";
        String response = "";

        switch (command) {
            case "CONNECT":
                textArea.append("Client connected: " + clientAddress.getHostAddress() + "\n");
                break;
            case "REGISTER":
                response = registerAccount(accountName, password, ipAddress);
                break;
            case "SEND":
                if (parts.length >= 2) {
                    String receiver = parts[1];
                    String sender = parts[2];
                    String emailContent = message.substring(command.length() + receiver.length() +  sender.length() + 2);
                    response = sendEmail(receiver, emailContent, sender); 
                    String emailList= loginAndListEmails(receiver);
                    textArea.append(emailList);
                }
                break;
            case "LOAD":  
                response = loadEmails(accountName); 
                break;
            case "LOGIN":
                response = loginAndListEmails(accountName);
                textArea.append("Account " + accountName + " logged in from IP: " + clientAddress.getHostAddress() + "\n");
                break;
            default:
                response = "Invalid command!";
                break;
        }
        return response;
    }

    private String registerAccount(String accountName, String password, String ipAddress) {
        if (accountName.isEmpty()) {
            return "Invalid account name!";
        }

        String baseFolderPath = "D:/mailserver";  
        File accountFolder = new File(baseFolderPath, accountName);

        if (accountFolder.exists()) {
            return "Account already exists!";
        }

        accountFolder.mkdirs();
        try {
            File accountFile = new File(accountFolder, accountName + "_info.txt");
            FileWriter accountWriter = new FileWriter(accountFile);
            String time =  LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));
            accountWriter.write("Account: " + accountName + "\n");
            accountWriter.write("Password: " + password + "\n");
            accountWriter.write("IP Address: " + ipAddress + "\n");
            accountWriter.write("Created At: " + time + "\n");
            accountWriter.write("Status: Welcome to our email service.");
            accountWriter.close();

            File newEmail = new File(accountFolder, "welcome_email.txt");
            FileWriter emailWriter = new FileWriter(newEmail);
            emailWriter.write("Welcome to our service! This is your first email.");
            emailWriter.close();

        } catch (IOException e) {
            return "Error creating account!";
        }

        return "Account registered successfully!";
    }

    private String sendEmail(String receiver, String emailContent, String sender) {
    	
    	File receiverFolder = new File("D:/mailserver/" + receiver);
        
        

        if (!receiverFolder.exists()) {
            return "Receiver account not found!";
        }

        
        try {
        	String time =  LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));
            
        	File emailFile = new File(receiverFolder, "email_from_" + sender + "_" + time+ ".txt");

            FileWriter writer = new FileWriter(emailFile);
            writer.write(emailContent);
            writer.close();
            return "Email sent successfully to " + receiver + "!";
        } catch (IOException e) {
            return "Error sending email: " + e.getMessage();
        }
    }

    private String loginAndListEmails(String accountName) {
        String baseFolderPath = "D:/mailserver"; 
        File accountFolder = new File(baseFolderPath, accountName);

        if (!accountFolder.exists()) {
            return "Account not found!";
        }

        File[] files = accountFolder.listFiles();
        if (files == null || files.length == 0) {
            return "No emails found!";
        }

        StringBuilder response = new StringBuilder("Emails in account " + accountName + ":\n");
        for (File file : files) {
            response.append(file.getName()).append("\n");
        }
        return response.toString();
    }
    

    private String loadEmails(String accountName) {
        String baseFolderPath = "D:/mailserver"; 
        File accountFolder = new File(baseFolderPath, accountName);

        if (!accountFolder.exists()) {
            return "Account not found!";
        }

        File[] files = accountFolder.listFiles();
        if (files == null || files.length == 0) {
            return "No emails found!";
        }

        StringBuilder response = new StringBuilder("Emails in account " + accountName + ":\n");
        for (File file : files) {
            
            if (file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                } catch (IOException e) {
                    response.append("Error reading email file: ").append(file.getAbsolutePath()).append("\n");
                }
            }
        }
        return response.toString();
    }

}

