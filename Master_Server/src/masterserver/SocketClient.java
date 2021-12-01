/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package masterserver;

import common.FileData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * @author Van_Son_Ho
 */
public class SocketClient extends Thread {
    protected Socket socket;
    protected JTable listConnectionTable;
    protected JTable listFileTable;

    public SocketClient(Socket socket, JTable listConnectionTable, JTable listFileTable) {
        this.socket = socket;
        this.listConnectionTable = listConnectionTable;
        this.listFileTable = listFileTable;
    }

    public void run() {
        try {
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            
            OutputStream os = socket.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            

            int index = 0;
            String reciveString = br.readLine();
            if ("client".equals(reciveString)) {
                DefaultTableModel listFile = (DefaultTableModel) listFileTable.getModel();
                bw.write(listFile.getDataVector().toString());
                bw.newLine();
                bw.flush();
                
                DefaultTableModel model = (DefaultTableModel) listConnectionTable.getModel();
                model.addRow(new Object[]{socket.getPort(), "Client"});
                model.getRowCount();
                listConnectionTable.setModel(model);
                
                while (true) {
                    try {
                        if (socket != null) {
                            String msg = "";
                            while ((msg = br.readLine()) != null) {
                                System.out.println(msg);
                                if ("Load".equals(msg)) {
                                    DefaultTableModel listFileSenToClientAgain = (DefaultTableModel) listFileTable.getModel();
                                    bw.write(listFile.getDataVector().toString());
                                    bw.newLine();
                                    bw.flush();
                                }
                                if ("Close".equals(msg)) {
                                    model = (DefaultTableModel) listConnectionTable.getModel();
                                    model.removeRow(index);
                                    listConnectionTable.setModel(model);
                                    break;
                                }
                            }
                        }
                        br.close();
                        bw.close();
                        break;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }else {
                //Nhận dữ liệu file server 
                String strFile = br.readLine();
                
                strFile = strFile.replace("], ", "/");
                strFile = strFile.replace("[", "");
                strFile = strFile.replace("]", "");
                int port = Integer.parseInt(reciveString);
                LoadListFileToTable(strFile, reciveString);
                
                DefaultTableModel modelListConnection = (DefaultTableModel) listConnectionTable.getModel();
                modelListConnection.addRow(new Object[]{socket.getPort(), "File Server"});
                modelListConnection.getRowCount();
                listConnectionTable.setModel(modelListConnection);
                while (true) {
                    try {
                        if (socket != null) {
                            String msg = "";
                            
                            while ((msg = br.readLine()) != null) {
                                
                                if ("Close".equals(msg)) {
                                    modelListConnection = (DefaultTableModel) listConnectionTable.getModel();
                                    modelListConnection.removeRow(index);
                                    listConnectionTable.setModel(modelListConnection);
                                    
                                    DefaultTableModel modelListFile = (DefaultTableModel) listFileTable.getModel();
                                    
                                    int modelListFileLen = modelListFile.getRowCount();
                                    for(int i = 0; i < modelListFileLen; i++){
                                        if(modelListFile.getValueAt(i, 5).toString().equals(String.valueOf(port))){
                                            modelListFile.removeRow(i);
                                            i--;
                                        }
                                        
                                    }
                                    //listFileTable.setModel(modelListFile);
                                    
                                    
                                    break;
                                }
                            }
                        }
                        br.close();
                        break;
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    
    private void  LoadListFileToTable(String strFile, String port){
        
        DefaultTableModel tableModel = (DefaultTableModel) listFileTable.getModel();

        String[] rmStrings = strFile.split("/");
        for (String rmString : rmStrings) {
            tableModel.addRow(new Object[]{rmString.split(",")[0],rmString.split(",")[1],rmString.split(",")[2], 
                            rmString.split(",")[3], socket.getInetAddress().getHostAddress(), port});
            
        }
        listFileTable.setModel(tableModel);
    }
}
