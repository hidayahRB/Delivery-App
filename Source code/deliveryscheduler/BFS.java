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
        queue.add(depot);
        depot.setVisited(true);
        
        Route newRoute = null;
        
        boolean isAnyNodeQualified = false;
        int currentUsedCapacity = 0;
        
        int routeCounter = 0;
        
        while ( !isAllNodeVisited() ) {
 
            Node element = queue.remove();
            if(element.getID() == 0) newRoute = initiateNewRoute();
            else newRoute.addStopToRoute(element.data);
            
            List<Node> neighbours = element.getNeighbours();
            
            while(!isAllNeighbourChecked(neighbours)){
                
                Node nextCustomer = getNearestNextCustomer(element, neighbours);
                
                if(isNodeQualifiedToBeAdded(nextCustomer, currentUsedCapacity)){

                    currentUsedCapacity += nextCustomer.getCapacity();

                    queue.add(nextCustomer); //update queue for BFS
                    
                    nextCustomer.setVisited(true);
                    isAnyNodeQualified = true;
                    
                    break;
                }
                else{
                    nextCustomer.setChecked(true);
                }
            }
            
            //Route capacity already at maximum or all node already visited
            if(isAnyNodeQualified == false){
                backToDepot(newRoute);
                newRoute.setCapacity(currentUsedCapacity);
                routeCounter++;

                routeOutput += "Route " + routeCounter + "\n" + newRoute;

                tourCost += newRoute.getRouteCost();
                
                //search from depot (id 0) again, new route
                queue.add(depot);
                currentUsedCapacity = 0;
            }
            
            resetCheckStatusForUnvisitedNode(neighbours);
            isAnyNodeQualified = false;
        }
        
        //if loop end before adding the last 'nextCustomer' to route (should be at line 103)
        if(!queue.isEmpty()){
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
        System.out.print(routeOutput);

    }
    
    private void backToDepot(Route currentRouteSearched){
        currentRouteSearched.addStopToRoute(firstNode.data);
    }
    
    private Route initiateNewRoute(){
        Route newRoute = new Route();
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

