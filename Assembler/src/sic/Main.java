package sic;
import iSA.ISALoader;
import passes.AssemblyPasses;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		HashMap<String,Integer> ISAMap = new HashMap<String,Integer>();
		ArrayList<String> objectCodeList = new ArrayList<>();
        
        
        /*
        OBJECT object = OBJECT.getInstance("SIC.txt");
        */
        
        ISALoader isaLoader= new ISALoader("D:\\MyProjects\\EclipseProjects\\Assembler\\inAndOut\\ISA.txt");
        AssemblyPasses passes = new AssemblyPasses("D:\\MyProjects\\EclipseProjects\\Assembler\\inAndOut\\prog3-indexed.txt");
        
        /*
        PassOne passOne= PassOne.getInstance("test.txt");
        PassTwo passTwo=PassTwo.getInstance("test.txt");
        */
        
        isaLoader.parseISAFile();
        ISAMap = isaLoader.getISAMap();
        passes.passOne();
        objectCodeList = passes.passTwo(ISAMap);
        
        
        // Print object code list
        for (int i=0;i<objectCodeList.size();i++){
            System.out.println(objectCodeList.get(i));
        }
        
        /*
        symbolTable = passOne.doPass();
        passOne.writeSymbolTable();                             // Inside PassOne
        objectCode = passTwo.doPass(ISAMap,symbolTable);        
        printArraylist(objectCode);
        passTwo.writeFile(symbolTable);                         // Inside passTwo
        object.write(objectCode,4096,4968);                     // Inside PassTwo
        */
    
	}

}
