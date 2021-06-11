import java.io.*;

public class MainPage {

    public static void main(String[] args) {
        
        // Choose whether Admin or Customer 
        
        // Reading input from text file
        File file = new File("sample1.txt");
 
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

    
}
