/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import common.FileData;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author yeula
 */
public class UDPClient {
    private static final int PIECES_OF_FILE_SIZE = 1024 * 32;
    private DatagramSocket clientSocket;
    private int port;
    private String serverHost;
    private int serverPort;

    public DatagramSocket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(DatagramSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    
    
    public UDPClient(String serverIP, int serverPort){
        this.serverHost = serverIP;
        this.serverPort = serverPort;
    }
    
    public UDPClient(){};
    public boolean initServerUDP() {
        
        try{
            Random r = new Random();
            port = r.nextInt(9999-1001) + 1001;
            clientSocket = new DatagramSocket(port);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public void openClient() {
        while(!initServerUDP())
        {
            System.out.println("Client is opened on port " + port);
        }
    }
    
    public void multiThreadSendMessage(String message){
        
        Thread sent = new Thread() {

            public void run() {
                byte[] sentBuf = new byte[1000];
                try{
                    InetAddress sentHost = InetAddress.getByName(serverHost);
                    sentBuf = message.getBytes("ascii");
                    DatagramPacket sentPacket = new DatagramPacket(sentBuf, sentBuf.length, sentHost, serverPort);

                    clientSocket.send(sentPacket);
                }
                catch (Exception e)
                { 
                        e.printStackTrace();
                }
            }
        };
        
        sent.start();
    }
   
    
    public void multiThreadReceiveFile(JTable jTableListFileClient) {
        
        Thread receive = new Thread(){
            
            public void run(){
                    
                byte[] receiveData = new byte[PIECES_OF_FILE_SIZE];
                DatagramPacket receivePacket;
                // get file info
                try {
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);

                new MultiThreadReceive(receiveData, receivePacket, clientSocket, port, jTableListFileClient).start();

                //end 
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        receive.start();
    }
    
}


class MultiThreadReceive extends Thread{
    private static final int PIECES_OF_FILE_SIZE = 1024 * 32;
    byte[] receiveData = new byte[PIECES_OF_FILE_SIZE];
    DatagramPacket receivePacket;
    private DatagramSocket clientSocket;
    private int port;
    private JTable jTableListFileClient;
    
    public MultiThreadReceive(byte[] receiveData, DatagramPacket receivePacket, DatagramSocket clientSocket, int port, JTable jTableClient){
        this.receiveData = receiveData;
        this.receivePacket = receivePacket;
        this.clientSocket = clientSocket;
        this.port = port;
        this.jTableListFileClient = jTableClient;
    }
    
    public void run(){
        String fileN = "";
        try{
        //new thread
        DefaultTableModel model = (DefaultTableModel) jTableListFileClient.getModel();
        int percent = 0;
        InetAddress inetAddress = receivePacket.getAddress();
        System.out.println(receivePacket.getData().length);
        ByteArrayInputStream bais = new ByteArrayInputStream(
                receivePacket.getData());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FileData fileInfo = null;
            try {
                fileInfo = (FileData) ois.readObject();
                
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(null,
                "Can't not Download file",
                "Inane error",
                JOptionPane.ERROR_MESSAGE);
            }

        // show file info
        int rowfile = 0;
        if (fileInfo != null) {
            fileN = fileInfo.getFileName();
            System.out.println("File name: " + fileInfo.getFileName());
            System.out.println("File size: " + fileInfo.getSizeByte());
            System.out.println("Pieces of file: " + fileInfo.getPiecesOfFile());
            System.out.println("Last bytes length: "+ fileInfo.getLastByteLength());
            
            rowfile = LoadListFileToTable(fileInfo);
        }
        // get file content
        System.out.println("Receiving file...");
        File fileReceive = new File(fileInfo.getDestinationDirectory() 
                + fileInfo.getFileName());
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(fileReceive));
        // write pieces of file
        for (int i = 0; i < (fileInfo.getPiecesOfFile() - 1); i++) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length, 
                    inetAddress, port);
            clientSocket.receive(receivePacket);
            bos.write(receiveData, 0, PIECES_OF_FILE_SIZE);
            
            percent = (i *100) / (fileInfo.getPiecesOfFile() - 1);
            model.setValueAt(percent + "%", rowfile, 2);
        }
        // write last bytes of file
        receivePacket = new DatagramPacket(receiveData, receiveData.length, 
                inetAddress, port);
        clientSocket.receive(receivePacket);
        bos.write(receiveData, 0, fileInfo.getLastByteLength());
        bos.flush();
        
        model.setValueAt("DONE", rowfile, 2);

        // close stream
        bos.close();
        }catch(IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
            "Can't not download file : ." + fileN,
            "Inane error",
            JOptionPane.ERROR_MESSAGE);
        }
    }
    
     private int  LoadListFileToTable(FileData file){
        DefaultTableModel model;      
        model = (DefaultTableModel) jTableListFileClient.getModel();
            Object[] items = new Object[]{
                file.getFileName(),
                file.getSizeAutoConvert(),
                "0%"
            };

            model.addRow(items);
        
        return model.getRowCount() - 1;
    }
}
