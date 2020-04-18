import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


// java GetFullyTracedTxs TxInputsAll TracedInputsFull
public class GetFullyTracedTxs {
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

  private static Map<Integer, Integer> txInputCounts =
    new HashMap<>();

  // takes ~36 sec
  private static void loadTxInputs(String file) throws Exception {
    System.err.println("Loading tx input counts from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        if (!txInputCounts.containsKey(tx)) {
          txInputCounts.put(tx, 0);
        }
        txInputCounts.put(tx, txInputCounts.get(tx) + 1);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " tx inputs.");
        }
      }
    }
    System.err.println("Loaded " + count + " tx inputs from file " + file);
  }

  private static void getTxs(String file) throws Exception {
    System.err.println("Loading all traced inputs from file " + file);
    Map<Integer, Integer> txTracedInputCounts = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        if (!txTracedInputCounts.containsKey(tx)) {
          txTracedInputCounts.put(tx, 0);
        }
        txTracedInputCounts.put(tx, txTracedInputCounts.get(tx) + 1);
      }
    }

    // check fully traced txns
    Set<Integer> fullyTracedTxns = new TreeSet<>();
    for (Map.Entry<Integer, Integer> entry : txTracedInputCounts.entrySet()) {
      int tx = entry.getKey();
      if (txInputCounts.get(tx) == entry.getValue()) {
        fullyTracedTxns.add(tx);
      }
    }

    for (int tx : fullyTracedTxns) {
      System.out.println(tx);
    }
  }

  public static void main(String[] args) throws Exception {
    loadTxInputs(args[0]);

    long start = System.currentTimeMillis();
    getTxs(args[1]);
    long time1 = System.currentTimeMillis();

    System.err.println("time to count: " + ((time1 - start) / 1000));
  }

}