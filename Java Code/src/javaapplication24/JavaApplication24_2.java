/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaApplication24;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JavaApplication24_2 {

    static double time = 0;
    static double tour_length = 0;
    static String tour_name = "";
    
    public static void main(String args[]) throws FileNotFoundException, Exception {

        String Reduction_Name = "TC";
        String write_path = "E:\\RA\\Pablo Moscato\\Code\\Java Code\\JavaApplication24\\build\\classes\\" + Reduction_Name + "\\Results\\";

//        int[] inst_arr = {185};
        int inst_arr[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 75, 76, 77, 78, 80, 81, 82, 83, 85, 86, 87, 88, 89, 91, 92, 93, 94, 95, 97, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 129, 131, 132, 133, 135, 136, 137, 138, 139, 140, 141, 142, 143, 146, 147, 148, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 163, 164, 165, 166, 167, 168, 169, 170, 171, 174, 175, 176, 177, 178, 180, 181, 182, 183, 184, 185, 186, 187, 189, 190, 191, 193, 194, 195, 196, 197, 198, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 225, 228, 229, 230, 231, 232, 233, 234, 235, 237, 238, 239, 240, 241, 242};

        int orig_length_arr[] = {66, 70, 78, 84, 90, 94, 102, 108, 114, 118, 126, 132, 138, 142, 150, 156, 162, 166, 170, 174, 180, 186, 190, 198, 204, 210, 214, 222, 228, 234, 238, 246, 252, 258, 262, 270, 276, 282, 286, 294, 300, 306, 310, 318, 324, 330, 334, 338, 342, 348, 354, 358, 366, 372, 378, 382, 390, 396, 400, 402, 406, 408, 414, 416, 420, 426, 430, 438, 444, 450, 454, 468, 471, 474, 478, 486, 492, 496, 498, 502, 503, 507, 507, 510, 516, 522, 526, 534, 540, 546, 550, 558, 564, 570, 574, 576, 582, 588, 594, 598, 606, 612, 618, 622, 623, 630, 631, 636, 642, 646, 654, 656, 659, 660, 670, 671, 676, 684, 694, 708, 718, 731, 732, 736, 742, 747, 756, 766, 780, 790, 804, 814, 816, 828, 838, 845, 851, 852, 856, 862, 876, 886, 896, 900, 910, 924, 934, 948, 958, 972, 976, 982, 996, 1006, 1014, 1020, 1030, 1044, 1054, 1054, 1056, 1068, 1078, 1092, 1102, 1116, 1126, 1136, 1140, 1150, 1164, 1174, 1183, 1188, 1198, 1206, 1212, 1216, 1222, 1236, 1246, 1260, 1270, 1284, 1294, 1296, 1308, 1318, 1332, 1342, 1352, 1356, 1366, 1376, 1377, 1380, 1381, 1390, 1404, 1414, 1428, 1438, 1452, 1456, 1461, 1462, 1476, 1486, 1500, 1501, 1510, 1521};

        int start_ind = 0;
        int end_ind = inst_arr.length;
        FileOutputStream Final_results_dataOut = new FileOutputStream(write_path + Reduction_Name + "_"
                + "_Final_result_" + inst_arr[start_ind] + "_to_" + inst_arr[(start_ind + end_ind) - 1] + ".txt");
        DataOutputStream Final_results_fileOut = new DataOutputStream(Final_results_dataOut);

        for (int i = start_ind; i < start_ind + end_ind; i++) {
            String graph_name = "graph" + inst_arr[i];
            double TotalTime = 0;
            TotalTime = System.currentTimeMillis();
            String Instance_Name = "E:/RA/Pablo\\ Moscato/dataset/FHCPCS/graph" + inst_arr[i] + ".hcp";
            String Program_Directory = "E:/RA/Pablo\\ Moscato/Code/Poland\\ Group\\ Paper/hc_tw_experiments-master/src";
            String command = "cd " + Program_Directory + "; ./hc_simple < " + Instance_Name;
//        boolean success = false;
            System.out.println("Executing BASH command:\n   " + command);
            Runtime r = Runtime.getRuntime();

            // write all commands with ; as a separator
            String[] commands = {"C:/cygwin/bin/bash.exe", "-c", command};

            int[] tour = polish_group_logic(command);

            TotalTime = (System.currentTimeMillis() - TotalTime);
            time = TotalTime / 1000;
            for (int u = 0; u < tour.length; u++) {
                tour[u] = tour[u] - 1;
            }
            if (tour.length != 1) {
                tour_length = orig_length_arr[start_ind];
                System.out.println("Final Total run time: " + time + '\t');
                /*Total run time*/
                System.out.println("Final Best solution Length: " + tour_length + '\t');
                /*Melhor solucao encontrada*/

                for (int u = 0; u < tour.length; u++) {
                    System.out.print(tour[u] + ", ");
                }
                System.out.println();
                System.out.println("SOLVED BY POLISH !!!!!!!!!!!!!!!!!");
            } else {
                tour_length = -1;
                System.out.println("Final Total run time: " + time + '\t');
                /*Total run time*/
                System.out.println("Final Best solution Length: " + tour_length + '\t');
                /*Melhor solucao encontrada*/
                System.out.println("Not Solved !!!!!!!!!!!!!!!!!");
            }
            try {//pasha2  
                Final_results_fileOut.writeBytes(String.valueOf(graph_name) + '\t');
                Final_results_fileOut.writeBytes(String.valueOf(orig_length_arr[start_ind]) + '\t');
                Final_results_fileOut.writeBytes(String.valueOf(tour_length) + '\t');
                /*tempo total da execucao*/
                Final_results_fileOut.writeBytes(String.valueOf(time) + '\n');
                /*melhor solucao encontrada*/
//        Final_results_fileOut.writeBytes(String.valueOf(Final_Total_Number_of_Geracoes)+'\n');			/*ultima atualizacao da melhor solucao*/
            } catch (IOException e) {
                throw new Exception("File not properly opened" + e.toString());
            }
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
