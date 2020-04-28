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


// Count percentage of inputs for which newest heuristic works
// broken down by the number of mixins (grouping 10+ mixins together)
// before some txn.

// java NewestByMixinsGroup10PlusBefore TxInputsAll TracedNewestHeuristic <end>
public class NewestByMixinsGroup10PlusBefore {

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

  private static Map<Pair, Integer> inputMixins =
    new HashMap<>();

  private static void loadTxInputs(String file) throws Exception {
    System.err.println("Loading all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        int mixins = parts.length - 3;
        if (mixins > 10) {
          mixins = 10;
        }
        inputMixins.put(new Pair(tx, idx), mixins);
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
    int end = Integer.parseInt(args[2]);

    loadTxInputs(txInputs);
    process(tracedNewest, end);
  }

  private static Map<Integer, Integer> mixinCount = new TreeMap<>();
  private static Map<Integer, Integer> mixinSuccess = new TreeMap<>();

  private static void process(String tracedNewest, int end) throws Exception {
    System.err.println("Reading inputs from " + tracedNewest);

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(tracedNewest))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);

        if (tx < end) {
          boolean isNewestSuccessful = Integer.parseInt(parts[2]) == 1;
          Pair inp = new Pair(tx, idx);
          int mixins = inputMixins.get(inp);

          if (!mixinCount.containsKey(mixins)) {
            mixinCount.put(mixins, 0);
          }
          mixinCount.put(mixins, mixinCount.get(mixins) + 1);

          if (isNewestSuccessful) {
            if (!mixinSuccess.containsKey(mixins)) {
              mixinSuccess.put(mixins, 0);
            }
            mixinSuccess.put(mixins, mixinSuccess.get(mixins) + 1);
          }

        }

        count++;
        if (count % 100000 == 0) {
          System.err.println("Processed " + count + " inputs from " + tracedNewest);
        }
      }
    }

    System.err.println("Total number of traced inputs before tx "
      + end + ": " + count);
    for (Map.Entry<Integer,Integer> entry : mixinSuccess.entrySet()) {
      int mixins = entry.getKey();
      int total = mixinCount.get(mixins);
      int correct = entry.getValue();
      System.out.print(mixins + " mixins: "
        + total + " traced, " + correct + " newest correct (");
      System.out.println((correct * 1.0) / total + ")");
    }
    System.err.println("Note inputs with more than 10 mixins are "
      + "put in the same bucket as the 10 mixin inputs.");
  }
}