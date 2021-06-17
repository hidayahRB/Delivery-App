package deliveryscheduler;

import static deliveryscheduler.MainPage.output;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Class used to run MCTS Algorithm by invoking search(level, iterations) method to get best tour
public class MctsAlgorithm {
    
    private final int ALPHA = 1;
    
    private double[][][] policy;
    private double[][] globalPolicy;
    private final List<Stop> nodeList;
    
    private final int riderCapacity;
    private final Depot depot;
    
    //variables responsible to terminate the algorithm when time's up
    public static final long startTime = System.nanoTime();
    private final long timeLimit; //in nano second for sake of simplicity
    
    public MctsAlgorithm(int riderCapacity, int level, int iteration, int numOfNode, List<Stop> nodeList, int timeLimitInSec) {
        this.timeLimit = timeLimitInSec * 1000000000L; //convert second to nano second
        startTimer(timeLimitInSec);
        
        policy = new double[level][numOfNode][numOfNode]; //node = no. of customer + depot
        globalPolicy = new double[numOfNode][numOfNode];
        this.nodeList = nodeList; //nodeList consist of Depot and all Customer objects

        depot = (Depot) nodeList.get(0);
        
        this.riderCapacity = riderCapacity;
    }
    
    /**
    * Input Parameters
    * level: int
    * iterations: int
    * @param [level]
    * @return a_tour
    */
    public Tour search(int level, int iteration){
        Tour best_tour = new Tour();
        best_tour.setTourCost((double) Integer.MAX_VALUE); //set high value to ease comparison later
        
        if( (level-1) == 0){

            //when running time is more than timeLimit, stop searching for best tour and return current best_tour
            if((System.nanoTime() - startTime) > timeLimit){
                return getDummyTour();
            }
            
            return rollout();
        }
        else{
            
            //when running time is more than timeLimit, stop searching for best tour and return current best_tour
            if((System.nanoTime() - startTime) > timeLimit){
                return getDummyTour();
            }
            
            policy[level-1] = globalPolicy;
            
            for (int i = 0; i < iteration; i++) {

                Tour new_tour = search(level - 1, i);
                
                if(new_tour.getTourCost() < best_tour.getTourCost()){
                    best_tour = new_tour;
                    adapt(best_tour, level);
                }

            }
            
            globalPolicy = policy[level-1];
        }
        return best_tour;
    }
    
    /**
    * Input Parameters
    * a_tour: Tour object that contain information about tour cost and all routes
    * level: int
    */
    private void adapt(Tour a_tour, int level) {
        
        for (Route route : a_tour.getRouteList()){
            
            ArrayList<Stop> stopList = route.getStopList();
            
            for (int i = 0; i < stopList.size() - 1; i++) {
                Stop stop = stopList.get(i);
                Stop nextStop = stopList.get(i + 1);

                policy[level - 1][stop.getID()][nextStop.getID()] += ALPHA;
                double z = 0.0;
                
                //find possible move from current stop (get not-visited-yet-stop(s))
                ArrayList<Stop> possibleMove = findPossibleMove(stop, nodeList);
                
                //for every possible move that can be made by stop (stop besides the stop itself or visited stop)
                for(int j = 0; i < possibleMove.size(); i++){
                    z += Math.exp(globalPolicy[stop.getID()][possibleMove.get(i).getID()]);
                }
                
                //for every possible move that can be made by stop
                for(int j = 0; i < possibleMove.size(); i++){
                    int stopID = stop.getID();
                    int possibleMoveID = possibleMove.get(i).getID();

                    policy[level - 1][stopID][possibleMoveID] -= ALPHA * (Math.exp(globalPolicy[stopID][possibleMoveID]) / z);
                }
                
                stop.setVisited(true);
            }
            
            resetStopStatus();
        }   
    }
    
    //Method used to generate new tour
    private Tour rollout () {
        
        Tour new_tour = new Tour(); //with first route with first stop at depot (ID=0), every route must start and end at depot (ID=0)
        insertNewRoute(new_tour);
        
        int currentRouteCapacity = 0; //get track of capacity value for every stop inserted to route
        
        while(true){
            //Do operation using last route and last stop
            Stop currentStop = getLastStop(new_tour.getLastRouteInTour());
            
            //check if no available possible next stop for currentStop
            if(isAllPossibleNextStopChecked()){
                
                    //current route is completed and should return to depot
                    backToDepot(new_tour.getLastRouteInTour());
                    new_tour.getLastRouteInTour().setCapacity(currentRouteCapacity);
                    new_tour.updateTourCost(); //sum current route cost to tour cost
                    
                    if(isAllStopVisited())
                        break;  // rollout process is done

                    //add new route into new_tour, again start at depot with ID 0
                    insertNewRoute(new_tour);
                    currentRouteCapacity = 0;

                    //reset check status for unvisited-stop to be used for next route search
                    resetCheckStatus();
                    
                    continue;  // skip to next loop to continue search a route for new vehicle               
            }
            
            //get possible move(s) for this stop (unvisited-stop && not-checked-stop && not same with currentStop)
            ArrayList<Stop> possible_move = findPossibleSuccessor(currentStop, nodeList);

            //retrieve possible next stop
            Stop nextStop = select_next_move(currentStop, possible_move);
            
            //if add nextStop to currentRoute does not exceed capacity limit of rider
            if(currentRouteCapacity + nextStop.getCapacity() <= riderCapacity){
                
                //add nextStop to current route
                new_tour.getLastRouteInTour().addStopToRoute(nextStop);

                //set nextStop as visited
                nextStop.setVisited(true);
                
                //update capacity used for current route/rider
                currentRouteCapacity += nextStop.getCapacity();
                new_tour.getLastRouteInTour().setCapacity(currentRouteCapacity);
                
                if(isAllStopVisited()){
                    backToDepot(new_tour.getLastRouteInTour());
                    new_tour.updateTourCost(); //sum current route cost to tour cost
                    break;
                }
                
                if(nextStop instanceof Depot){
                    new_tour.updateTourCost();
                    insertNewRoute(new_tour);
                    currentRouteCapacity = 0;
                }

                resetCheckStatus();
                
            }
            else{
                nextStop.setChecked(true);
            }
        }
                        
        resetStopStatus();
        return new_tour;
    }

    /**
    * Input Parameters
    * currentStop: Stop object that convey information about what is the current node (either Depot or Customer) 
    * possible_successors: List object that convey information about what is the possible move from your current node
    * [More explanations] Let say we want search from A to B or C or D, then our currentStop is A, and if B is searched/checked and it is impossible to be next node of A anymore,
    * then my possible_successors are C and D only. The 1d probability array will then be an double array of size 2 (2 possible nodes C and D).
    * Return Type
    * selected_successor: Stop object that convey information about what is the node selected to move after current node
    */
    private Stop select_next_move(Stop currentStop, List<Stop> possible_successors) {
        //initialize 1d probability array that have same size with possible_successors
        double[] probability = new double[possible_successors.size()];

        double sum = 0;
                
        for (int i = 0; i < possible_successors.size(); i++) {
            probability[i] = Math.exp(globalPolicy[currentStop.getID()][possible_successors.get(i).getID()]);
            sum += probability[i];
        }

        double mrand = new Random().nextDouble() * sum;
        
        int i = 0;
        sum = probability[0];
        
        while (sum < mrand)
            sum += probability[++i];
        
        return possible_successors.get(i);
    }
    
    //Helper method
    
    private boolean isAllStopVisited(){
        
        for(Stop stop : nodeList){
            if(stop.isVisited() == false) return false;
        }
        
        return true;
    }
    
    //check whether there are possible next stop for currentStop (refer rollout())
    private boolean isAllPossibleNextStopChecked(){
        
        for(Stop stop : nodeList){
            if(stop.isVisited()) continue;
            if(!stop.isChecked()) return false;
        }
        
        return true;
    }
    
    private Stop getLastStop(Route currentRouteChecked){
        return currentRouteChecked.getLastStop();
    }
    
    private void backToDepot(Route currentRoute){
        currentRoute.addStopToRoute(depot);
    }
    
    private void insertNewRoute(Tour tour){
        ArrayList<Stop> stopList = new ArrayList<>();
        
        //add depot at the front of stopList
        stopList.add(depot);
        
        //set visited status for depot to true
        depot.setVisited(true);
        
        Route newRoute = new Route();
        newRoute.setStopList(stopList);
        
        tour.addRouteToTour(newRoute);
    }
    
    //Get possible next move for currentNodeForCheck, return list of node which is not visited by current route
    private ArrayList<Stop> findPossibleMove(Stop currentNodeForCheck, List<Stop> nodeList){
        ArrayList<Stop> possible_next_move = new ArrayList<>();
        
        //special condition, handle the depot's visitedStatus that is always true
        if(!(currentNodeForCheck instanceof Depot)) possible_next_move.add(depot);
        
        for(Stop stop : nodeList){
            if(!stop.isVisited() && !stop.equals(currentNodeForCheck)) possible_next_move.add(stop);
        }
        
        return possible_next_move;
    }
    
    private ArrayList<Stop> findPossibleSuccessor(Stop currentNodeForCheck, List<Stop> nodeList){
        ArrayList<Stop> possible_next_move = new ArrayList<>();

        for(Stop stop : nodeList){

            if(stop.equals(currentNodeForCheck) || stop.isChecked()) continue;
            
            //Special condition, handle depot's visitedStatus always true
            if(stop instanceof Depot && !stop.isChecked()) possible_next_move.add(stop);
            
            else if(!stop.isVisited() && !stop.isChecked()) possible_next_move.add(stop);
        }

        return possible_next_move;
    }
    
    private void resetCheckStatus(){
        for(Stop stop : nodeList){
            if(!stop.isVisited())
                stop.setChecked(false);
        }
    }
    
    //reset status as all method share the same nodeList
    private void resetStopStatus(){
        for(Stop stop : nodeList){
            stop.setChecked(false);
            if(stop instanceof Depot) continue;
            stop.setVisited(false);
        }
    }
    
    //invoke this method if times up, dummyTour is return to satisfied the behaviour of search method (return type Tour and the comparison)
    private Tour getDummyTour(){
        Tour dummyTour = new Tour();
        dummyTour.setTourCost(Integer.MAX_VALUE);
        
        return dummyTour;
    }

    public static void startTimer(int timeLimitInSec){
        
        new Thread(){
            public void run(){
                int timeCounter = 0;
                
                //get output as expected
                synchronized(deliveryscheduler.MainPage.output){
                
                    output.print("Time taken : " + timeCounter++ + "s");
                    
                    while(!isTimesUp(timeLimitInSec)){
                            
                        try {
                            Thread.sleep(1000);
                        }catch (InterruptedException ex) {

                        }

                        output.print("\r" + "Time taken : " + timeCounter++ + "s");
                    }
                }
                        
            }
        }.start();
        
    }
    
    private static boolean isTimesUp(int timeLimitInSec){
        int runTime = (int) ((System.nanoTime() - startTime)/1000000000L);
        
        return runTime >= timeLimitInSec;
    }
    
}
