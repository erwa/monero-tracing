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


// Count percentage of inputs with 1 or more mixins
// for which newest heuristic is successful.
// java CalculateNewestSuccessNoZeroMixins TxInputsAll TracedNewestHeuristic
public class CalculateNewestSuccessNoZeroMixins {

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

  private static Map<Pair, Integer> inputRingSize =
    new HashMap<>();

  private static void loadTxInputs(String file) throws Exception {
    System.err.println("Loading all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        inputRingSize.put(new Pair(tx, idx), parts.length - 2);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    }
    System.err.println("Loaded " + count + " input keys from file " + file);
  }

  public static void main(String[] args) throws Exception {
    String txInputs = args[0];
    String tracedNewest = args[1];

    loadTxInputs(txInputs);

    System.err.println("Reading inputs from " + tracedNewest);

    int count = 0;
    int numOneOrMore = 0;
    int success = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(tracedNewest))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        Pair inp = new Pair(tx, idx);

        if (inputRingSize.get(inp) > 1) {
            numOneOrMore++;
            if (Integer.parseInt(parts[2]) == 1) {
                success++;
            }
        }

        count++;
        if (count % 100000 == 0) {
          System.err.println("Processed " + count + " inputs from " + tracedNewest);
        }
      }
    }

    System.out.println("Total number of traced inputs: " + count);
    System.out.println("Total number of traced inputs: "
        + "with one than one mixin: " + numOneOrMore);
    System.out.println("Total number of one or more inputs for which " +
        "newest heuristic works: " + success);
    System.out.println("Percentage: " + (success * 1.0 / numOneOrMore));
  }
}