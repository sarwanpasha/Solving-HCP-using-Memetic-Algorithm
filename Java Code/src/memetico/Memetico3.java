package memetico;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import memetico.lkh.LocalSearchLKH;
import memetico.logging.IPCLogger;
import memetico.logging.NullPCLogger;
import memetico.logging.MemeticoSnapshot;
import memetico.util.LocalSearchOpName;
import tsplib4j.TSPLibInstance;
import tsplib4j.graph.DistanceTable;
import org.marcos.uon.tspaidemo.util.log.ILogger;
import org.marcos.uon.tspaidemo.util.log.ValidityFlag;
import memetico.util.ProblemConfiguration;
import memetico.util.ProblemInstance;

import java.text.*;

import java.io.IOException;
import static java.lang.Math.floor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Vector;

import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.LinkedList; 

public class Memetico3 {

    static DecimalFormat prec = new DecimalFormat("#.##");

    Population memePop;
    CrossoverOperators refCrossover = null;
    LocalSearchOperators refLocalSearch = null;
    ConstructionAlgorithms refConstr = null;
    MutationOperators refMut = null;
    DiCycleRestartOperators refRestart = null;
//    static int numReplications = 30;

    private IPCLogger logger;
    private ProblemInstance problem;
    private static double AverTotalTime = 0, AverPopInit = 0,
            AverSolOpt = 0, AverGen = 0, AverQuality = 0;
    private static double initialSolution = 0;

    public static double[][] Original_dist_mat;



    /* -------------------------------- Construtor -----------------------------*/

    /**
     * @param structSol: struct of the Solution
     * @param structPop: struct of the Population
     */
    public Memetico3(IPCLogger logger, ValidityFlag.ReadOnly continuePermission, ProblemInstance problem, 
            String structSol, String structPop, String ConstAlg, int TamPop, int TxMut, String BuscaLocal, 
            String OPCrossover, String OPRestart, String OPMutation, boolean includeLKH, long MaxTime, 
            long MaxGenNum, long reignLimit, long numReplications,FileOutputStream Final_results_dataOut,
            DataOutputStream Final_results_fileOut,String graph_name,
            int orig_length,int true_cost/*, DataOutputStream fileOut, DataOutputStream compact_fileOut*/) throws Exception {
        this.problem = problem;
        TSPLibInstance tspLibInstance = problem.getTspLibInstance();
        Instance inst;
        Instance inst_1;

        double Final_Total_Time = 0;
        double Final_Best_solution_Length = 0;
        double Final_Total_Number_of_Geracoes = 0;
   
        
        switch (tspLibInstance.getDataType()) {
            case ATSP:
            default:
                //atsp should be safe (ish) even if it is in fact tsp
                inst = new ATSPInstance();
                inst_1 = new ATSPInstance();
        }

        
        //give the memeticoInstance the required data
        inst.setDimension(tspLibInstance.getDimension());
        inst_1.setDimension(tspLibInstance.getDimension());
        
//        System.out.println("Dimentions : " + tspLibInstance.getDimension());
        double[][] memeticoMat_temp;
        {
            DistanceTable distanceTable = tspLibInstance.getDistanceTable();
            double[][] memeticoMat = ((GraphInstance) inst).getMatDist();
            for (int i = 0; i < inst.getDimension(); ++i) {
                for (int k = 0; k < inst.getDimension(); ++k) {
                    memeticoMat[i][k] = distanceTable.getDistanceBetween(i, k);
//                    System.out.print(memeticoMat[i][k] + " ");
                }
//                System.out.println();
            }
            memeticoMat_temp = memeticoMat;
        }
        Original_dist_mat = memeticoMat_temp;
       int dim_temp_2 = inst.getDimension();
        int ones_counter_2_temp = ones_counter_fun_dist_mat(Original_dist_mat, dim_temp_2);
        System.err.println("Number of ones (INITIALLY) in distance matrix = " + ones_counter_2_temp + 
                ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        this.logger = logger;
        int GenNum = 0, i;
        double TotalTime = 0;
        double start_time_2 = 0;
//        double TotalTime = 0, bestTime = 0, auxTime, recombineTime;
//        double Aver_time = 0, Aver_Gen = 0,
//                time_vmp, time_init, Quality = 0;
        int cont_OptimalSol = 0, count;
        PocCurAgent pocCurPop[];
//   FileOutputStream dataOut = new FileOutputStream("debug.txt");
//   DataOutputStream afileOut = new DataOutputStream (dataOut);


        // we set up a global upper bound, this should be done differently...
//        double best_aux = Double.MAX_VALUE;
        int ageOfBest = 0;

//   Agent refAgent = null;

        //System.out.println('\n' + "Opening file " + name);
        int temp = (int) numReplications;
        double[] final_runtime = new double[temp];
        for (count = 0; count < numReplications; count++) {
            System.out.println("numReplications = " + (count+1) + "/" + numReplications);
//            recombineTime = time_vmp = time_init = 0;
            GenNum = 0;

//      refAgent 		= selectAgentStruct(refAgent);

            logger.reset();
            
//            System.err.println("SolutionStructure.DICYCLE_TYPE = " + SolutionStructure.DICYCLE_TYPE);
            memePop = new Population(inst, TamPop, Agent.POKET_CURRENT, SolutionStructure.DICYCLE_TYPE);
            pocCurPop = (PocCurAgent[]) memePop.pop;

            refConstr = selectConstructionAlgorithm(refConstr, ConstAlg, inst);
            refMut = selectMutationOperator(refMut, OPMutation);
            refRestart = selectRestartOperator(refRestart, OPRestart, refConstr);
            refCrossover = selectCrossoverOperator(refCrossover, OPCrossover);
            refLocalSearch = selectLocalSearchOperator(refLocalSearch, BuscaLocal, inst);
            LocalSearchLKH lkh = new LocalSearchLKH(problem.getConfiguration().problemFile);

//            System.out.println("path = " + problem.getConfiguration().problemFile);

            TotalTime = System.currentTimeMillis();
            start_time_2 = System.currentTimeMillis();
//            System.err.println("refConstr = " + refConstr);
            // we initialize the population of agents
            // with a method based on the nearest neighbour heuristic for the TSP
            //I have removed this 1
            NNInicializePop(refConstr, 0, (int) (Math.random() * ((GraphInstance) inst).dimension),
                    (int) (Math.random() * ((GraphInstance) inst).dimension), inst);
            
            // Pasha logic for initialization (start)
            for (i = 0; i < memePop.popSize; i++) {
                refMut.runMutation(pocCurPop[i].current);
                pocCurPop[i].current.calculateCost(inst);
                refLocalSearch.runLocalSearch(pocCurPop[i].current, inst);
            }
            // and also mutate and optimize all the pockets except the best pocket
            for (i = 1; i < memePop.popSize; i++) {
                refMut.runMutation(pocCurPop[i].pocket);
                pocCurPop[i].pocket.calculateCost(inst);
                refLocalSearch.runLocalSearch(pocCurPop[i].pocket, inst);
            }
            
            SolutionStructure tmp_inst = pocCurPop[0].current.deepCopy();
            ((GraphInstance) inst_1).setMatDist(memeticoMat_temp);
            
            int instance_matrix_dimensions = inst.getDimension();
            //get union of two LKH here
            double temp1_inst_1[][] = initial_LKH_Tour(instance_matrix_dimensions, inst_1, lkh.runLocalSearch_individually(tmp_inst,inst_1), 
                    Original_dist_mat);
            double temp2_inst_1[][] = initial_LKH_Tour(instance_matrix_dimensions, inst_1, lkh.runLocalSearch_individually(tmp_inst,inst_1), 
                    Original_dist_mat);
            //concat the two LKH matrices here
            double dist_arr_inst_1[][] = matrix_concat(instance_matrix_dimensions, temp1_inst_1, temp2_inst_1);
            //save the union matrix for instance 1
            ((GraphInstance) inst_1).setMatDist(dist_arr_inst_1);
            // Pasha logic for initialization (end)

            // and we evalutate individual agent and return the best
            for (int ind = 0; ind < memePop.popSize; ind++) {
                memePop.evaluatePop_individually(inst_1,ind);
            }
            // and we evalutate all the agents and return the best
//            memePop.evaluatePop(inst);
//            /*log the initial generation too*/
//            logger.log(problem.getName(), memePop, GenNum);

            initialSolution += memePop.bestAgent.bestCost;
//      //System.out.println("Otima: " +OptimalSol);
            //System.out.println("melhor: " + memePop.bestAgent.bestCost);
            //System.out.println("InitPop Time " + (System.currentTimeMillis() - TotalTime) / 1000);

            refMut.mutationRate = TxMut;


            /* Here we start the Generations loop */
/*          String str = new String();
        for(i=0; i < memePop.popSize; i++) {str += pocCurPop[i].current.cost; str += " ";}
        //System.out.println("Currents: "+str);
        str = "";
          for(i=0; i < memePop.popSize; i++) {str += pocCurPop[i].pocket.cost; str += " ";}
          //System.out.println("Pockets: "+str);*/


            for(int r = 0; r<memePop.popSize; r++){
                memePop.updateAgents_individually(inst_1,r); //separate instance for each agent
            }
//            memePop.updateAgents(inst);

            int gensSinceNewBestOrRestart = 0;
            SolutionStructure bestSolution = memePop.bestSolution;
            //Generation Loop (start) 
            while (GenNum < MaxGenNum)
            // the stoping criteria used are two in this case.
            // This should be improved in a future version.
            {
                memePop.agentPropagation();

                {
                    PocCurAgent curRoot = (PocCurAgent) memePop.pop[0];
                    if (reignLimit > 0 && curRoot.noChangeCounter >= reignLimit) {
                        System.err.println("reignLimit = " + reignLimit + ", reignLimit = " + 
                                reignLimit + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        //kill the king and propagate again
                        NNInicializePop(refConstr, 0, (int) (Math.random() * ((GraphInstance) inst).dimension),
                                (int) (Math.random() * ((GraphInstance) inst).dimension), inst, false);
                        curRoot.pocket.calculateCost(inst);
                        curRoot.updateAgent(inst);
                        memePop.agentPropagation();
                        gensSinceNewBestOrRestart = 0;
                    }
                }

                if(bestSolution != memePop.bestSolution) {
                    bestSolution = memePop.bestSolution;
                    gensSinceNewBestOrRestart = 0;
                } else {
                    ++gensSinceNewBestOrRestart;
                }

                //importantly, we want to prevent any logging from happening once we lose permission, 
//                lest it interfere with other data such as loaded json
                if (!continuePermission.isValid()) {
                    return;
                }
System.err.println("memePop.bestSolution.cost = " + memePop.bestSolution.cost + ", " + 
        "problem.getTargetCost() = " + problem.getTargetCost() + ", My True Cost = " + true_cost);
                // if (memePop.bestAgent.bestCost <= OptimalSol)  break;
                if (memePop.bestSolution.cost <= problem.getTargetCost()) {
//                if (memePop.bestSolution.cost <= true_cost) {
                    break;
                }
                
//System.err.println("after Break");
                /* This seems to be the correct point at which to log since it's above the break? */
                logger.tryLog(problem.getName(), memePop, GenNum, false);

//	  memePop.orderChildren();

                recombinePocketsWithCurrents(memePop, refCrossover, inst, refLocalSearch);

                for (i = 0; i < memePop.popSize; i++) refMut.runMutation(pocCurPop[i].current);
                // if the incumbent hasn't been altered in 30 generations
                // RestartPablo
                if (gensSinceNewBestOrRestart > 0 && gensSinceNewBestOrRestart % 30 == 0) {
                    int aux_rand = (int) (Math.random() * 3 + 1);
                    NNInicializePop(refConstr, aux_rand, (int) (Math.random() * ((GraphInstance) inst).dimension),
                            (int) (Math.random() * ((GraphInstance) inst).dimension), inst);
//                    NNInicializePop(refConstr, 0, (int) (Math.random() * ((GraphInstance) inst).dimension),
//                            (int) (Math.random() * ((GraphInstance) inst).dimension), inst, false);


                    for (int ind = 0; ind < memePop.popSize; ind++) {
                        memePop.evaluatePop_individually(inst_1,ind); 
                    }
//                    memePop.evaluatePop(inst);
                    for(int r = 0; r<memePop.popSize; r++){
                        memePop.updateAgents_individually(inst_1,r);
                    }
//                    memePop.updateAgents(inst);
                    memePop.agentPropagation();

//                    System.out.println("now at Restart... incumbent cost is:" + pocCurPop[0].pocket.cost +
//                            " GenNum: " + GenNum + " Tempo: " +
//                            bestTime);

                    int mutationRateAux = TxMut;
                    TxMut = 100; // increase to high mutation rate

                    // mutate and optimize the currents
                    for (i = 0; i < memePop.popSize; i++) {
                        refMut.runMutation(pocCurPop[i].current);
                        pocCurPop[i].current.calculateCost(inst);
                        refLocalSearch.runLocalSearch(pocCurPop[i].current, inst);
                    }
                    // and also mutate and optimize all the pockets except the best pocket
                    for (i = 1; i < memePop.popSize; i++) {
                        refMut.runMutation(pocCurPop[i].pocket);
                        pocCurPop[i].pocket.calculateCost(inst);
                        refLocalSearch.runLocalSearch(pocCurPop[i].pocket, inst);
                    }
                    TxMut = mutationRateAux; // return the mutation to its previous value

                    for (int ind = 0; ind < memePop.popSize; ind++) {
                        memePop.evaluatePop_individually(inst_1,ind); 
                    }
//                    memePop.evaluatePop(inst);
                    for(int r = 0; r<memePop.popSize; r++){
                        memePop.updateAgents_individually(inst_1,r);
                    }
//                    memePop.updateAgents(inst);
                    memePop.agentPropagation();
//                    String str = new String();
           /* for(i=0; i < 4; i++) {str += pocCurPop[i].current.cost; str += " ";}
           System.out.println("Currents: "+str);
           str = ""; */
//                    for (i = 0; i < 4; i++) {
//                        str += pocCurPop[i].pocket.cost;
//                        str += " ";
//                    }
                    //System.out.println("Pockets: " + str);

	   /* System.out.println("getting out of Restart... incumbent cost is:" + pocCurPop[0].pocket.cost +
                              " GenNum: " +GenNum + " Tempo: " +
                              bestTime);*/
	                gensSinceNewBestOrRestart = 0;
                }// end of RestartPablo

//	    for(i=0; i < memePop.popSize; i++)  refLocalSearch.runLocalSearch(pocCurPop[i].current, inst);


/*        if(memePop.bestAgent.bestCost == 38707){
          String str = new String();
//        for(i=0; i < memePop.popSize; i++) {str += pocCurPop[i].current.cost; str += " ";}
//        System.out.println("Currents: "+str);
//        str = "";
          for(i=0; i < memePop.popSize; i++) {str += pocCurPop[i].pocket.cost; str += " ";}
          System.out.println("Pockets: "+str);
           ((DiCycle)((PocCurAgent)memePop.bestAgent).pocket).printDiCycle();
        }*/

                for(int r = 0; r<memePop.popSize; r++){
                    memePop.updateAgents_individually(inst_1,r);
                }
//                memePop.updateAgents(inst);

                if(includeLKH) {
                    //apply LKH to all currents
                    for (PocCurAgent agent : pocCurPop) {
//                        System.out.println("agent.noChangeCounter = " + agent.noChangeCounter + " !!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        if (agent.noChangeCounter > 1) {
                            DiCycle tmp = (DiCycle) agent.current.deepCopy();
//                            System.out.println("tmp = " + tmp);
                            lkh.runLocalSearch(tmp, inst);
                            tmp.calculateCost(inst);
                            if (memePop.isNewSolutionStructure(tmp.cost) || 
                                    memePop.pop[0].testValues(getSameValues(tmp))) {
                                agent.insertSolutionStructure(tmp);
                                agent.updateAgent(inst); //note that there is a redundant call to 
                                //calculateCost since it was already run above/for the pocket 
                                //still in the agent at the beginning of the call
//                                //don't increase the no change counter
//                                agent.noChangeCounter = 1;
                            }
                        }
                    }
//                    gensSinceNewBestOrRestart = 0; //disable restart
                } //if(includeLKH) {

                //may as well update the generation at the end of the loop, so that gen 0 reflects 
                //the state of the initial population reordered etc but before local search/recombination
                GenNum++;
                System.out.println("GenNum "+GenNum + "/" + MaxGenNum + ", Graph Name = " + graph_name);

                int dim_temp_2_2 = inst.getDimension();
        int ones_counter_2_temp_2 = ones_counter_fun_dist_mat(Original_dist_mat, dim_temp_2_2);
        System.err.println("Number of ones (before function call) in distance matrix = " + ones_counter_2_temp_2 + 
                ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                //This function insert edges iteratively in distance matrix
                double[][] dist_arr_temp = iterative_edge_insertion(dim_temp_2_2, inst_1, tmp_inst, lkh, Original_dist_mat, 
                        dist_arr_inst_1);
                dist_arr_inst_1 = dist_arr_temp;
                ((GraphInstance) inst_1).setMatDist(dist_arr_inst_1);
                
                int dim_temp = inst.getDimension();
                int ones_counter_2 = ones_counter_fun(dim_temp, inst_1);
                System.out.println("Number of ones in sparse distance matrix = " + ones_counter_2 + 
                        ", In original matrix = " + ones_counter_fun_dist_mat(Original_dist_mat, dim_temp_2_2));
            
                
                double TotalTime_2 = (System.currentTimeMillis() - start_time_2);
            
                System.out.println("Time Consumerd = " + (TotalTime_2/1000) + " seconds");
                if((TotalTime_2/1000)>600){
                    System.err.println("Time Break after 10 Minutes");
                    break;
                }
            } // end of while, exiting the generations loop.
            //Generation Loop (end)
            
            
            
            
            
            
            //force a log since this is the end of the run
            logger.log(problem.getName(), memePop, GenNum, true);
            TotalTime = (System.currentTimeMillis() - TotalTime);

//      if (count==0)                                 //?
//         pocCurPop[0].pocket.saveInOptTour(name);
//            Aver_time += TotalTime / 1000;
//            Aver_Gen += GenNum;

//            if (problem.getTargetCost() == memePop.bestAgent.bestCost)
//                cont_OptimalSol++;

//            Quality += memePop.bestAgent.bestCost;
//
//            try {
//                fileOut.writeBytes(String.valueOf(name + '\t'));                /*nome do arquivo*/
//                fileOut.writeBytes(String.valueOf(prec.format((double) (TotalTime / 1000))) + '\t');    /*tempo total da execucao*/
//                fileOut.writeBytes(String.valueOf((long) memePop.bestAgent.bestCost) + '\t');    /*melhor solucao encontrada*/
//                fileOut.writeBytes(String.valueOf(prec.format((double) bestTime)) + '\t');            /*ultima atualizacao da melhor solucao*/
//                fileOut.writeBytes(String.valueOf((int) GenNum) + '\n');            /*numero total de solucoes*/
//            } catch (IOException e) {
//                throw new Exception("File not properly opened" + e.toString());
//            }
            //System.out.println("Tempo total de execucao: " + (TotalTime / 1000) + '\t');        /*Tempo total de execucao*/
            //System.out.println("Melhor solucao encontrada: " + memePop.bestAgent.bestCost + '\t');    /*Melhor solucao encontrada*/
            //System.out.println("Numero total de Geracoes: " + GenNum + '\n');            /*Numero total de Geracoes*/
       
//      System.out.println("Total run time: "+(TotalTime/1000)+'\t');	/*Total run time*/
//      System.out.println("Best solution Length: "+memePop.bestAgent.bestCost +'\t');	/*Melhor solucao encontrada*/
//      System.out.println("Total Number of Geracoes: "+GenNum + '\n');
        
      final_runtime[count] =  (TotalTime/1000);
      
      System.out.println("Graph Name = " + graph_name);
        }//for

        
        Arrays.sort(final_runtime);
   Final_Total_Time = TotalTime/1000;
   Final_Best_solution_Length = memePop.bestAgent.bestCost;
   Final_Total_Number_of_Geracoes = GenNum;
   
   System.out.println("Final Total run time: "+final_runtime[0]+'\t');	/*Total run time*/
   System.out.println("Final Best solution Length: "+ Final_Best_solution_Length +'\t');	/*Melhor solucao encontrada*/
   System.out.println("Final Total Number of Geracoes: "+Final_Total_Number_of_Geracoes + '\n');
   
   
   try {//pasha2  
        Final_results_fileOut.writeBytes(String.valueOf(graph_name)+'\t');
        Final_results_fileOut.writeBytes(String.valueOf(orig_length)+'\t');
        Final_results_fileOut.writeBytes(String.valueOf(Final_Best_solution_Length)+'\t');	/*tempo total da execucao*/
        Final_results_fileOut.writeBytes(String.valueOf(final_runtime[0])+'\n');	/*melhor solucao encontrada*/
//        Final_results_fileOut.writeBytes(String.valueOf(Final_Total_Number_of_Geracoes)+'\n');			/*ultima atualizacao da melhor solucao*/
    }catch(IOException e){
        throw new Exception("File not properly opened" + e.toString());
    }
   
   
   
//   afileOut.close();

//        initialSolution = initialSolution / numReplications;
//        Quality = Quality / numReplications;
//        Quality = (100 * (Quality - problem.getTargetCost()) / problem.getTargetCost());
//        AverQuality += Quality;

//        try {
//            compact_fileOut.writeBytes(name + '\t');                                    /*arquivo*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (initialSolution))) + '\t');                /*solucao inicial*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (100 * (initialSolution - OptimalSol) / OptimalSol))) + '\t');    /*qualidade de solucao inicial*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (Quality))) + '\t');                    /*qualidade da solucao final*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (Aver_time / numReplications))) + '\t');            /*tempo total medio*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (Aver_Gen / numReplications))) + '\t');            /*numero de geracoes*/
//            compact_fileOut.writeBytes(String.valueOf((int) (cont_OptimalSol)) + " (" + String.valueOf((int) (numReplications)) + ")" + '\n');/*numero de solucoes otimas*/
//        } catch (IOException e) {
//            throw new Exception("File not properly opened" + e.toString());
//        }

//        AverTotalTime += (Aver_time / numReplications);
//        AverPopInit += initialSolution;
//        AverSolOpt += cont_OptimalSol;
//        AverGen += (Aver_Gen / numReplications);
    }//Constructor ends here


    /* ------------------------------------ MAIN ------------------------------------*/
    public static void main(IPCLogger logger, String args[]) {
        long MaxTime = 100, MaxGenNum;
        int PopSize = 16; /* Number of agents of the population */
        int mutationRate = 5, count;
//        int numReplications = 30;
        int numReplications = 1;
        Instance inst = null;
        Vector matRT[];
        Reduction reduction = null;
        // counter to check the number of instances, 27 in the assymetric case

        // names of the instances of the TSP which are going to be used for the
        // experiments. We are assuming they are located in the same directory.
        // A GUI would be needed here.
        String MetodoConstrutivo = "Nearest Neighbour";
        
//          String BuscaLocal = "Recursive Arc Insertion";//Recursive Arc Insertion";
//        String BuscaLocal = "3opt";//3opt;
        String BuscaLocal = "Lin–Kernighan Heuristic";// LKH;
        //C:\Program Files (x86)\Common Files\Oracle\Java\javapath (LKH Path)
        
        String SingleorDouble = "Double";
        
        String OPCrossover = "Strategic Arc Crossover - SAX";
//        String OPCrossover = "Distance Preserving Crossover - DPX";
//        String OPCrossover = "Uniform";
//        String OPCrossover = "Multiple Fragment - NNRER";
        
        String OPReStart = "Insertion",
                OPMutacao = "MutationInsertion";
        String structSol = "DiCycle";
        String structPop = "Ternary Tree";
        
        System.out.println("Local Search Operator = " + BuscaLocal);
        System.out.println("OPCrossover= " + OPCrossover);
        
        
  
int start_ind = 0; //upto 117
int countNames = 1; //number of instances

        //path = E:\RA\Pablo Moscato\Code\Java Code\JavaApplication24\build\classes\TC
        String Reduction_Name = "SR";
//        String Reduction_Name = "LK";
        String path = "/" + Reduction_Name + "/";
        String path_opt = "/opt_tour/";
        String write_path = "E:\\RA\\Pablo Moscato\\Code\\Java Code\\JavaApplication24\\build\\classes\\" + 
        Reduction_Name +"\\Results\\"; //copy
//String write_path = "E:\\RA\\Pablo Moscato\\Code\\Java Code\\JavaApplication24\\build\\classes\\" + 
//        Reduction_Name +"\\Results2\\"; //copy
//String write_path = "E:\\RA\\Pablo Moscato\\Code\\Java Code\\JavaApplication24\\build\\classes\\" + 
//        Reduction_Name +"\\Results3\\"; //copy
//        String path = "/";

String Names[] =new String[118];
String Names_opt[] =new String[118];
//long OptimalSol[] =new long[num_files];

for(int ii=0; ii<100;ii++){
    Names[ii] = path+ "graph" + (ii+1);
    Names_opt[ii] = path_opt+ "graph" + (ii+1);
//    Names[ii] = "/tsp225";
}

Names[100] = path+ "graph" + 109;
Names[101] = path+ "graph" + 110;
Names[102] = path+ "graph" + 144;
Names[103] = path+ "graph" + 145;
Names[104] = path+ "graph" + 172;
Names[105] = path+ "graph" + 173;
Names[106] = path+ "graph" + 199;
Names[107] = path+ "graph" + 200;
Names[108] = path+ "graph" + 253;
Names[109] = path+ "graph" + 268;
Names[110] = path+ "graph" + 271;
Names[111] = path+ "graph" + 272;
Names[112] = path+ "graph" + 290;
Names[113] = path+ "graph" + 298;
Names[114] = path+ "graph" + 340;
Names[115] = path+ "graph" + 703;
Names[116] = path+ "graph" + 989;
Names[117] = path+ "graph" + 1001;

Names_opt[100] = path_opt+ "graph" + 109;
Names_opt[101] = path_opt+ "graph" + 110;
Names_opt[102] = path_opt+ "graph" + 144;
Names_opt[103] = path_opt+ "graph" + 145;
Names_opt[104] = path_opt+ "graph" + 172;
Names_opt[105] = path_opt+ "graph" + 173;
Names_opt[106] = path_opt+ "graph" + 199;
Names_opt[107] = path_opt+ "graph" + 200;
Names_opt[108] = path_opt+ "graph" + 253;
Names_opt[109] = path_opt+ "graph" + 268;
Names_opt[110] = path_opt+ "graph" + 271;
Names_opt[111] = path_opt+ "graph" + 272;
Names_opt[112] = path_opt+ "graph" + 290;
Names_opt[113] = path_opt+ "graph" + 298;
Names_opt[114] = path_opt+ "graph" + 340;
Names_opt[115] = path_opt+ "graph" + 703;
Names_opt[116] = path_opt+ "graph" + 989;
Names_opt[117] = path_opt+ "graph" + 1001;

int orig_length_arr[] = {66,70,78,84,90,94,102,108,114,118,126,132,138,142,150,156,162,166,170,174,180,
    186,190,198,204,210,214,222,228,234,238,246,252,258,262,270,276,282,286,294,300,306,310,318,324,330,
    334,338,342,348,354,358,366,372,378,382,390,396,400,402,406,408,414,416,420,426,430,438,444,450,454,
    460,462,462,468,471,474,478,480,486,492,496,498,500,502,503,507,507,510,510,516,522,526,534,540,540,
    546,546,550,558,606,606,804,804,1002,1002,1200,1200,1578,1644,1662,1662,1770,1806,2010,4024,7918,9528};
int OptimalSol[] = orig_length_arr;

        try {


FileOutputStream Final_results_dataOut = new FileOutputStream(write_path + Reduction_Name + "_" +  
        OPCrossover + "_Final_result_" + (start_ind+1) + "_to_" + (start_ind + countNames) + ".txt");
DataOutputStream Final_results_fileOut = new DataOutputStream (Final_results_dataOut);


//            for (count = 0; count < countNames; count++) {
            for (count=start_ind; count < start_ind + countNames; count++){

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
                LocalDateTime now = LocalDateTime.now();  
                System.out.println(dtf.format(now));  
   
        String instance_path = Names[count] + ".tsp";
        String instance_path_opt = Names_opt[count] + ".hcp.tou";
        
        System.out.println("Graph Name = " + instance_path);
        
        String graph_name = instance_path;
        int orig_length = OptimalSol[count];
                
//        System.out.println("Graph Name = " + graph_name);
                ProblemInstance problem = ProblemInstance.create(new ProblemConfiguration(Memetico.class.getResource(instance_path), Memetico.class.getResource(instance_path_opt)));
                MaxGenNum = (int) (5 * 13 * Math.log(13) * Math.sqrt(problem.getTspLibInstance().getDimension()));

                //default to false even though the lkh performance is good; just an example configuration
                boolean include_lkh_bool = true; 
                Memetico3 meme = new Memetico3(logger, () -> true, problem, structSol, structPop, MetodoConstrutivo,
                        PopSize, mutationRate, BuscaLocal, OPCrossover, OPReStart, OPMutacao, include_lkh_bool, 
                        MaxTime, MaxGenNum, 0, numReplications,
                        Final_results_dataOut,Final_results_fileOut,
                graph_name,orig_length,orig_length_arr[count]);
            }//for

        } catch (IOException e) {
            System.err.println("File not opened properly" + e.toString());
//      System.exit(1);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
//      System.exit(1);
        }

//   System.exit(0);
    }

    public static void main(String[] args) {
        main(new NullPCLogger(), args);
    }

    public double[][] matrix_concat(int mat_size, double[][] temp1_inst_1, double[][] temp2_inst_1){
                double dist_arr_inst_1[][] = new double[mat_size][mat_size];
                
                for(int y=0;y<mat_size;y++){
                    for(int t=0;t<mat_size;t++){
                        int first_val = (int)temp1_inst_1[y][t];
                        int second_val = (int)temp2_inst_1[y][t];
                        if(first_val==1){
                            dist_arr_inst_1[y][t] = 1;
                        }
                        if(second_val==1){
                            dist_arr_inst_1[y][t] = 1;
                        }
                        if(y==t){
                            dist_arr_inst_1[y][t] = mat_size;
                        }
                        if(first_val!=1 && second_val!=1 && y!=t){
                            dist_arr_inst_1[y][t] = 2;
                        }
                    }
                }
                return dist_arr_inst_1;
            }
    
    
    public double[][] initial_LKH_Tour(int orig_inst_dim, Instance inst_1, int[]tour_arr_inst_1, 
            double[][]Original_dist_fun){
                double dist_arr[][] = array_to_matrix_first_time(orig_inst_dim,tour_arr_inst_1);

                int[] conf_short_path_temp = get_shortest_path(Original_dist_fun, tour_arr_inst_1, orig_inst_dim);
                for(int t = 0; t<(conf_short_path_temp.length-1);t++){
                        dist_arr[conf_short_path_temp[t]][conf_short_path_temp[t+1]] = 1;
                        dist_arr[conf_short_path_temp[t+1]][conf_short_path_temp[t]] = 1;
                    }

                try{
                    int conf_edge_1 = conf_short_path_temp[0];
                    int conf_edge_2 = conf_short_path_temp[(conf_short_path_temp.length-1)];

                    //replace conflicting edge value with |V| (start)
                    dist_arr[conf_edge_1][conf_edge_2] = orig_inst_dim;
                    dist_arr[conf_edge_2][conf_edge_1] = orig_inst_dim;
                    //replace conflicting edge value with |V| (end)
                }catch(Exception EX){
                    System.out.println("No conflicting edge while initialization");
                }
                ((GraphInstance) inst_1).setMatDist(dist_arr);
                return dist_arr;
    }
    
    
    public int ones_counter_fun(int dim_temp, Instance inst_1){
            double[][] test_mat_4 = ((GraphInstance) inst_1).getMatDist();
            int ones_counter = 0;
            for(int w=0; w<dim_temp; w++){
                for(int q=0; q<dim_temp; q++){
                    if(test_mat_4[w][q]==1){
                        ones_counter++;
                    }
                }
            }
//            System.out.println("Number of ones = " + ones_counter + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return ones_counter;
            }
    
public int ones_counter_fun_dist_mat(double[][] dist_mat, int dim_temp){
            double[][] test_mat_4 = dist_mat;
            int ones_counter = 0;
            for(int w=0; w<dim_temp; w++){
                for(int q=0; q<dim_temp; q++){
                    if(test_mat_4[w][q]==1){
                        ones_counter++;
                    }
                }
            }
//            System.out.println("Number of ones = " + ones_counter + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return ones_counter;
}    
    
    public double[][] iterative_edge_insertion(int orig_inst_dim, Instance inst_1, 
                SolutionStructure tmp_inst, LocalSearchLKH lkh, double[][] Original_dist_fun, double[][] sparse_mat){

                int dim_temp_2_2 = orig_inst_dim;
//                System.out.println("Number of ones (ONE) in distance matrix = " + 
//                ones_counter_fun_dist_mat(Original_dist_fun, dim_temp_2_2) + 
//                ", ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                //run LKH to get near optimal solution
                int tour_arr_inst_1[] = lkh.runLocalSearch_individually(tmp_inst,inst_1);
                double dist_arr[][] = array_to_matrix(orig_inst_dim,tour_arr_inst_1, sparse_mat);

            
                int[] conf_short_path = get_shortest_path(Original_dist_fun, tour_arr_inst_1, orig_inst_dim);

                boolean unique_path_check = check_uniqueness_of_shortest_path(conf_short_path,inst_1,sparse_mat);
//                System.err.println("unique_path_check = " + unique_path_check);

                double[][] Original_dist_mat_final = new double[orig_inst_dim][orig_inst_dim];
                for(int i=0; i<Original_dist_mat.length; i++)
                    for(int j=0; j<Original_dist_mat[i].length; j++)
                        Original_dist_mat_final[i][j]=Original_dist_mat[i][j];
                
//                double[][] Original_dist_fun_final = Original_dist_fun;
//                System.out.println("Number of ones (two) in distance matrix = " + 
//                ones_counter_fun_dist_mat(Original_dist_fun, dim_temp_2_2) + 
//                ", ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                if(unique_path_check==false){
//                    int while_counter = 0;
                    double[][] test_11 = Original_dist_mat_final;
                    while(unique_path_check==false){

//                        System.out.println("Number of ones (two point one) in distance matrix = " + 
//                ones_counter_fun_dist_mat(Original_dist_fun, dim_temp_2_2) + 
//                ", ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                        int temp_3 = (int)floor(conf_short_path.length/2);
                        test_11[conf_short_path[temp_3-1]][conf_short_path[temp_3]] = orig_inst_dim;
                        test_11[conf_short_path[temp_3]][conf_short_path[temp_3-1]] = orig_inst_dim;
                        
//                        System.out.println("Number of ones (two point two) in distance matrix = " + 
//                ones_counter_fun_dist_mat(Original_dist_fun, dim_temp_2_2) + 
//                ", ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                        
                        int[] conf_short_path_1 = get_shortest_path(test_11, tour_arr_inst_1, orig_inst_dim);
                        boolean unique_path_check_1 = check_uniqueness_of_shortest_path(conf_short_path_1,
                                inst_1,sparse_mat);

                        
                        
                        unique_path_check = unique_path_check_1;
                        conf_short_path = conf_short_path_1;
                        
//                        while_counter = while_counter+2;
//                        System.err.println("Out of While Loop = " + 
//while_counter + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                  
                }
//                System.out.println("Number of ones (THREE) in distance matrix = " + 
//                ones_counter_fun_dist_mat(Original_dist_fun, dim_temp_2_2) +
//                ", ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                
                for(int t = 0; t<(conf_short_path.length-1);t++){
                    dist_arr[conf_short_path[t]][conf_short_path[t+1]] = 1;
                    dist_arr[conf_short_path[t+1]][conf_short_path[t]] = 1;
                }
                try{
                    int conf_edge_1 = conf_short_path[0];
                    int conf_edge_2 = conf_short_path[(conf_short_path.length-1)];
                    //replace conflicting edge value with |V| (start)
                    dist_arr[conf_edge_1][conf_edge_2] = orig_inst_dim;
                    dist_arr[conf_edge_2][conf_edge_1] = orig_inst_dim;
                    //replace conflicting edge value with |V| (end)
                }catch(Exception ex){
//                    System.err.println("I AM HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                    int dim_temp_2 = orig_inst_dim;
//        int ones_counter_2_temp = ones_counter_fun_dist_mat(Original_dist_fun, dim_temp_2);
//        System.err.println("Number of ones (FINALLY) in distance matrix = " + ones_counter_2_temp + 
//                ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    //here edges are complete. Hence the sparse matrix must become equal to original matrix
                    for(int i=0; i<Original_dist_fun.length; i++)
                    for(int j=0; j<Original_dist_fun[i].length; j++)
                        dist_arr[i][j]=Original_dist_fun[i][j];
//                    dist_arr = Original_dist_fun;
                    
                }
                ((GraphInstance) inst_1).setMatDist(dist_arr);
                
//                System.err.println("Number of ones ITERATIVE END = " + ones_counter_end + ", FOR MATRIX: " + ones_counter_check_mat_2
//                        + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                
                return dist_arr;

            }//iterative edge insertion function ends here
    
    public boolean check_uniqueness_of_shortest_path(int[] conf_short_path, Instance inst_1, double[][] old_sparse_mat){
            boolean unique_check = false;
            int unique_edge_check = 0;
//            double[][] test_1 = ((GraphInstance) inst_1).getMatDist();
            double[][] test_1 = old_sparse_mat;
            for (int ii = 0; ii < conf_short_path.length-1; ii++) {
                int temp_2 = (int)test_1[conf_short_path[ii]][conf_short_path[ii+1]];
                if (temp_2==1){
                    unique_edge_check++;
                }
            }
//            System.out.println("Counter Check = " + unique_edge_check + ", Out of: " + (conf_short_path.length-1));
            if(unique_edge_check!=(conf_short_path.length-1)){
                unique_check = true;
            }
            return unique_check;
            }
    
    public int[] get_shortest_path(double[][] Original_dist_fun, int[] tour_arr_inst_1, int orig_inst_dim){
            // Getting unique shortest path between endpoints on conflicting edge (start)
            int conf_finder_inst_1[][] = conflicting_edge_finder(tour_arr_inst_1,Original_dist_fun);
//            System.out.println("First = " + conf_finder_inst_1[0][0] + ", Second = " + conf_finder_inst_1[0][1]);
            Dijkstras_Shortest_Path inst_1_path = new Dijkstras_Shortest_Path();
            // Adjacency list for storing which vertices are connected 
            ArrayList<ArrayList<Integer>> adj = new ArrayList<ArrayList<Integer>>(orig_inst_dim); 
            for (int i_check = 0; i_check < orig_inst_dim; i_check++) { 
                adj.add(new ArrayList<Integer>()); 
            } 
            for (int i_check = 0; i_check < orig_inst_dim; i_check++)
            {
                for (int i_check_2 = 0; i_check_2 < orig_inst_dim; i_check_2++)
                {
                    int temp_var = (int)Original_dist_fun[i_check][i_check_2];
                    if (temp_var == 1){
                        inst_1_path.addEdge(adj, i_check, i_check_2);
                        inst_1_path.addEdge(adj, i_check_2, i_check);
                    }
                }
            }
            int source = conf_finder_inst_1[0][0];
            int destination = conf_finder_inst_1[0][1];
            int[] conf_short_path = inst_1_path.printShortestDistance(adj, source, destination, orig_inst_dim);
            return conf_short_path;
            }
    
    
public int[][] conflicting_edge_finder(int tour_arr[], double[][]Original_dist_fun){
    int conf_finder[][] = new int[1][2];
    for(int q=1; q<tour_arr.length;q++){

        if(q!=(tour_arr.length-1)){
            int temp_check = (int)tour_arr[q];
            int temp_check2 = (int)tour_arr[q+1];
            int check = (int)Original_dist_fun[temp_check][temp_check2];
            if(check!=1){
                conf_finder[0][0] = temp_check;
                conf_finder[0][1] = temp_check2;
            }
        }else{
            int temp_check = (int)tour_arr[q];
            int check = (int)Original_dist_fun[temp_check][0];
            if(check!=1){
                conf_finder[0][0] = temp_check;
                conf_finder[0][1] = 0;
            }
        }
    }
    return conf_finder;
}


public double[][] array_to_matrix_first_time(int dimensions,int tour_arr[]){
    double dist_arr[][] = new double[dimensions][dimensions];
    
//    double dist_arr[][] = dist_mat_old;
    for(int w = 0;w<dimensions; w++){
        for(int q = 0;q<dimensions; q++){
            if(w==q){
                    dist_arr[w][q] = dimensions;
                    dist_arr[q][w] = dimensions;
            }
            else{
                dist_arr[w][q] = 2;
                dist_arr[q][w] = 2;
            }
        }
    }
//            int count_check = 0;
            for(int w = 0;w<dimensions; w++){
                if(w<(dimensions-1)){
                    dist_arr[tour_arr[w]][tour_arr[w+1]] = 1;
                    dist_arr[tour_arr[w+1]][tour_arr[w]] = 1;
//                    count_check++;
                }else{
                    dist_arr[tour_arr[w]][tour_arr[0]] = 1;
                    dist_arr[tour_arr[0]][tour_arr[w]] = 1;
//                    count_check++;
                }
            }
//            System.out.println("count_check = " + count_check);
return dist_arr;
            }

public double[][] array_to_matrix(int dimensions,int tour_arr[], double[][] dist_mat_old){
//    double dist_arr[][] = new double[dimensions][dimensions];
    
    double dist_arr[][] = dist_mat_old;

//            int count_check = 0;
            for(int w = 0;w<dimensions; w++){

                if(w<(dimensions-1)){
                    dist_arr[tour_arr[w]][tour_arr[w+1]] = 1;
                    dist_arr[tour_arr[w+1]][tour_arr[w]] = 1;
//                    count_check++;
                }else{
                    dist_arr[tour_arr[w]][tour_arr[0]] = 1;
                    dist_arr[tour_arr[0]][tour_arr[w]] = 1;
//                    count_check++;
                }
            }
//            System.out.println("count_check = " + count_check);
return dist_arr;
            }
    /* ------------------------------------ selectCrossoverOperator ------------------------------------*/
/*public Agent selectSolutionStruct(Agent refAgent, String StructSol)
{
   return(refAgent);
}
*/

    /* ------------------------------------ selectSolutionStruct -----------------------------------*/
/*public SolutionStruct selectSolutionStruct (String SolutionStr)
{
   if (SolutionStr.equals("DiCycle")){
      DiCycle dc = new DiCycle(Instance.Dimension);
   }
   return();
}
*/
    /* ------------------------------------ selectCrossoverOperator ------------------------------------*/
    public CrossoverOperators selectCrossoverOperator(CrossoverOperators refCrossover, String OPCrossover) {
        if (OPCrossover.equals("Strategic Arc Crossover - SAX")) {
            CrossoverSAX sax = new CrossoverSAX();
            refCrossover = sax;
        } else if (OPCrossover.equals("Distance Preserving Crossover - DPX")) {
            CrossoverDPX dpx = new CrossoverDPX();
            refCrossover = dpx;
        } else if (OPCrossover.equals("Uniform")) {
            CrossoverUNN unn = new CrossoverUNN();
            refCrossover = unn;
        } else if (OPCrossover.equals("Multiple Fragment - NNRER")) {
            CrossoverMFNN mfnn = new CrossoverMFNN();
            refCrossover = mfnn;
        }
        return (refCrossover);
    }


    /* ------------------------------------ selectRestartOperator ------------------------------------*/
    public DiCycleRestartOperators selectRestartOperator(DiCycleRestartOperators refRestart, String OPRestart, ConstructionAlgorithms refConstr) {
        if (OPRestart.equals("Insertion")) {
            RestartInsertion mi = new RestartInsertion();
            mi.setConstAlg(refConstr);
            refRestart = mi;
        } else if (OPRestart.equals("Cut")) {
            RestartCut mi = new RestartCut();
            mi.setConstAlg(refConstr);
            refRestart = mi;
        }
        return (refRestart);

    }


    /* ------------------------------------ selectMutationOperator ------------------------------------*/
    public MutationOperators selectMutationOperator(MutationOperators refMut, String OPMutation) {
//   if(OPMutation.equals("Insertion")){
        MutationInsertion mi = new MutationInsertion();
        refMut = mi;
//   }
        return (refMut);

    }


    /* ------------------------------------ selectConstructionAlgorithm ------------------------------------*/
    public ConstructionAlgorithms selectConstructionAlgorithm(ConstructionAlgorithms refConstr, String Constr, Instance inst) {
        NearestNeigh nn = new NearestNeigh(inst);
        refConstr = nn;

        return (refConstr);
    }


    /* ------------------------------------ selectCrossoverOperator ------------------------------------*/
    public LocalSearchOperators selectLocalSearchOperator(LocalSearchOperators refLocalSearch, String OPLocalSearch, Instance inst) throws IOException {
        if (OPLocalSearch.equals("Recursive Arc Insertion")) {
            LocalSearchRAI rai = new LocalSearchRAI(inst);
            refLocalSearch = rai;
        } else if (OPLocalSearch.equals("3opt")) {
            LocalSearch3opt opt3 = new LocalSearch3opt();
            refLocalSearch = opt3;
        } else if(OPLocalSearch.equals("Lin–Kernighan Heuristic")) {
//            System.out.println("path = " + problem.getConfiguration().problemFile);
            refLocalSearch = new LocalSearchLKH(problem.getConfiguration().problemFile);
        } else /*if (OPLocalSearch.equals("3opt"))*/ {
            LocalSearchJohnson lsj = new LocalSearchJohnson(inst);
            refLocalSearch = lsj;
        }

        return (refLocalSearch);
    }


    /* ------------------------------------ Recombine_Pockets_with_Currents ------------------------------------*/
    private void recombinePocketsWithCurrents(Population memePop, CrossoverOperators refCrossover, Instance inst, 
            LocalSearchOperators refLocalSearch) throws Exception {
        GraphInstance graphInst = (GraphInstance) (inst);
        int endPoint[][] = new int[graphInst.dimension][2];              /* just in case ?? */
        SolutionStructure parentA, parentB;
        DiCycle child = new DiCycle(graphInst.dimension);

        parentA = ((PocCurAgent) memePop.pop[1]).pocket; /* pop: The population is declared as an array of agents */
        parentB = ((PocCurAgent) memePop.pop[2]).current; /* pop: The population is declared as an array of agents */

//   System.out.println("Crossover");
        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[0].testValues(getSameValues(child)))
            memePop.pop[0].insertSolutionStructure(child);
        else for (int i = 0; i < graphInst.dimension; i++)
            child.dontlook[i] = true;

        for (int i = 1; i < memePop.nrParents; i++)
            Crossover(refCrossover, memePop, endPoint, i, inst, refLocalSearch);
    }


    /* ------------------------------------ Crossover ------------------------------------*/
    private void Crossover(CrossoverOperators refCrossover, Population memePop, int EndPoint[][], int parent, 
            Instance inst, LocalSearchOperators refLocalSearch) throws Exception {
        GraphInstance graphInst = (GraphInstance) inst;   //?
        PocCurAgent pocCurPop[] = (PocCurAgent[]) memePop.pop;
        int child1, child2, child3, i;
        SolutionStructure parentA, parentB;
        DiCycle child;

        child1 = parent * 3 + (int) (Math.random() * 3);
        child2 = child1 + 1;

        if (child2 == parent * 3 + 4)
            child2 -= 3;

        child3 = child2 + 1;

        if (child3 == parent * 3 + 4)
            child3 -= 3;


        parentA = pocCurPop[child3].pocket;
        parentB = pocCurPop[child2].pocket;

        child = new DiCycle(graphInst.dimension);

        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[parent].testValues(getSameValues(child)))
            memePop.pop[parent].insertSolutionStructure(child);
        else
            for (i = 0; i < graphInst.dimension; i++)
                child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();

        child = new DiCycle(graphInst.dimension);

//verificar ultima linha...

        parentA = pocCurPop[parent].pocket;
        parentB = pocCurPop[child2].current;

        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[child1].testValues(getSameValues(child)))
            memePop.pop[child1].insertSolutionStructure(child);
        else
            for (i = 0; i < graphInst.dimension; i++)
                child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();

        child = new DiCycle(graphInst.dimension);

        parentA = pocCurPop[child1].pocket;
        parentB = pocCurPop[child3].current;

        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[child2].testValues(getSameValues(child)))
            memePop.pop[child2].insertSolutionStructure(child);
        else
            for (i = 0; i < graphInst.dimension; i++)
                child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();

        child = new DiCycle(graphInst.dimension);

        parentA = pocCurPop[child2].pocket;
        parentB = pocCurPop[child1].current;

        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[child3].testValues(getSameValues(child)))
            memePop.pop[child3].insertSolutionStructure(child);
        else
            for (i = 0; i < graphInst.dimension; i++)
                child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();
    }

    /* ------------------------------------ NNInicializaPop --------------------*/
    private int[][] getSameValues(DiCycle child) {
        int values[][] = new int[10][3], city;

        for (int i = 0; i < 10; i++) {
            city = (int) (Math.random()) * child.size;
            values[i][0] = city;
            values[i][1] = child.arcArray[city].from;
            values[i][2] = child.arcArray[city].tip;
        }
        return (values);
    }


    /* ------------------------------------ NNInicializaPop --------------------*/
    private void NNInicializePop(ConstructionAlgorithms refConstr, int ind, int startcity1, int startcity2, 
            Instance inst, boolean includeSubPop) {
        int last3Poc[], last3Cur[];       //?
        GraphInstance graphInst = (GraphInstance) inst;

        if (graphInst.graphType == GraphInstance.ATSP_TYPE &&
                ((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
            if (startcity1 == 0)
                startcity1++;
            if (startcity2 == 0)
                startcity2++;
        }
        // we run a Nearest Neighbour algorithm to initialize the Pop[ind].Pocket
        // solution starting from city startcity1. We keep record of the last
        // three cities added to the path.
        last3Poc = refConstr.runConstrAlg(((PocCurAgent) memePop.pop[ind]).pocket, startcity1, inst);

        
        // we analogously create the current solution, now using startcity2 as
        // the starting city
//   for (i=0; i<3; i++) last3[i][1] = last3[i][0];
        last3Cur = refConstr.runConstrAlg(((PocCurAgent) memePop.pop[ind]).current, startcity2, inst);
//        System.out.println("memePop.pop[" + ind + "]).current = " + ((PocCurAgent) memePop.pop[ind]).current
//                + ", !!!!!!!!!!!!!!");
        if(includeSubPop) {
            if (last3Poc[0] == last3Cur[0] || last3Poc[0] == last3Cur[1] || last3Poc[0] == last3Cur[2] ||
                    last3Poc[0] == last3Poc[1] || last3Poc[0] == last3Poc[2])
                last3Poc[0] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Poc[1] == last3Cur[0] || last3Poc[1] == last3Cur[1] || last3Poc[1] == last3Cur[2] ||
                    last3Poc[1] == last3Poc[0] || last3Poc[1] == last3Poc[2])
                last3Poc[1] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Poc[2] == last3Cur[0] || last3Poc[2] == last3Cur[1] || last3Poc[2] == last3Cur[2] ||
                    last3Poc[2] == last3Poc[0] || last3Poc[2] == last3Poc[1])
                last3Poc[2] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Cur[0] == last3Cur[1] || last3Cur[0] == last3Cur[2] || last3Cur[0] == last3Poc[0] ||
                    last3Cur[0] == last3Poc[1] || last3Cur[0] == last3Poc[2])
                last3Cur[0] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Cur[1] == last3Cur[0] || last3Cur[1] == last3Cur[2] || last3Cur[1] == last3Poc[0] ||
                    last3Cur[1] == last3Poc[1] || last3Cur[1] == last3Poc[2])
                last3Cur[1] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Cur[2] == last3Cur[0] || last3Cur[2] == last3Cur[1] || last3Cur[2] == last3Poc[0] ||
                    last3Cur[2] == last3Poc[1] || last3Cur[2] == last3Poc[2])
                last3Cur[2] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (ind < memePop.nrParents) {
                // we recursively initialze the solutions using the NNAlgorithm
                // seeding it with the last visited cities of the leader agent
                NNInicializePop(refConstr, ind * 3 + 1, last3Poc[0], last3Cur[0], inst);
                NNInicializePop(refConstr, ind * 3 + 2, last3Poc[1], last3Cur[1], inst);
                NNInicializePop(refConstr, ind * 3 + 3, last3Poc[2], last3Cur[2], inst);
            }
        }
    }

    private void NNInicializePop(ConstructionAlgorithms refConstr, int ind, int startcity1, int startcity2, Instance inst) {
//        System.err.println("ind = " + ind + ", !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        NNInicializePop(refConstr, ind, startcity1, startcity2, inst, true);
    }


    public ILogger<MemeticoSnapshot> getLogger() {
        return logger;
    }
}//fim da classe
