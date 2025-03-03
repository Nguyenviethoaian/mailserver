package mail_server;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MailClient extends JFrame {

    private JPanel contentPane;
    private JTextField textFieldAccount;
    private JTextField textFieldPassword;
    private JTextField textFieldIP;
    private JTextField textFieldPort;
    private JTextField textFieldRecipient;
    private JTextArea textArea;
    private DatagramSocket socket;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MailClient frame = new MailClient();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MailClient() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Mail Client");
        setBounds(100, 100, 700, 600);

        contentPane = new JPanel();
        contentPane.setBackground(Color.LIGHT_GRAY);
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        JLabel lblTitle = new JLabel("Mail Client");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 22));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblIP = new JLabel("Server IP:");
        JLabel lblPort = new JLabel("Port:");
        JLabel lblAccount = new JLabel("Account:");
        JLabel lblPassword = new JLabel("Password:");
        JLabel lblRecipient = new JLabel("Recipient:");

        textFieldIP = new JTextField();
        textFieldPort = new JTextField();
        textFieldAccount = new JTextField();
        textFieldPassword = new JTextField();
        textFieldRecipient = new JTextField();

        JButton buttonConnect = new JButton("Connect");
        JButton buttonRegister = new JButton("Register");
        JButton buttonLogin = new JButton("Login");
        JButton buttonSendEmail = new JButton("Send Email");

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Setting up layout with GroupLayout for a clean UI
        GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Horizontal layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(lblTitle, GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblIP)
                        .addComponent(lblPort)
                        .addComponent(lblAccount)
                        .addComponent(lblPassword)
                        .addComponent(lblRecipient))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(textFieldIP)
                        .addComponent(textFieldPort)
                        .addComponent(textFieldAccount)
                        .addComponent(textFieldPassword)
                        .addComponent(textFieldRecipient))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(buttonConnect)
                        .addComponent(buttonRegister)
                        .addComponent(buttonLogin)
                        .addComponent(buttonSendEmail)))
                .addComponent(scrollPane)
        );

        // Vertical layout
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(lblTitle)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIP)
                    .addComponent(textFieldIP)
                    .addComponent(buttonConnect))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPort)
                    .addComponent(textFieldPort))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccount)
                    .addComponent(textFieldAccount)
                    .addComponent(buttonRegister))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(textFieldPassword)
                    .addComponent(buttonLogin))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecipient)
                    .addComponent(textFieldRecipient)
                    .addComponent(buttonSendEmail))
                .addComponent(scrollPane)
        );

        // Button event handlers
        buttonConnect.addActionListener(e -> {
            String ipAddress = textFieldIP.getText();
            int port = Integer.parseInt(textFieldPort.getText());
            connectToServer(ipAddress, port);
            sendCommand("CONNECT");
        });

        buttonRegister.addActionListener(e -> {
            String accountName = textFieldAccount.getText();
            String password = textFieldPassword.getText();
            String ipAddress = getLocalIP();  
            sendCommand("REGISTER " + accountName + " " + password+ " " + ipAddress);
        });

        buttonLogin.addActionListener(e -> {
            String accountName = textFieldAccount.getText();
            sendCommand("LOGIN " + accountName);
        });

        buttonSendEmail.addActionListener(e -> {
            String sender = textFieldAccount.getText();
            String recipient = textFieldRecipient.getText();
            String content = JOptionPane.showInputDialog("Enter email content: ");
            if (recipient.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter the recipient account!");
            } else if (content != null && !content.isEmpty()) {
                sendCommand("SEND " + recipient + " " + content);
            }
        });
    }

    private void connectToServer(String ipAddress, int port) {
        try {
            socket = new DatagramSocket();
            textArea.append("Connected to server at " + ipAddress + " on port " + port + "\n");
        } catch (Exception ex) {
            textArea.append("Error connecting to server: " + ex.getMessage() + "\n");
        }
    }

    private void sendCommand(String command) {
        if (socket == null) {
            textArea.append("Error: Not connected to server. Please click 'Connect' first.\n");
            return;
        }

        try {
            InetAddress serverAddress = InetAddress.getByName(textFieldIP.getText());
            int port = Integer.parseInt(textFieldPort.getText());
            byte[] sendData = command.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            textArea.append("Server response: " + response + "\n");

        } catch (Exception ex) {
            textArea.append("Error sending command: " + ex.getMessage() + "\n");
        }
    }
    private String getLocalIP() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to get IP";  
        }
    }
}

