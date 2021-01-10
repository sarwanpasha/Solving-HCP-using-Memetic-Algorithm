/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaApplication24;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class JavaApplication24 {

    public static void main (String args[]){
System.out.println("Started!!!!");
        int instance_number = 989;
        String Instance_Name = "E:/RA/Pablo\\ Moscato/dataset/FHCPCS/graph" + instance_number + ".hcp";
        String Program_Directory = "E:/RA/Pablo\\ Moscato/Code/Poland\\ Group\\ Paper/hc_tw_experiments-master/src";
        String command = "cd " + Program_Directory + "; ./hc_simple < " + Instance_Name;
        int[] tour = polish_group_logic(command);
        if(tour.length!=1){
        for(int i=0;i<tour.length;i++){
            System.out.print(tour[i] + ", ");
        }
        System.out.println();
        }else{
            System.out.println("Not Solved");
        }
        
    }
    
    public static int[] polish_group_logic(String command){
//        System.out.println("Executing BASH command:\n   " + command);
        Runtime r = Runtime.getRuntime();

        // write all commands with ; as a separator
        String[] commands = {"C:/cygwin/bin/bash.exe", "-c", command};
        String[] splited = new String[1];
        try {
            Process p = r.exec(commands);

            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            String line1 = "";
            String line2 = "";
            String line3 = "";
            int counter = 0;
            while ((line = b.readLine()) != null) {
//                System.out.println("line = " + line);
                counter++;
                if(counter==1){
                    line1 = line;
                }else if(counter==2){
                    line2  = line;
                }else if(counter==3){
                    line3 = line;
                }
            }
            b.close();
//            System.out.println("line 1 = " + line1);
//            System.out.println("line 2 = " + line2);
//            System.out.println("line 3 = " + line3);
            
            splited = line2.split(" ");
//            System.out.println("line 2 Length = " + (int)splited.length);
            return processLine(splited);
//            success = true;
        } catch (Exception e) {
            System.err.println("Failed to execute bash with command: " + command);
            e.printStackTrace();
        }
//        System.err.println("success = " + success);
        return processLine(splited);
    }
    
    private static int[] processLine(String[] strings) {
    int[] intarray=new int[strings.length];
    int i=0;
    for(String str:strings){
        try {
            intarray[i]=Integer.parseInt(str);
            i++;
        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException("Not a number: " + str + " at index " + i, e);
        }
    }
    return intarray;
}
}
