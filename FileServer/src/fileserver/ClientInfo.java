/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileserver;

/**
 *
 * @author yeula
 */
public class ClientInfo {
    private String addressIP;
    private int port;
    private String fileRequest;
    
    public ClientInfo(String addIP, int port, String fileName){
        this.addressIP = addIP;
        this.port = port;
        this.fileRequest = fileName;
    }
    
    public ClientInfo(){
        
    }

    public String getAddressIP() {
        return addressIP;
    }

    public void setAddressIP(String addressIP) {
        this.addressIP = addressIP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFileRequest() {
        return fileRequest;
    }

    public void setFileRequest(String fileRequest) {
        this.fileRequest = fileRequest;
    }
    
    
}
