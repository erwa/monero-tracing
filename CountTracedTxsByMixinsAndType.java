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


// TODO: FIX: make this count by mixins
// Count number traced txs broken down by mixin and type (clear, clear -> hidden, hidden)
// starting from some tx id

// for post RingCT, use <start> = 890806
// java CountTracedTxsByMixinsAndType TxInputsAll TracedTxTypeMap <start>
public class CountTracedTxsByMixinsAndType {

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
        inputMixins.put(new Pair(tx, idx), parts.length - 3);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    }
    System.err.println("Loaded " + count + " input keys from file " + file);
  }

  private static Map<Integer, Integer> txTypeMap = new HashMap<>();

  private static void loadTracedTxTypeMap(String file) throws Exception {
    System.err.println("loading tx type map from " + file);
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        txTypeMap.put(Integer.parseInt(parts[0]),
          Integer.parseInt(parts[1]));
      }
    }
    System.err.println("done loading tx type map from " + file);
  }

  public static void main(String[] args) throws Exception {
    String txInputs = args[0];
    String tracedTxTypeMapFile = args[1];
    String tracedNewest = args[2];
    int start = Integer.parseInt(args[3]);

    loadTxInputs(txInputs);
    loadTracedTxTypeMap(tracedTxTypeMapFile);
    process(tracedNewest, start);
  }

  private static Map<Integer, Integer> mixinCount = new TreeMap<>();

  // type 1 txns (fully clear)
  private static Map<Integer, Integer> mixinSuccess1 = new TreeMap<>();
  // type 2 txns (clear -> hidden)
  private static Map<Integer, Integer> mixinSuccess2 = new TreeMap<>();
  // type 3 txns (fully hidden)
  private static Map<Integer, Integer> mixinSuccess3 = new TreeMap<>();

  // read traced newest file (which records correct/incorrect)
  private static void process(String tracedNewest, int start) throws Exception {
    System.err.println("Reading inputs from " + tracedNewest);

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(tracedNewest))) {
      for (String line; (line = br.readLine()) != null; ) {
        // each line is "txId idx correct/incorrect"
        String[] parts = line.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);

        if (tx >= start) {
          boolean isNewestSuccessful = Integer.parseInt(parts[2]) == 1;
          Pair inp = new Pair(tx, idx);
          int mixins = inputMixins.get(inp);

          if (mixins > 10) {
            mixins = 10;
          }

          if (!mixinCount.containsKey(mixins)) {
            mixinCount.put(mixins, 0);
          }
          mixinCount.put(mixins, mixinCount.get(mixins) + 1);

          if (isNewestSuccessful) {
            int type = txTypeMap.get(tx);
            Map<Integer,Integer> mixinSuccessMap;
            switch (type) {
              case 1: mixinSuccessMap = mixinSuccess1; break;
              case 2: mixinSuccessMap = mixinSuccess2; break;
              case 3: mixinSuccessMap = mixinSuccess3; break;
              default:
                throw new RuntimeException();
            }
            if (!mixinSuccessMap.containsKey(mixins)) {
              mixinSuccessMap.put(mixins, 0);
            }
            mixinSuccessMap.put(mixins, mixinSuccessMap.get(mixins) + 1);
          }

        }

        count++;
        if (count % 100000 == 0) {
          System.err.println("Processed " + count + " inputs from " + tracedNewest);
        }
      }
    }

    System.err.println("Total number of traced inputs after tx "
      + start + ": " + count);

    // now print per-mixin stats
    for (Map.Entry<Integer,Integer> entry : mixinCount.entrySet()) {
      int mixins = entry.getKey();
      int total = entry.getValue();

      int correct1 = mixinSuccess1.containsKey(mixins) ? mixinSuccess1.get(mixins) : 0;
      int correct2 = mixinSuccess2.containsKey(mixins) ? mixinSuccess2.get(mixins) : 0;
      int correct3 = mixinSuccess3.containsKey(mixins) ? mixinSuccess3.get(mixins) : 0;

      // mixins, total traced, correct1, correct2, correct3
      if (mixins == 10) {
        System.out.print("10+");
      } else {
        System.out.print(mixins);
      }
      System.out.println("," + total + "," + correct1 + "," + correct2 + "," + correct3);
    }
  }
}
