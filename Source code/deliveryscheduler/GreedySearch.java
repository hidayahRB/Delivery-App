package deliveryscheduler;

import java.util.Scanner;

public class GreedySearch<E>{

    protected int originalC = 10;
    protected int C = 10;
    protected int[] custCapacity;
    protected int totalCust = 0;
    protected double closestCustomerDistance=0;
    protected int closestCustomer;
    protected int currentX;
    protected int currentY;
    protected int vehicle = 1;
    protected double vehicleCost = 0;
    protected double totalCost = 0;


    private java.util.LinkedList<Integer> custIDunderC = new java.util.LinkedList<>();
    private java.util.LinkedList<Customer> customers = new java.util.LinkedList<>();
    private java.util.LinkedList<Integer> customerID = new java.util.LinkedList<>();
    private java.util.LinkedList<Integer> route = new java.util.LinkedList<>();
    Depot localDepot = new Depot(0, 0, 0);          //testing local depot object because no main object of depot yet


    public void findCustUnderC(){                   //to list the customer that fits the rider capacity (not more than C) (always refreshed aft sending parcel)

        custIDunderC.clear();                       // to reset the list back to 0
        for(int i=0; i<customerID.size(); i++){
            if(customers.get(customerID.get(i)).capacity <= C){
                custIDunderC.addLast(customerID.get(i));
            }
        }
        //System.out.println(custIDunderC);           //print the customers under C


                //C will change after sending to houses, if rider(maxCap =10) go to house 2 (cap = 4), 
                //then rider will have c = 6 left n then we find cust with less than c=6

                //this is for each tour, but need to call the fx again to recalc but with new c
    }



    public int findNearestCust(){
        double currentCustomerDistance = 0;

        //findCustUnderC();                           //to find the customer going through that tour 


        if(custIDunderC.size() == 0){                       //go back to depot

     
            System.out.println("Cost: " + vehicleCost);
            System.out.println("");
            displayRoute();

            route.clear();

            localDepot.calculateCost(currentX, currentY);      //distance of last customer to depot
            C = originalC;                                  //reset capacity value
            currentX = localDepot.x;                        //reset location of rider back to depot
            currentY = localDepot.y;
            findCustUnderC();                               //find new customer for next tour
            System.out.println("Vehicle " + ++vehicle);
            vehicleCost = 0;       

        }
        else{
            System.out.println("Vehicle " + vehicle);
        }

        closestCustomerDistance = customers.get(custIDunderC.get(0)).calculateCost(currentX, currentY);
        closestCustomer = custIDunderC.get(0);


        for(int i=1; i<custIDunderC.size(); i++){

            currentCustomerDistance = customers.get(custIDunderC.get(i)).calculateCost(currentX, currentY);

            if(currentCustomerDistance<closestCustomerDistance){
                closestCustomerDistance = currentCustomerDistance;
                closestCustomer = custIDunderC.get(i);
            }
        }

        currentX = customers.get(closestCustomer).x;
        currentY = customers.get(closestCustomer).y;                    //location of rider

        vehicleCost = vehicleCost + closestCustomerDistance;
        totalCost = totalCost + closestCustomerDistance;

        System.out.println("closest customer distance : " + closestCustomerDistance);
        System.out.println("closest customer : " + (closestCustomer+1));

        return 0;
    }



    public void addCustomer(int x,int y,int capacity){      //add customer names into a list
        customers.addLast(new Customer(x, y, capacity));
        customerID.addLast(customers.size()-1);
    }



    public void updateCapacity(){                   //to know how many capacity left yg postman x hantar lg

        System.out.println("customers left: " + customerID.size());
        System.out.println("removing customer: " + (closestCustomer+1));
        System.out.println("");

       
        C = C - customers.get(closestCustomer).capacity;            //to update capacity of postman
        System.out.println("Capacity: "+ C);
        System.out.println("");
        
        removeCustomer(closestCustomer);                          //to remove customer yg dah dapat parcel from the list

    }

    public void removeCustomer(int closestCustomer){

        for(int i = 0; i<= customerID.size(); i++){
            if(customerID.get(i)==closestCustomer){
                route.addLast(closestCustomer+1);
                customerID.remove(i);
                break;
            }
        }

    }



    public int getNumberOfCustomers(){
        return customerID.size();
    }



    public double getTourCost(){
        return totalCost;
    }



    public double getVehicleCost(){
        return vehicleCost;
    }


    public void setDepot(int x,int y,int capacity){
        localDepot = new Depot(x, y, capacity);        
    }


    public void setCapacity(int c){
        this.C = c;
        this.originalC = c;
    }

    public void displayRoute(){
        route.addFirst(0);
        route.addLast(0);

        System.out.println("Route: ");

        for(int i = 0; i<route.size(); i++){
            System.out.print(route.get(i));
            if(i!=(route.size()-1)){
                System.out.print(" -> ");
            }
        }
        System.out.println("");
        System.out.println("");

    }
    
    public void runGreedy(){
        
        Scanner sc = new Scanner (System.in);

        System.out.println("");
        System.out.println("GREEDY SEARCH");
        System.out.println("");


        //N & C
        System.out.println("N C: ");
        String ncInput;
        ncInput = sc.nextLine();
        String[] splitNCInput = ncInput.split(" ");
        int[] ncToInteger = new int[splitNCInput.length];

        for(int j=0; j<splitNCInput.length; j++){
            ncToInteger[j]= Integer.parseInt(splitNCInput[j]);               
        }
        int n = ncToInteger[0];
        int c = ncToInteger[1];
        setCapacity(c);
        

        //DEPOT
        System.out.println("depot: ");
        String depotInput;
        depotInput = sc.nextLine();
        String[] splitDepotInput = depotInput.split(" ");
        int[] depotLineToInteger = new int[splitDepotInput.length];

        for(int j=0; j<splitDepotInput.length; j++){
            depotLineToInteger[j]= Integer.parseInt(splitDepotInput[j]);               
        }
        setDepot(depotLineToInteger[0], depotLineToInteger[1], depotLineToInteger[2]);


        //CUSTOMER  
        for (int i = 0; i<(n-1) ; i++){

            System.out.println("customer "+ (i+1));
            String userInput;
            userInput = sc.nextLine();
            String[] splitInput = userInput.split(" ");
            int[] toInteger = new int[splitInput.length];

            for(int j=0; j<splitInput.length; j++){
              toInteger[j]= Integer.parseInt(splitInput[j]);               
            }

            addCustomer(toInteger[0], toInteger[1], toInteger[2]);

        }
        
        while(getNumberOfCustomers() != 0){

            findCustUnderC();

            findNearestCust();
    
            updateCapacity();

        }
        
        System.out.println("Cost: " + getVehicleCost());
        System.out.println("");
        
        displayRoute();
        
        System.out.println("Tour cost: "+ getTourCost());
        System.out.println("");

        sc.close();
    }
}