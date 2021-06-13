import java.io.*;

public class MainPage {

    public static void main(String[] args) {
        
        // Choose whether Admin or Customer 
        
        // Reading input from text file
        File file = new File("./Instances/sample1.txt");
 
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = br.readLine()) != null) {
                String[] inputArr= line.trim().split(" ");
                //Put in the matrix using loop
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //GREEDY SEARCH ALGO but still ada receive input from user
        GreedySearch<Integer> greedyAlgo = new GreedySearch<>();
        greedyAlgo.runGreedy();
    
    }
}
