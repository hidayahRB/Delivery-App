package deliveryscheduler;

import deliveryscheduler.BFS.Node;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainPage {

    public static final PrintStream output = System.out;
    
    public static void main(String[] args) {
        
        // Choose which algorithm to use
        System.out.println("--------Best Tour Search--------");
        System.out.println("Enter 1 for Basic Simulation");
        System.out.println("Enter 2 for Greedy Search");
        System.out.println("Enter 3 for MCTS algorithm");
        System.out.println("--------------------------------\n");
        System.out.print("Choose algorithm to begin search : ");
        
        Scanner in = new Scanner(System.in);
        int choice = in.nextInt();
        
        System.out.println("");
        
        final int riderCapacity;
        
        if(choice == 1){
            ArrayList<Node> nodeList = new ArrayList<>();
            riderCapacity = loadInputForBasicSimulation(nodeList);
            
            constructCompleteUndirectedGraph(nodeList);
            
            Node depot = nodeList.get(0);
            
            System.out.println("The BFS traversal of the graph is \n");
        
            new BFS(depot, riderCapacity);
        }
        
        //GREEDY SEARCH ALGO but still receiving input from user when running
        else if(choice == 2){
            GreedySearch<Integer> greedyAlgo = new GreedySearch<>();
            greedyAlgo.runGreedy();
        }
        
        else if(choice == 3){
            ArrayList<Stop> nodeList = new ArrayList<>();
            riderCapacity = loadInputForMCTSAlgo(nodeList);
            
            final int level = 3, iteration = 1000, timeLimitInSec = 10, numOfNode = nodeList.size();
            Tour best_tour = new MctsAlgorithm(riderCapacity, level, iteration, numOfNode, nodeList, timeLimitInSec).search(level,iteration);

            synchronized(output){
                output.println("\n");
                output.println(best_tour);
            }
        }
        
        else System.out.println("Error input! Please try again.");
    
    }
    
    public static int loadInputForBasicSimulation(ArrayList<Node> nodeList){
        // Reading input from text file
        File file = new File("./Instances/sample1.txt");
 
        int riderCapacity = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            boolean isFirstLineRead = false;
            
            while ((line = br.readLine()) != null) {
                
                String trimmedInput = line.trim();
                
                if(!isFirstLineRead){
                    riderCapacity = Integer.parseInt(line);
                    isFirstLineRead = true;
                    continue;
                }
                
                String[] inputArr= trimmedInput.split(" ");
                //Put in the matrix using loop
                
                int x = Integer.parseInt(inputArr[0]);
                int y = Integer.parseInt(inputArr[1]);
                int capacity = Integer.parseInt(inputArr[2]);
                
                if(capacity == 0){
                    nodeList.add(new Node(new Depot(x, y, capacity)));
                }
                else{
                    nodeList.add(new Node(new Customer(x, y, capacity)));
                }
                
            }
            
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return riderCapacity;
        
    }
    
    private static void constructCompleteUndirectedGraph(List<Node> nodeList){
        
        for(Node node : nodeList){
            
            for(Node neighbourNode : nodeList){
                if(neighbourNode.equals(node)) continue;
                
                node.addneighbours(neighbourNode);
            }
            
        }
        
    }
    
    public static int loadInputForMCTSAlgo(ArrayList<Stop> nodeList){
        // Reading input from text file
        File file = new File("./Instances/sample1.txt");
 
        int riderCapacity = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            boolean isFirstLineRead = false;
            
            while ((line = br.readLine()) != null) {
                String trimmedInput = line.trim();
                
                if(!isFirstLineRead){
                    riderCapacity = Integer.parseInt(line);
                    isFirstLineRead = true;
                    continue;
                }
                
                String[] inputArr= trimmedInput.split(" ");
                //Put in the matrix using loop
                
                int x = Integer.parseInt(inputArr[0]);
                int y = Integer.parseInt(inputArr[1]);
                int capacity = Integer.parseInt(inputArr[2]);
                
                if(capacity == 0){
                    nodeList.add(new Depot(x, y, capacity));
                }
                else{
                    nodeList.add(new Customer(x, y, capacity));
                }
            }
            
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return riderCapacity;
        
    }
}
