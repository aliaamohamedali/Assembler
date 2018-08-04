package iSA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class ISALoader {
	private HashMap<String,Integer> ISAMap ;
    private String fileName;
    
    public ISALoader(String fileName) {
    	this.fileName = fileName;
    	this.ISAMap = new HashMap<String, Integer>();
    }
    
    public void parseISAFile() {
     
    	String line = null;                                                         
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
                String[] splited= line.split("\\s+");
                this.ISAMap.put(splited[0], Integer.decode(splited[1]));             
            }
            bufferedReader.close();                                                
        }
        catch(java.io.IOException ex) {
            System.out.println("Unable to open file '" +fileName + "'");
        }   			
    }
    
    public HashMap<String,Integer> getISAMap(){
    	return this.ISAMap;
    }

}
