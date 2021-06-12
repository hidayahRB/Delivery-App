package deliveryscheduler;

import java.util.ArrayList;

public class Route {
    
    //store customer list for 1 trip/route, start and end with Depot
    private ArrayList<Stop> stopList;
    
    private double routeCost;
    private int capacity;

    public void addStopToRoute(Stop newStop){
        
        //every time add stop to route, update cost immediately
        routeCost += getLastStop().calculateCost(newStop.x, newStop.y);
        stopList.add(newStop);
    }
    
    //Getter and Setter method
    
    public void setCapacity(int capacity){
        this.capacity = capacity;
    }
    
    public int getCapacity(){
        return capacity;
    }
    
    public double getRouteCost(){
        return routeCost;
    }
    
    public Stop getLastStop(){
        return stopList.get(stopList.size()-1);
    }
    
    public ArrayList<Stop> getStopList() {
        return stopList;
    }
    
    public void setStopList(ArrayList<Stop> stopList) {
        this.stopList = stopList;
    }

}
