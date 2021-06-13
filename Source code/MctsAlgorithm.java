package deliveryscheduler;

import static deliveryscheduler.DeliveryScheduler.output;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//hati2, algo run dengan betul bila program jalan sekali harung je, kalau ulang2, kena edit static tuu
public class MctsAlgorithm {
    
    private final int ALPHA = 1;
    
    private double[][][] policy;
    private double[][] globalPolicy;
    private final List<Stop> nodeList; //edit jadi final
    
    private final int riderCapacity;
    private final Depot depot; //edit new
    
    //variables responsible to terminate the algorithm when time's up
    public static final long startTime = System.nanoTime();
    private final long timeLimit; //in nano second for sake of simplicity
    
    public MctsAlgorithm(int riderCapacity, int level, int iteration, int numOfNode, List<Stop> nodeList, int timeLimitInSec) {
        this.timeLimit = timeLimitInSec * 1000000000L; //convert second to nano second
        startTimer(timeLimitInSec);
        
        policy = new double[level][numOfNode][numOfNode]; //node = no. of customer + depot
        globalPolicy = new double[numOfNode][numOfNode];
        this.nodeList = nodeList; //integrate nodeList dgn Depot object

        depot = (Depot) nodeList.get(0); //edit new
        
        this.riderCapacity = riderCapacity;
    }
    
    /**
    * Input Parameters
    * level: int, refer README.md for more info
    * iterations: int, refer README.md for more info
    * @param [level]
    * @return a_tour
    * a_tour: class object, convey information about tour cost, all routes and etc. (depend on you)
    */
    public Tour search(int level, int iteration){
        Tour best_tour = new Tour();
        best_tour.setTourCost((double) Integer.MAX_VALUE); //set high value untuk mudahkan comparison nnti
        
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
    * a_tour: class object, convey information about tour cost, all routes and etc. (depend on you)
    * level: int, refer README.md for more info
    * Return Type
    * No object returned (void)
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
    
    /**
    * There is no parameters (input) nor object returned
    */
    private Tour rollout () {
        
        Tour new_tour = new Tour(); //with first route with first stop at 0, every route must start and end at depot (ID=0)
        insertNewRoute(new_tour);
        
        int currentRouteCapacity = 0; //get track of capacity value for every stop inserted to route
        
        while(true){
            //Do operation using last route and last stop
            Stop currentStop = getLastStop(new_tour.getLastRouteInTour());
            
            //if no available possible next stop for currentStop
            if(isAllPossibleNextStopChecked()){
                
                    //current route is completed and should return to depot
                    backToDepot(new_tour.getLastRouteInTour());
                    new_tour.getLastRouteInTour().setCapacity(currentRouteCapacity);
                    new_tour.updateTourCost(); //sum current route cost to tour cost
                    
                    if(isAllStopVisited())
                        break;  // rollout process is done

                    //add new route into new_tour  // else add new vehicle, again start at depot with ID 0
                    insertNewRoute(new_tour); //kena ada rider class
                    currentRouteCapacity = 0;

                    //reset check status for unvisited-stop for next route
                    resetCheckStatus();
                    
                    continue;  // skip to next loop to continue search a route for new vehicle               
            }

            //find every possible successors that is not yet checked for currentStop
            
            //get possible move(s) for this stop (unvisited-stop && not-checked-stop && beside currentStop)
            ArrayList<Stop> possible_move = findPossibleSuccessor(currentStop, nodeList);

            //retrieve possible next stop
            Stop nextStop = select_next_move(currentStop, possible_move);
            
            //if add nextStop to currentRoute does not violate any rules //kena ada rider class
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
    * currentStop: depend, it can be an int or node class as long as it convey information about what is your current node 
    * possible_successors: depend, it can be a list of int or list of node class objects as long as it convey information about what is the possible move from your current node
    * [More explanations] let say I want search from A to B or C or D, then my currentStop is A, and if B is searched/checked and it is impossible to be next node of A anymore,
    * then my possible_successors are C and D only. The 1d probability array will then be an double array of size 2 (2 possible nodes C and D).
    * Return Type
    * selected_successor: depend, it can be an int or node class as long as it convey information about what is the node selected to move next from current node
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
        
        //add depot at the front of stopList, nodeList[0] is always depot
        stopList.add(depot);
        
        //set visited status for depot to true, to avoid bug
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
                synchronized(DeliveryScheduler.output){
                
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
