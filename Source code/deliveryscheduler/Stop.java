package deliveryscheduler;

public class Stop {
    
    protected final int ID;
    protected int x, y;
    protected int capacity;
    
    protected boolean checked, visited;
    
    public Stop(int id, int x, int y, int capacity) {
        ID = id;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
    }

    public double calculateCost(int secondX, int secondY){
        double shortest;
        int distanceX, distanceY;

        distanceX = Math.abs(this.x - secondX);
        distanceY = Math.abs(this.y - secondY); 
        
        shortest = Math.sqrt( (distanceX*distanceX) + (distanceY*distanceY) );

        return shortest;
    }
    
    //Getter and Setter method
    
    public int getCapacity() {
        return capacity;
    }

    public int getID() {
        return ID;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isVisited() {
        return visited;
    }
    
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    
}
