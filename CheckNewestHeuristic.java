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


// java CheckNewestHeuristic TxInputsAll TracedInputsAllWithKeys
public class CheckNewestHeuristic {
  private static Map<Integer, String> txDayMap = new HashMap<>();
  private static Map<String, Integer> dayTxCount = new TreeMap<>();
  private static Set<Integer> tracedTxs = new HashSet<>();

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

  private static void checkTraced(String file) throws Exception {
    System.err.println("Loading traced txs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        Pair input = new Pair(Integer.parseInt(parts[0]),
          Integer.parseInt(parts[1]));
        int realKey = Integer.parseInt(parts[2]);

        System.out.print(parts[0] + " " + parts[1] + " ");
        Integer lastKey = inpLastKeyMap.get(input);
        if (lastKey == null) {
          System.err.println("No entry for tx input (" + parts[0]
            + ", " + parts[1] + ")");
        }
        if (lastKey != null && realKey == lastKey) {
          System.out.println("1");
        } else {
          System.out.println("0");
        }
        count++;
        if (count % 100000 == 0) {
          System.err.println("Processed " + count + " traced inputs");
        }
      }
    }
    System.err.println("Checked " + count + " transaction inputs from file " + file);
  }

  private static Map<Pair, Integer> inpLastKeyMap =
    new HashMap<>();

  private static void loadTxInputs(String file) throws Exception {
    System.err.println("Loading all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        int lastKey = Integer.parseInt(parts[parts.length - 1]);
        inpLastKeyMap.put(new Pair(tx, idx), lastKey);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    }
    System.err.println("Loaded " + count + " input keys from file " + file);
  }

  public static void main(String[] args) throws Exception {
    String txInputKeysFile = args[0];
    String tracedTxsFile = args[1];

    loadTxInputs(txInputKeysFile);
    checkTraced(tracedTxsFile);
  }
}