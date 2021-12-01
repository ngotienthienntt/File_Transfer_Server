/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileserver;

import com.sun.xml.internal.ws.api.message.Packet;
import common.FileData;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author yeula
 */
public class UDPServer {

    private static final int PIECES_OF_FILE_SIZE = 1024 * 32;
    private DatagramSocket serverSocket;
    private String clientHost = "localhost";
    private String serverHost = "localhost";
    private int serverPort = 6678;
    private boolean running;
    private Thread receive;
    private String sourcePath = "./DataFile/";

    private ArrayList<ClientInfo> ListClient = new ArrayList<ClientInfo>();

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }


//start server
    
    public boolean initServerUDP() {
        
        try{
            Random r = new Random();
            serverPort = r.nextInt(9999-1001) + 1001;
            serverSocket = new DatagramSocket(serverPort);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public void openServer() {
        while(!initServerUDP())
        {
            System.out.println("Server is opened on port " + serverPort);
        }
    }

    
    public void disconnectClient(){
        if(serverSocket !=  null)
            serverSocket.close();
    }

    public void receiveMultiClient(JTextArea jText) {
        receive = new Thread("receive_thread") {

            public void run() {
                System.out.println("Server running");
                while (true) {
                    byte[] receivedBuf = new byte[1000];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedBuf, receivedBuf.length);
                    byte[] data = null;
                    try {

                        serverSocket.receive(receivedPacket);
                        
                        data = new byte[receivedPacket.getLength()];
                        System.arraycopy(receivedPacket.getData(), receivedPacket.getOffset(), data, 0, receivedPacket.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ClientInfo tempClient = new ClientInfo(receivedPacket.getAddress().getHostAddress(),
                            receivedPacket.getPort(), new String(data));

                    ListClient.add(tempClient);
                    String strClient = tempClient.getAddressIP() + ":" + tempClient.getPort() + " | "
                            + tempClient.getFileRequest() + "\n";
                    jText.setText(jText.getText() + strClient);

                    sentFileMultiClient(sourcePath + tempClient.getFileRequest(), "./Data/", tempClient);
                }

            }

        };

        receive.start();
    }


    public void sentFileMultiClient(String sPath, String destinationDir, ClientInfo client) {
        //System.out.println(sourcePath);
        Thread sent = new Thread() {

            public void run() {
                InetAddress inetAddress;
                DatagramPacket sendPacket;

                try {
                    
                    File fileSend = new File(sPath);    

                    InputStream inputStream = new FileInputStream(fileSend);
                    
                    BufferedInputStream bis = new BufferedInputStream(inputStream);
                    inetAddress = InetAddress.getByName(client.getAddressIP());
                    
                    byte[] bytePart = new byte[PIECES_OF_FILE_SIZE];
                    
                    // get file size
                    long fileLength = fileSend.length();
                    int piecesOfFile = (int) (fileLength / PIECES_OF_FILE_SIZE);
                    int lastByteLength = (int) (fileLength % PIECES_OF_FILE_SIZE);

                    // check last bytes of file
                    if (lastByteLength > 0) {
                        piecesOfFile++;
                    }

                    // split file into pieces and assign to fileBytess
                    byte[][] fileBytess = new byte[piecesOfFile][PIECES_OF_FILE_SIZE];
                    int count = 0;
                    while (bis.read(bytePart, 0, PIECES_OF_FILE_SIZE) > 0) {
                        fileBytess[count++] = bytePart;
                        bytePart = new byte[PIECES_OF_FILE_SIZE];
                    }

                    // read file info
                    FileData fileInfo = new FileData();
                    fileInfo.setFileName(fileSend.getName());
                    fileInfo.setSize(fileSend.length());
                    fileInfo.setPiecesOfFile(piecesOfFile);
                    fileInfo.setLastByteLength(lastByteLength);
                    fileInfo.setDestinationDirectory(destinationDir);

                    // send file info
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(fileInfo);
                    sendPacket = new DatagramPacket(baos.toByteArray(),
                            baos.toByteArray().length, inetAddress, client.getPort());
                    serverSocket.send(sendPacket);
                    

                    // send file content
                    System.out.println("Sending file...");
                    // send pieces of file
                    for (int i = 0; i < (count - 1); i++) {
                        sendPacket = new DatagramPacket(fileBytess[i], PIECES_OF_FILE_SIZE,
                                inetAddress, client.getPort());
                        serverSocket.send(sendPacket);
                        waitMillisecond(40);
                    }
                    // send last bytes of file
                    sendPacket = new DatagramPacket(fileBytess[count - 1], PIECES_OF_FILE_SIZE,
                            inetAddress, client.getPort());
                    serverSocket.send(sendPacket);
                    waitMillisecond(40);

                    // close stream
                    bis.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Sent.");
            }

        };

        sent.start();
    }

    public void waitMillisecond(long millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
