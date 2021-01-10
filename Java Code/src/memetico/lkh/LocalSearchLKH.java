package memetico.lkh;

import com.google.common.io.ByteStreams;
import memetico.*;
import org.apache.commons.lang.SystemUtils;
import tsplib4j.TSPLibInstance;
import tsplib4j.TSPLibTour;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Requires that an LHK executable is available via the PATH env var
 */
public class LocalSearchLKH extends DiCycleLocalSearchOperator {
    //these are /all/ necessary - in case the problem doesn't already exist on disk.
    private File problemFile;
    private File paramFile;
    private File initialTourFile;
    private File resultTourFile;
    private File candidateFile;
    private File piFile;
//    private String baseParams;
//
    public LocalSearchLKH(URL problemResource) throws IOException {
        problemFile = File.createTempFile("currentProblem", ".tsp");
        InputStream tmpInput = problemResource.openStream();
        OutputStream tmpOutput = new FileOutputStream(problemFile);
        ByteStreams.copy(tmpInput, tmpOutput);
        tmpInput.close();
        tmpOutput.close();
        paramFile = File.createTempFile("lhkConfig", ".par");
        initialTourFile = File.createTempFile("initialSolution", ".tsp");
        resultTourFile = File.createTempFile("resultSolution", ".tsp");
        candidateFile = new File(String.format("candidates%s.lkhdat", UUID.randomUUID()));
        piFile = new File(String.format("pi%s.lkhdat", UUID.randomUUID()));
//        StringWriter tmpWriter = new StringWriter();
        PrintWriter paramOutlet = new PrintWriter(new FileWriter(paramFile));
        paramOutlet.printf("PROBLEM_FILE = %s%n", problemFile.getPath());
        paramOutlet.printf("INITIAL_TOUR_FILE = %s%n", initialTourFile.getPath());
        paramOutlet.printf("TOUR_FILE = %s%n", resultTourFile.getPath());
        paramOutlet.printf("CANDIDATE_FILE = %s%n", candidateFile.getPath());
        paramOutlet.printf("PI_FILE = %s%n", piFile.getPath());
        paramOutlet.printf("MAX_TRIALS = %s%n", 1);
        paramOutlet.println("RUNS = 1");
//        paramOutlet.println("STOP_AT_OPTIMUM = YES");
//        paramOutlet.print("OPTIMUM = ");
        paramOutlet.close();
//        baseParams = tmpWriter.toString();

        //make sure they are deleted when the jvm closes
        problemFile.deleteOnExit();
        paramFile.deleteOnExit();
        initialTourFile.deleteOnExit();
        resultTourFile.deleteOnExit();
        candidateFile.deleteOnExit();
        piFile.deleteOnExit();
    }

    public void runLocalSearch(SolutionStructure soln, Instance inst) {
//        System.err.println("soln = " + soln);
//        System.err.println("inst = " + inst);
        
        DiCycle destination = (DiCycle) soln;
        try {
            destination.saveInOptTour(initialTourFile);
//            System.err.println("initialTourFile = " + initialTourFile);
            Runtime r = Runtime.getRuntime();
//            System.err.println("Runtime = " + r);
            //path = C:\Users\Pasha\AppData\Local\Temp\lhkConfig7612249004811815173.par
            
//            String[] aa = new String[]{"LKH", paramFile.getPath()};
//            System.out.println("Path = " + (new String[]{"LKH", paramFile.getPath()}) + ", !!!!!!!!!!!!!!!!!!!!!!!!");
            
            Process p = r.exec(new String[]{"LKH", paramFile.getPath()});
//            System.err.println("Process = " + p);
//            Process p = r.exec("E:\\RA\\Pablo Moscato\\Code\\C Code\\LKH\\LKH-2.0.9\\LKH.exe");
            OutputStream outlet = p.getOutputStream();
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outlet));
//            System.err.println("BufferedWriter = " + writer);
            writer.write("\n");
            writer.flush();
            writer.close();
//            System.err.println("Before");
            p.waitFor();
//            System.err.println("p.exitValue() = " + p.exitValue());
            if(p.exitValue() != 0) {
                System.err.println("Error: LKH Failed - printing stdout and stderr outputs..");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String eachLine = reader.readLine();
                while (eachLine != null) {
                    System.out.println(eachLine);
                    eachLine = reader.readLine();
                }
                reader.close();
                reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                eachLine = reader.readLine();
                while (eachLine != null) {
                    System.err.println(eachLine);
                    eachLine = reader.readLine();
                }
                reader.close();
            }

            TSPLibInstance tmpInst = new TSPLibInstance(resultTourFile);
//            System.out.println("resultTourFile = " + resultTourFile + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            TSPLibTour theTour = tmpInst.getTours().get(0);
//            System.out.println("theTour.size() = " + theTour.size() + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            for (int i = 0; i < theTour.size(); i++) {
                int cur = theTour.get(i);
                int next = theTour.get((i + 1) % theTour.size()); //this % means that at the end, "next" will return to the start of the tour
                destination.arcArray[cur].tip = next;
                destination.arcArray[next].from = cur;
                
//                System.out.print(cur + ", ");
            }
//            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public int[] runLocalSearch_individually(SolutionStructure soln, Instance inst) {
//        System.err.println("soln = " + soln);
//        System.err.println("inst = " + inst);
//isAvailable();
        int tour_arr [] = new int [inst.getDimension()];
        DiCycle destination = (DiCycle) soln;
        try {
            destination.saveInOptTour(initialTourFile);
//            System.err.println("initialTourFile = " + initialTourFile);
            Runtime r = Runtime.getRuntime();
//            System.err.println("Runtime = " + r);
            //path = C:\Users\Pasha\AppData\Local\Temp\lhkConfig7612249004811815173.par
            
//            String[] aa = new String[]{"LKH", paramFile.getPath()};
//            System.out.println("Path = " + (new String[]{"LKH", paramFile.getPath()}) + ", !!!!!!!!!!!!!!!!!!!!!!!!");
            
            Process p = r.exec(new String[]{"LKH", paramFile.getPath()});
//            System.err.println("Process = " + p);
//            Process p = r.exec("E:\\RA\\Pablo Moscato\\Code\\C Code\\LKH\\LKH-2.0.9\\LKH.exe");
            OutputStream outlet = p.getOutputStream();
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outlet));
//            System.err.println("BufferedWriter = " + writer);
            writer.write("\n");
            writer.flush();
            writer.close();
//            System.err.println("Before");
            p.waitFor();
//            System.err.println("p.exitValue() = " + p.exitValue());
            if(p.exitValue() != 0) {
                System.err.println("Error: LKH Failed - printing stdout and stderr outputs..");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String eachLine = reader.readLine();
                while (eachLine != null) {
                    System.out.println(eachLine);
                    eachLine = reader.readLine();
                }
                reader.close();
                reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                eachLine = reader.readLine();
                while (eachLine != null) {
                    System.err.println(eachLine);
                    eachLine = reader.readLine();
                }
                reader.close();
            }

            TSPLibInstance tmpInst = new TSPLibInstance(resultTourFile);
//            System.out.println("resultTourFile = " + resultTourFile + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            TSPLibTour theTour = tmpInst.getTours().get(0);
            
//            System.out.println("theTour.size() = " + theTour.size() + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            for (int i = 0; i < theTour.size(); i++) {
                int cur = theTour.get(i);
                int next = theTour.get((i + 1) % theTour.size()); //this % means that at the end, "next" will return to the start of the tour
//                destination.arcArray[cur].tip = next;
//                destination.arcArray[next].from = cur;
                tour_arr[i] = cur;
                
//                System.out.print(cur + ", ");
            }
//            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tour_arr;
    }    
    /**
     * Identifies whether or not the LKH executable is available in the PATH (and therefore whether or not this heuristic can be used)
     * Curtesy of https://stackoverflow.com/a/23539220
     * @return
     */
    public static boolean isAvailable() {
        String exec = SystemUtils.IS_OS_WINDOWS ? "LKH.exe" : "LKH";
//        System.err.println("System.getenv(\"PATH\") = " + System.("PATH"));
//        System.err.println("LKH Path = " + exec);
System.err.println("PASHA!!!!!!!!!!!!!!!  = " + Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve(exec))));
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve(exec)));
    }
}