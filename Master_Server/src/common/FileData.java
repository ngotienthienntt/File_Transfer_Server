/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;

/**
 *
 * @author yeula
 */
public class FileData implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private String destinationDirectory;
    private String sourceDirectory;
    private int piecesOfFile;
    private int lastByteLength;
    private String status;
    
    private String fileName;
    private long size;
    private String lastModified;
    private String type;

    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    public void setDestinationDirectory(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public int getPiecesOfFile() {
        return piecesOfFile;
    }

    public void setPiecesOfFile(int piecesOfFile) {
        this.piecesOfFile = piecesOfFile;
    }

    public int getLastByteLength() {
        return lastByteLength;
    }

    public void setLastByteLength(int lastByteLength) {
        this.lastByteLength = lastByteLength;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSizeByte() {
        return size;
    }
    
    public String getSizeAutoConvert() {
        
        if (this.size < 1024)
            return Round(this.size) + "Byte";
        else if (this.size < Math.pow(1024, 2))
            return Round(this.size / 1024) + "KB";
        else if (this.size < Math.pow(1024, 3))
            return Round(this.size / Math.pow(1024, 2)) + "MB";
        else 
            return Round(this.size / Math.pow(1024, 3)) + "GB";
    }
    
    private double Round(double a){       
        return  Math.round(a * 100.0) / 100.0;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
}
