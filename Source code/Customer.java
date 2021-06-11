public class Customer extends Stop{
    
    //count id start from 1, increment for every Customer object we create
    private static int id = 1;

    public Customer(int x, int y, int capacity){
        super(id++, x , y, capacity);
    }

}
