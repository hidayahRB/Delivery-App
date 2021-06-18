package deliveryscheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BFS {
    
    private Queue<Node> queue;
    private Node firstNode;
    
    private double tourCost = 0;
    private final int riderCapacity;

    private String routeOutput = "";
    
    static class Node
    {
        Stop data;
        List<Node> neighbours;
        
        Node(Stop stop){ //stop means either Depot/Customer
            data = stop;
            neighbours = new ArrayList<>();
        }
        
        public void addneighbours(Node neighbourNode){
            this.neighbours.add(neighbourNode);
        }
        
        public List<Node> getNeighbours() {
            return neighbours;
        }
        
        public void setNeighbours(List<Node> neighbours) {
            this.neighbours = neighbours;
        }
        
        public double calculateDistance(int nextCustomerX, int nextCustomerY){
            return data.calculateCost(nextCustomerX, nextCustomerY);
        }
        
        //Getter and Setter method (extract from Stop class)
        public int getX(){
            return data.x;
        }
        
        public int getY(){
            return data.y;
        }
        
        public int getID(){
            return data.getID();
        }
        
        public int getCapacity(){
            return data.getCapacity();
        }
        
        public boolean isChecked(){
            return data.isChecked();
        }
        
        public boolean isVisited(){
            return data.isVisited();
        }
        
        public void setChecked(boolean checked) {
            data.setChecked(checked);
        }

        public void setVisited(boolean visited) {
            data.setVisited(visited);
        }
        
    }  
    
    // Constructor
    public BFS(Node node, int riderCapacity){
        queue = new LinkedList<>();
        firstNode = node;
        this.riderCapacity = riderCapacity;
        
        bfs(node);
    }
    
    private void bfs(Node depot)
    {
        // We implement queue in this method
        queue.add(depot);
        // we set depot node as visited
        depot.setVisited(true);
        // initialize the route is null
        Route newRoute = null;
        // initialize that there are no nodes qualified yet
        boolean isAnyNodeQualified = false;
        // initialize the current used capacity of rider is still 0
        int currentUsedCapacity = 0;
        // and lastly there are no best route suggested yet 
        int routeCounter = 0;
        
        // As the program does not visited all the group yet 
        while ( !isAllNodeVisited() ) {
            // we assign node element to call an element from the queue
            Node element = queue.remove();
            
            if(element.getID() == 0) {
                // if the ID is 0 = depot 
                // we initiate new route
                newRoute = initiateNewRoute();
            }else{
                // if its not the depot
                // straight to add stop in the route
                newRoute.addStopToRoute(element.data);
            }
            // assign Node neighbour and get neighbour from node element 
            List<Node> neighbours = element.getNeighbours();
            // as the program does not yet check all the neighbours from the first stop 
            while(!isAllNeighbourChecked(neighbours)){
                // the program get the nearest customer from depot by performing a certain calculation 
                // go to getNearestNextCustomer() method
                Node nextCustomer = getNearestNextCustomer(element, neighbours);
                
                if(isNodeQualifiedToBeAdded(nextCustomer, currentUsedCapacity)){ 
                    // How we check whether the customer is qualified or not to be added?
                    // We check whether the total of currentUsedCapacity + nextNodeForCheck.getCapacity()) <= riderCapacity
                    // go to the isNodeQualifiedToBeAdded() method
                    // so if it's TRUE if statement is ran
                    
                    // So in this statement the program update the currentUsedCapasity,
                    currentUsedCapacity += nextCustomer.getCapacity();
                    // add the nextCustomer in the queue,
                    queue.add(nextCustomer); //update queue for BFS
                    // lastly set this nextCustomer as visited
                    nextCustomer.setVisited(true);
                    isAnyNodeQualified = true;
                    // after this it will break this current while loop
                    break;
                }
                else{
                    // if the if statement is false, then proceed to check the next customer
                    nextCustomer.setChecked(true);
                }
            }
            
            // When the route capacity already at maximum or all node were already visited
            // the program should run this if statement only when isAnyNodeQualified = false when the capacity of rider already exceed maximum capacity
            if(isAnyNodeQualified == false){
                // the route will return back to depot and generate new route
                backToDepot(newRoute);
                newRoute.setCapacity(currentUsedCapacity);
                routeCounter++;

                routeOutput += "Route " + routeCounter + "\n" + newRoute;

                tourCost += newRoute.getRouteCost();
                
                //search from depot (id 0) again, new route
                queue.add(depot);
                currentUsedCapacity = 0;
            }
            // the program will reset this method and variable
            resetCheckStatusForUnvisitedNode(neighbours);
            isAnyNodeQualified = false;
        }
        
        //if while loop isAllNeighbourChecked() end before adding the last 'nextCustomer' to route (should be at line 103)
        if(!queue.isEmpty()){
            // the program will just add the LAST customer to the route
            Node element = queue.remove();
            newRoute.addStopToRoute(element.data);
        }
        
        //when reaching here, the current route processed still not complete (no back to Depot), so here we add Depot as last Node
        backToDepot(newRoute);
        newRoute.setCapacity(currentUsedCapacity);
        
        routeCounter++;

        routeOutput += "Route " + routeCounter + "\n" + newRoute;
        
        tourCost += newRoute.getRouteCost();

        
        System.out.println("Tour cost : " + tourCost + "\n");
        // program print out the route output
        System.out.print(routeOutput);

    }
    
    // method to go back to depot 
    private void backToDepot(Route currentRouteSearched){
        // add depot as the next stop after the last customer in each generated route 
        currentRouteSearched.addStopToRoute(firstNode.data);
    }
    
    // method to initiate a new route
    private Route initiateNewRoute(){
        Route newRoute = new Route();
        // the route must always starts from depot location
        newRoute.getStopList().add( (Depot) firstNode.data);
        
        return newRoute;
    }
    
    private boolean isAllNeighbourChecked(List<Node> neighbourList){
        
        for(Node node : neighbourList){
            if(node.isVisited()) continue;
            
            if(!node.isChecked()) return false;
        }
        
        return true;
    }
    
    private void resetCheckStatusForUnvisitedNode(List<Node> neighbourList){
        for(Node node : neighbourList){
            if(node.isVisited()) continue;
            
            node.setChecked(false);
        }
    }
    
    private boolean isAllNodeVisited(){
        
        for(Node node : firstNode.getNeighbours()){
            if(!node.isVisited()) return false;
        }
        
        return true;
    }
    
    //as search always start from depot (id 0), next node must be Customer, hence the method name: getNearestNextCustomer
    private Node getNearestNextCustomer(Node currentNodeForCheck, List<Node> neighbourList){
        Node nearestNextCustomer = null;
        double dummyForComparison = Integer.MAX_VALUE;
        
        for(Node node : neighbourList){
            if(node.isVisited() || node.isChecked()) continue;
            
            double distanceToNextCustomer = currentNodeForCheck.calculateDistance(node.getX(), node.getY());
            if(distanceToNextCustomer < dummyForComparison){
                dummyForComparison = distanceToNextCustomer;
                nearestNextCustomer = node;
            }
        }
        
        return nearestNextCustomer;
    }
    
    private boolean isNodeQualifiedToBeAdded(Node nextNodeForCheck, int currentUsedCapacity){
        return (currentUsedCapacity + nextNodeForCheck.getCapacity()) <= riderCapacity;
    }

}

