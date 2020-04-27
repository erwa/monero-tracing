import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


// Count percentage of inputs for which newest heuristic
// is successful. (includes zero-mixin inputs)
// java CalculateNewestSuccess TracedNewestHeuristic
public class CalculateNewestSuccess {
  public static void main(String[] args) throws Exception {
    String file = args[0];

    System.err.println("Reading inputs from " + file);

    int count = 0;
    int success = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        if (Integer.parseInt(line.split(" ")[2]) == 1) {
            success++;
        }
        count++;
        if (count % 100000 == 0) {
          System.err.println("Processed " + count + " inputs from " + file);
        }
      }
    }

    System.out.println("Total number of traced inputs: " + count);
    System.out.println("Total number for which " +
        "newest heuristic works: " + success);
    System.out.println("Percentage: " + (success * 1.0 / count));
  }
}