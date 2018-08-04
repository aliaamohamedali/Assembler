package passes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class AssemblyPasses {
	
	// For Pass 1
	private HashMap<String,Integer> symbolTable ;
    private String fileName;
    private String IntermediateFile;
    private String SymbolTableFile;
    
    private int counter=0;
    private int startingAddress=0;
    private String name;
    
    // For Pass 2
    //private String fileName;
    private ArrayList<String> objectCodeList;
    private String objectCodeFile;
    
    public AssemblyPasses(String fileName) {
    	this.fileName = fileName;
    	
    	this.symbolTable = new HashMap<String,Integer>();
    	this.IntermediateFile=fileName.substring(0,fileName.length()-4)+"Intermediate.txt";
        this.SymbolTableFile=fileName.substring(0,fileName.length()-4)+"SymbolTable.txt";
        
        this.objectCodeList= new ArrayList<>();
        this.objectCodeFile=fileName.substring(0,fileName.length()-4)+"ObjectCode.txt";
        
    }
    
    
    
    // Pass One
    public void passOne(){
        String line = null;                                                         
        FileReader fileReader = null;
        try {
            FileWriter fileWriter = new FileWriter(this.IntermediateFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
                line=line.replaceAll("^\\s+", "");
                String[] splited= line.split("\\s+");
                if (splited.length==3){
                    if((splited[1].compareTo("START"))==0){
                        name=splited[0];
                        counter=hexToDecimal(splited[2]);
                        startingAddress=counter;
                        printWriter.println(Integer.toHexString(counter) +"     "+ splited[0]+"     "+splited[1]+"      "+splited[2]);
                        counter -=3;
                    }
                    else if(symbolTable.get(splited[0])==null){
                        this.symbolTable.put(splited[0], counter);
                        if ((splited[1].compareTo("RESW")==0)){
                            int incrementer = (Integer.valueOf(splited[2])-1)*3;
                            printWriter.println(Integer.toHexString(counter)  +"     "+ splited[0]+"    "+splited[1]+"     "+splited[2]);
                            counter+=incrementer;
                        }
                        else if ((splited[1].compareTo("RESB")==0)){
                            int incrementer = (Integer.valueOf(splited[2])-1);
                            printWriter.println(Integer.toHexString(counter)  +"     "+ splited[0]+"    "+splited[1]+"     "+splited[2]);
                            counter+=incrementer;
                        }
                        else if ((splited[1].compareTo("BYTE")==0)){
                        	if(splited[2].indexOf(0)=='X') {
                        		System.out.println("x");
                        		printWriter.println(Integer.toHexString(counter)  +"     "+ splited[0]+"     "+splited[1]+"     "+splited[2]);
                        		counter++;
                        	}
                        	else {
                              int incrementer = splited[2].length()-6;
                              printWriter.println(Integer.toHexString(counter)  +"     "+ splited[0]+"     "+splited[1]+"     "+splited[2]);
                              counter+=incrementer;
                        	}
                        }
                        else
                        {
                            printWriter.println(Integer.toHexString(counter) +"     "+ splited[0]+"     "+splited[1]+"     "+splited[2]);
                        }
                    }
                }
                else{
                    printWriter.println(Integer.toHexString(counter) +"     "+"     "+ "     "+splited[0]+"     "+splited[1]);
                }
                counter+=3;
            }
            printWriter.close();
            bufferedReader.close();                                                 
        }
        catch(java.io.IOException ex) {
            System.out.println("Unable to open file '" +fileName + "'");
        }
        this.writeSymbolTable();
    }
    
    // Pass Two
    public ArrayList<String> passTwo(HashMap<String,Integer> ISAMap) {
        try {
            FileReader fileReader =new FileReader(this.fileName);
            BufferedReader bufferedReader= new BufferedReader(fileReader);
            String line =null;
            int counter=0;
            while((line=bufferedReader.readLine())!=null){
                counter++;
                int opcode=0;
                line=line.replaceAll("^\\s+", "");
                String[] splited= line.split("\\s+");
                    if (ISAMap.get(splited[(splited.length-2)])!=null)
                        {
                        String opcodeStr= Integer.toHexString(ISAMap.get(splited[(splited.length-2)]))+"0000";
                        opcode=hexToDecimal(opcodeStr);
                       if(splited[splited.length-1].indexOf(",X")==splited[splited.length-1].length()-2){
                           opcode+=32768;                         //8000
                           opcode+=symbolTable.get(splited[splited.length-1].substring(0,(splited[splited.length-1].length()-2)));
                           opcodeStr="0000"+Integer.toHexString(opcode);
                       }
                       else{
                           opcode+=symbolTable.get(splited[splited.length-1]);
                           opcodeStr="0000"+Integer.toHexString(opcode);
                        }
                            this.objectCodeList.add(opcodeStr.substring(opcodeStr.length()-6,opcodeStr.length()));
            }
                        else if ( splited[(splited.length-2)].compareTo("WORD")==0 ){
                        String opcodeStr;
                        opcodeStr="000000"+Integer.toHexString(Integer.valueOf(splited[splited.length-1]));
                        this.objectCodeList.add(opcodeStr.substring(opcodeStr.length()-6,opcodeStr.length()));
                    }
                    else if( splited[(splited.length-2)].compareTo("BYTE")==0){
                        char[] charArray= splited[splited.length-1].substring(2,(splited[splited.length-1].length()-1)).toCharArray();
                        for (int i=0;i<charArray.length;i++){
                            this.objectCodeList.add(Integer.toHexString((int)charArray[i]));
                        }
                    }
                    else if(splited[(splited.length-2)].compareTo("START")==0 ||splited[(splited.length-2)].compareTo("RESW")==0 || splited[(splited.length-2)].compareTo("RESB")==0 ){

                    }
                    else {
                        System.out.println("error");
                    }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    this.writeIntermediateFile();    
    this.writeObjectCode(startingAddress, startingAddress+counter);  //4096,4968
    return objectCodeList;
    }
    
    // After Pass 2
    private void writeObjectCode(int startAddress,int endAddress){
        int counter=startAddress,lineCounter=0,oldstart=startAddress;
        String line="";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(this.objectCodeFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("H"+name+"  "+Integer.toHexString(startAddress)+Integer.toHexString(endAddress));
            for (String s : this.objectCodeList){
                if(lineCounter<=27){
                    if(s.length()==6 ){
                        lineCounter+=3;
                        counter+=3;
                        line =line+"  " + s;
                    }
                    else if(s.length()==2){
                        counter++;
                        lineCounter++;
                        line =line+"  " + s;
                    }
                    if (lineCounter==30){
                        String temp ="000000"+Integer.toHexString(oldstart);
                        printWriter.println("T"+temp.substring(temp.length()-6,temp.length())+Integer.toHexString(lineCounter)+line);
                        oldstart=counter;
                        lineCounter=0;
                        line="";
                    }
                }
            }
            String temp ="000000"+Integer.toHexString(oldstart);
            printWriter.println("T"+temp.substring(temp.length()-6,temp.length())+Integer.toHexString(lineCounter)+line);
            temp ="000000"+Integer.toHexString(startAddress);
            printWriter.println("E"+temp.substring(temp.length()-6,temp.length()));
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    

    // After Pass 2
    private void writeIntermediateFile(){
        FileWriter fileWriter = null;
        int counter=0,startcounter=0;
        try {
            FileReader fileReader = new FileReader(this.IntermediateFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            fileWriter = new FileWriter(fileName.substring(0,fileName.length()-4)+"obj1.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            while((line = bufferedReader.readLine()) != null) {
                String[] splited= line.split("\\s+");
                if((splited[2].compareTo("START"))!=0){
                    printWriter.println(line + "        "+objectCodeList.get(counter));
                    counter++;
                }
                else{
                    if (startcounter>1){
                        System.out.println("error");
                    }
                    else {
                        startcounter++;
                        printWriter.println(line);
                    }
                }

            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // After Pass 1
    private void writeSymbolTable(){
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(this.SymbolTableFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (String name: symbolTable.keySet()){
                String key =name.toString();
                int value = symbolTable.get(name);
                printWriter.println(key + "     " + Integer.toHexString(value));
            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
      
    private  int  hexToDecimal (String number){
        int hex=0;
        char[] charArray = number.toCharArray();
        int i=charArray.length-1;
        for (char c:charArray){
            if(Integer.valueOf(c)>=48 && Integer.valueOf(c)<=57){
                hex +=(Integer.valueOf(c)-48)*Math.pow(16,i);
                i--;
            }
            else if(Integer.valueOf(c)>=65 && Integer.valueOf(c)<=70){
                hex +=(Integer.valueOf(c)-55)*Math.pow(16,i);
                i--;
            }
            else if(Integer.valueOf(c)>=97 && Integer.valueOf(c)<=102){
                hex +=(Integer.valueOf(c)-87)*Math.pow(16,i);
                i--;
            }
        }
        return hex;
    }
    
    
    

}
