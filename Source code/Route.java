import java.util.ArrayList;

public class Route {
    
    //store customer list for 1 trip/route, start and end with Depot
    private ArrayList<Stop> stopList;
    
    private double routeCost;
    private int capacity;

    public Route() {
        stopList = new ArrayList<>();
    }
    
    public void addStopToRoute(Stop newStop){
        
        //every time add stop to route, update cost immediately
        routeCost += getLastStop().calculateCost(newStop.x, newStop.y);
        stopList.add(newStop);
    }
    
    @Override
    public String toString(){
        String str = "";
        
        for (int i = 0; i < stopList.size(); i++) {
            if(i == 0) str += "" + stopList.get(i).getID();
            
            else{
                str += " -> " + stopList.get(i).getID();
            }
        }
        
        str += "\nCapacity: " + capacity + "\nRoute cost: " + routeCost + "\n\n";
        
        return str;
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
