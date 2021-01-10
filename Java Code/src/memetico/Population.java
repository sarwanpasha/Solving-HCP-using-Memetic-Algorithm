package memetico;

public class Population {

    /*  VARIAVEIS CONSTANTES  */
    public int popSize,    /* Number of agents of the population */
            nrParents;  /* for a ternary tree topology, number of parent nodes */

    public Agent bestAgent,
            worstAgent;
    
    public Agent allAgents[] = new Agent[13];

    public int n_ary = DEFAULT_N_ARY, newBestSol = 0;

    public static final int DEFAULT_N_ARY = 3;

    public Agent pop[]; /* The population is declared as an array of agents */
    int agentType;

    public SolutionStructure bestSolution;


    /* -------------------------------- Construtor -----------------------------*/
    public Population(Instance inst, int size, int agentType, int solutionStructureType) throws Exception {
        // we use a ternary tree of four levels (thus, with 40 agents), if
        // the instance size exceeds 1000 cities.
        // this it totally instance and problem dependent and should not be here...
        popSize = size;
        nrParents = (int) Math.floor(popSize / n_ary);
        this.agentType = agentType;
        //Get the type of the agent to be used in this population
        switch (agentType) {
            case Agent.POKET_CURRENT:
                pop = new PocCurAgent[popSize];
                break;
            default:
                System.out.println("Invalid Agent Type");
                System.exit(1);
                break;
        }


        for (int i = 0; i < popSize; i++) {
            switch (agentType) {
                case Agent.POKET_CURRENT:
                    pop[i] = new PocCurAgent(inst.dimension, solutionStructureType);
//                    System.out.println("ind = " + i + ", pop[" + i + "] = " + pop[i].bestCost);
                    break;
                default:
                    throw new Exception("Invalid Agent Type");
            }
        }
    }

    /* ------------------------------------ EvaluatePop ------------------------------------*/
// This should be done differently, we should pass a Population index
// value of the population we would like to evaluate. Evaluating the
// population means assining a guiding function value to each agent
// in the population.

    // For NP problems, the guiding function is related with the objective
// function and is computable in polynomial time. For other type of
// problems, this may involve calling some other appropiate measure of
// quality. For instance, it may be the result of iteratively playing
// some sort of game (like when the task is to evolve winning strategies).
    
    public void evaluatePop_individually(Instance inst, int agent_id) {
        double bestCost = Long.MAX_VALUE;

//        for (int ind = 0; ind < popSize; ind++) {
            pop[agent_id].calculateCost(inst);

            if (pop[agent_id].bestCost < bestCost) {
                bestCost = pop[agent_id].bestCost;
                bestAgent = pop[agent_id];
            }
//        }
        newBestSol++;

        if (bestSolution == null || bestAgent.bestCost < bestSolution.cost) {
            switch (agentType) {
                case Agent.POKET_CURRENT:
                    bestSolution = ((PocCurAgent)bestAgent).pocket.deepCopy();
                    break;
                default:
                    System.out.println("Invalid Agent Type");
                    System.exit(1);
                    break;
            }
        }
    }
    
    public void evaluatePop(Instance inst) {
        double bestCost = Long.MAX_VALUE;

        for (int ind = 0; ind < popSize; ind++) {
            pop[ind].calculateCost(inst);

            if (pop[ind].bestCost < bestCost) {
                bestCost = pop[ind].bestCost;
                bestAgent = pop[ind];
            }
        }
        newBestSol++;

        if (bestSolution == null || bestAgent.bestCost < bestSolution.cost) {
            switch (agentType) {
                case Agent.POKET_CURRENT:
                    bestSolution = ((PocCurAgent)bestAgent).pocket.deepCopy();
                    break;
                default:
                    System.out.println("Invalid Agent Type");
                    System.exit(1);
                    break;
            }
        }
    }


    /* ------------------------------------ OrderChildren ------------------------------------*/
    public void orderChildren() {
        int i, j, firstCh, lastCh, parent;

        for (parent = nrParents - 1; parent >= 0; parent--) {
            firstCh = n_ary * parent + 1;
            lastCh = n_ary * parent + n_ary;
            for (i = firstCh; i < lastCh; i++) {
                for (j = (i + 1); j <= lastCh; j++) {
                    if (pop[i].cost > pop[j].cost) {
                        pop[i].exchangeSolutionStructures(pop[j]);
                    }
                }
            }
        }
    }


    /* ------------------------------------ IsNewPocket ------------------------------------*/
//public boolean isNewPocket (long cost) //?  isNewSolutionStructure already garantees that
//{                                      //   there will be no current equals to a pocket
//  for (int i=0; i < popSize; i++)      //since it is called before any time there is
//     if (cost == pop[i].pocket.cost)   // a new current insertSolutionStructure into an agent
//        return(false);
//
//  return(true);
//}


    /* ------------------------------------ IsNew ------------------------------------*/
    public boolean testValues(int values[][]) {
        for (int i = 0; i < popSize; i++) if (pop[i].testValues(values)) return true;
        return false;
    }


    /* ------------------------------------ IsNew ------------------------------------*/
    public boolean isNewSolutionStructure(double cost) {

        for (int i = 0; i < popSize; i++)
            if (!pop[i].isNewSolutionStructure(cost))
                return (false);

        return (true);
    }

    /* ------------------------------------ PocketProp ------------------------------------*/
    public void agentPropagation()
// PocketProp should be a problem independent component that
// propagates the best solutions up the tree.
    {
        // this should be changed for the MemePool, for instance here we
        // should have something like
        // Solution BestSolution=null
        // not something referencing `SolutionStructure'...
        Agent auxAgent = null;

        double minCost;
        int firstChild, minChild = 0;

        // we should avoid having these formulas to compute who are the
        // child nodes of the tree. The topology should be computed once
        // at the beginning and the PocketProp component should be general
        // for all hierarchical (directed acyclic graph) population strucutres

       
        for (int parent = nrParents - 1; parent >= 0; parent--) { 

            firstChild = n_ary * parent + 1;
            minCost = Double.MAX_VALUE;

//            System.err.print( "Parent = "+ pop[parent].cost + " ----> ");
            for (int i = firstChild; i < firstChild + n_ary; i++) {
//                System.err.print("pop[" + i + "] = " + pop[i].cost + ", ");
                if (pop[i].cost < minCost) {
                    minCost = pop[i].cost;
                    auxAgent = pop[i];
                    minChild = i;
                }
            }
//            System.err.print("minCost = " + minCost + ", pop[" + parent + "].cost = " + pop[parent].cost + ", ");
            if (minCost < pop[parent].cost) {
//                System.err.println("Exchanging pop[" + parent + "] = " + pop[parent].cost + " with " + "pop[" + minChild + "] = " + 
//                        pop[minChild].cost + ", ");
                pop[parent].exchangeSolutionStructures(pop[minChild]);
//                System.err.println("After Exchanging pop[" + parent + "] = " + pop[parent].cost + " with " + "pop[" + minChild + "] = " + 
//                        pop[minChild].cost + ", ");
                
            }
//            System.err.println();
//            System.err.println( ", pop[" + parent + "].cost = "+ pop[parent].cost + ", ");
        } // for (int parent = nrParents - 1; parent >= 0; parent--) {
//        System.err.println();
        //Update population best Agent
        bestAgent = pop[0];

        if (bestAgent.bestCost < bestSolution.cost) {
            switch (agentType) {
                case Agent.POKET_CURRENT:
                    bestSolution = ((PocCurAgent)bestAgent).pocket.deepCopy();
                    break;
                default:
                    System.out.println("Invalid Agent Type");
                    System.exit(1);
                    break;
            }
        }
    }

    
    public void pocket_agent_Propagation()
// PocketProp should be a problem independent component that
// propagates the best solutions up the tree.
    {

        
//        System.out.println("pop[0, 1, 2, 3] cost = " + pop[0].cost + ", " + pop[1].cost + ", " + pop[2].cost + ", " + pop[3].cost);
        if(pop[0].cost>pop[1].cost){
//            System.out.print("Exchanged pop[0] with pop[1], Old values, pop[0] = " + pop[0].cost + ", pop[1] = " + pop[1].cost);
            pop[0].exchangeSolutionStructures(pop[1]);
//            System.out.println(", New values, pop[0] = " + pop[0].cost + ", pop[1] = " + pop[1].cost);
        }
        if(pop[0].cost>pop[2].cost){
//            System.out.print("Exchanged pop[0] with pop[2], Old values, pop[0] = " + pop[0].cost + ", pop[2] = " + pop[2].cost);
            pop[0].exchangeSolutionStructures(pop[2]);
//            System.out.println(", New values, pop[0] = " + pop[0].cost + ", pop[2] = " + pop[2].cost);
        }
        if(pop[0].cost>pop[3].cost){
//            System.out.print("Exchanged pop[0] with pop[3], Old values, pop[0] = " + pop[0].cost + ", pop[3] = " + pop[3].cost);
            pop[0].exchangeSolutionStructures(pop[3]);
//            System.out.println(", New values, pop[0] = " + pop[0].cost + ", pop[3] = " + pop[3].cost);
        }
        
//        System.out.println("pop[1, 4, 5, 6] cost = " + pop[1].cost + ", " + pop[4].cost + ", " + pop[5].cost + ", " + pop[6].cost);
        if(pop[1].cost>pop[4].cost){
//            System.out.print("Exchanged pop[1] with pop[4], Old values, pop[1] = " + pop[1].cost + ", pop[4] = " + pop[4].cost);
            pop[1].exchangeSolutionStructures(pop[4]);
//            System.out.println(", New values, pop[1] = " + pop[1].cost + ", pop[4] = " + pop[4].cost);
        }
        if(pop[1].cost>pop[5].cost){
//            System.out.print("Exchanged pop[1] with pop[5], Old values, pop[1] = " + pop[1].cost + ", pop[5] = " + pop[5].cost);
            pop[1].exchangeSolutionStructures(pop[5]);
//            System.out.println(", New values, pop[1] = " + pop[1].cost + ", pop[5] = " + pop[5].cost);
        }
        if(pop[1].cost>pop[6].cost){
//            System.out.print("Exchanged pop[1] with pop[6], Old values, pop[1] = " + pop[1].cost + ", pop[6] = " + pop[6].cost);
            pop[1].exchangeSolutionStructures(pop[6]);
//            System.out.println(", New values, pop[1] = " + pop[1].cost + ", pop[6] = " + pop[6].cost);
        }
        
//        System.out.println("pop[2, 7, 8, 9] cost = " + pop[2].cost + ", " + pop[7].cost + ", " + pop[8].cost + ", " + pop[9].cost);
        if(pop[2].cost>pop[7].cost){
//            System.out.print("Exchanged pop[2] with pop[7], Old values, pop[2] = " + pop[2].cost + ", pop[7] = " + pop[7].cost);
            pop[2].exchangeSolutionStructures(pop[7]);
//            System.out.println(", New values, pop[2] = " + pop[2].cost + ", pop[7] = " + pop[7].cost);
        }
        if(pop[2].cost>pop[8].cost){
//            System.out.print("Exchanged pop[2] with pop[8], Old values, pop[2] = " + pop[2].cost + ", pop[8] = " + pop[8].cost);
            pop[2].exchangeSolutionStructures(pop[8]);
//            System.out.println(", New values, pop[2] = " + pop[2].cost + ", pop[8] = " + pop[8].cost);
        }
        if(pop[2].cost>pop[9].cost){
//            System.out.print("Exchanged pop[2] with pop[9], Old values, pop[2] = " + pop[2].cost + ", pop[9] = " + pop[9].cost);
            pop[2].exchangeSolutionStructures(pop[9]);
//            System.out.println(", New values, pop[2] = " + pop[2].cost + ", pop[9] = " + pop[9].cost);
        }
        
//        System.out.println("pop[3, 10, 11, 12] cost = " + pop[3].cost + ", " + pop[10].cost + ", " + pop[11].cost + ", " + pop[12].cost);
        if(pop[3].cost>pop[10].cost){
//            System.out.print("Exchanged pop[3] with pop[10], Old values, pop[3] = " + pop[3].cost + ", pop[10] = " + pop[10].cost);
            pop[3].exchangeSolutionStructures(pop[10]);
//            System.out.println(", New values, pop[3] = " + pop[3].cost + ", pop[10] = " + pop[10].cost);
        }
        if(pop[3].cost>pop[11].cost){
//            System.out.print("Exchanged pop[3] with pop[11], Old values, pop[3] = " + pop[3].cost + ", pop[11] = " + pop[11].cost);
            pop[3].exchangeSolutionStructures(pop[11]);
//            System.out.println(", New values, pop[3] = " + pop[3].cost + ", pop[11] = " + pop[11].cost);
        }
        if(pop[3].cost>pop[12].cost){
//            System.out.print("Exchanged pop[3] with pop[12], Old values, pop[3] = " + pop[3].cost + ", pop[12] = " + pop[12].cost);
            pop[3].exchangeSolutionStructures(pop[12]);
//            System.out.println(", New values, pop[3] = " + pop[3].cost + ", pop[12] = " + pop[12].cost);
        }
        
//        for(int q=0;q<pop.length;q++){
//            PocCurAgent testing = (PocCurAgent) pop[q];
//            testing[q] = 5;
//        }

        //Update population best Agent
        bestAgent = pop[0];

        if (bestAgent.bestCost < bestSolution.cost) {
            switch (agentType) {
                case Agent.POKET_CURRENT:
                    bestSolution = ((PocCurAgent)bestAgent).pocket.deepCopy();
                    
                    break;
                default:
                    System.out.println("Invalid Agent Type");
                    System.exit(1);
                    break;
            }
        }
    }
    
    public void updateAgents_individually(Instance inst, int agent_id) {
            pop[agent_id].updateAgent(inst);
    }
    
    /* ------------------------------------ atualiza_pockets -----------------*/
    public void updateAgents(Instance inst) {
        int i;

        for (i = 0; i < popSize; i++) {
            pop[i].updateAgent(inst);
        }
    }

    /* ------------------------------------ Parent ------------------------------------*/
    private int[] getChildren(int x) {
        int i, child[] = new int[n_ary];

        for (i = 0; i < n_ary; i++)
            child[i] = x * n_ary + (i + 1);

        return (child);
    }


    /* ------------------------------------ Parent ------------------------------------*/
    private int getParent(int x) {
        float parent = x / n_ary - 1;

        if (parent != (int) parent)
            return ((int) parent + 1);
        else
            return ((int) parent);
    }

}
