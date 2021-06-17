package deliveryscheduler;

public class Depot extends Stop{
    //id for depot always '0', and only one depot for entire program
    public Depot(int x, int y, int capacity){
        super(0, x , y, capacity);
        visited = true;
    }
}
