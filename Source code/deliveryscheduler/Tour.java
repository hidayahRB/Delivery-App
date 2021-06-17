package deliveryscheduler;

import java.util.ArrayList;

public class Tour {
    
    private double tourCost;
    private ArrayList<Route> routeList;
    
    public Tour() {
        routeList = new ArrayList<>();
    }
    
    public void updateTourCost(){
        tourCost += getLastRouteInTour().getRouteCost();
    }
    
    public void addRouteToTour(Route route){
        routeList.add(route);
    }
    
    @Override
    public String toString(){
        String str = "Tour\n" + "Tour Cost : " + tourCost + "\n";
        int routeCounter = 1;

        for(Route route : routeList){
            str += "\nRoute " + routeCounter + "\n";
            
            ArrayList<Stop> stopList = route.getStopList();
            
            for (int i = 0; i < stopList.size(); i++) {
                if(i == stopList.size()-1) str += stopList.get(i).getID() + "\n";
                
                else{
                    str += stopList.get(i).getID() + " --> ";
                }
            }
            
            routeCounter++;
            
            str += "Capacity: " + route.getCapacity() + "\n";
            str += "Route cost: " + route.getRouteCost() + "\n";
        }
        
        return str;
    }
    
    //Getter and Setter method
    
    public double getTourCost() {
        return tourCost;
    }

    public void setTourCost(double tourCost) {
        this.tourCost = tourCost;
    }
    
    public Route getLastRouteInTour(){
        return routeList.get(routeList.size()-1);
    }
    
    public ArrayList<Route> getRouteList() {
        return routeList;
    }

    
}
