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


// Filters input key guesses down to those that are correct.
//
// time java FilterCorrect TracedInputsAllWithKeys DestKeysRingCTTraced
//
// Both TracedInputsAllWithKeys and DestKeysRingCTTraced should use ids
public class FilterCorrect {
  private static class Pair {
    int tx;
    int idx;
    Pair(int tx, int idx) {
      this.tx = tx;
      this.idx = idx;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) {
        return false;
      }

      if (!(o instanceof Pair)) {
        return false;
      }

      Pair p = (Pair) o;
      return p.tx == this.tx && p.idx == this.idx;
    }

    @Override
    public int hashCode() {
      return 53 * Integer.hashCode(tx) + Integer.hashCode(idx);
    }
  }

  private static Map<Pair, Integer> inputKeysMap = new HashMap<>();

  private static void loadTxInputs(String file) throws Exception {
    System.err.println("Loading all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        int key = Integer.parseInt(parts[2]);
        inputKeysMap.put(new Pair(tx, idx), key);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    }
    System.err.println("Loaded " + count + " input keys from file " + file);
  }

  public static void main(String[] args) throws Exception {
    String tracedInputsFile = args[0];
    String destKeysFile = args[1];

    loadTxInputs(tracedInputsFile);
    processDestKeys(destKeysFile);
  }

  private static void processDestKeys(String file) throws Exception {
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        int key = Integer.parseInt(parts[2]);
        Pair input = new Pair(tx, idx);

        if (!inputKeysMap.containsKey(input)) {
          throw new RuntimeException("tx " + tx + " input " + idx + " should be in inputKeysMap!");
        }

        if (inputKeysMap.get(input) == key) {
          System.out.println(tx + " " + idx + " " + key);
        }

        count++;
        if (count % 1000 == 0) {
          System.err.println("Processed " + count + " inputs from " + file);
        }
      }
    }

    System.err.println("Total number of dest inputs processed: " + count);
  }
}
