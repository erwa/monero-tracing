import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


// java TraceTxs TxInputsAll
public class TraceTxs {
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

  private static Map<Pair, Set<Integer>> inpKeysMap =
    new HashMap<>();

  // takes ~36 sec
  private static void loadTxInputs(String file) throws Exception {
    System.err.println("Loading all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        Set<Integer> keys = new HashSet<>();
        for (int i = 2; i < parts.length; i++) {
          keys.add(Integer.parseInt(parts[i]));
        }
        inpKeysMap.put(new Pair(tx, idx), keys);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    }
    System.err.println("Loaded " + count + " input keys from file " + file);
  }

  public static void main(String[] args) throws Exception {
    String file = args[0];

    long start = System.currentTimeMillis();
    loadTxInputs(file);
    long time1 = System.currentTimeMillis();

    System.err.println("time to load: " + ((time1 - start) / 1000));

    trace(1);
    long time2 = System.currentTimeMillis();

    System.err.println("time to trace iter: " + ((time2 - time1) / 1000));

    printTraced();
  }

  private static Map<Pair,Integer> tracedInputs = new TreeMap<>(
    (Pair p1, Pair p2) -> {
      if (p1.tx != p2.tx) {
        return p1.tx - p2.tx;
      }
      return p1.idx - p2.idx;
    });

  private static void trace(int iterCount) {
    // alg: get all inputs with 1 key
    // remove but save key
    // increment traced inputs count

    System.err.println("start iter " + iterCount + ": num untraced inputs: " + inpKeysMap.size());
    long start = System.currentTimeMillis();

    // get all inputs with 1 key
    Set<Integer> tracedKeys = new HashSet<>();
    Iterator<Map.Entry<Pair, Set<Integer>>> iter = inpKeysMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Pair, Set<Integer>> entry = iter.next();
      if (entry.getValue().size() == 1) {
        // add to traced keys set
        tracedKeys.addAll(entry.getValue());

        // add to traced inputs
        tracedInputs.put(entry.getKey(), entry.getValue().iterator().next());

        // remove from inputs
        iter.remove();
      }
    }

    System.err.println("num newly traced inputs: " + tracedKeys.size());

    if (tracedKeys.size() > 0) {
      // remove traced keys from all remaining inputs
      for (Map.Entry<Pair, Set<Integer>> entry : inpKeysMap.entrySet()) {
        entry.getValue().removeIf(key -> tracedKeys.contains(key));
      }
    }

    System.err.println("end iter " + iterCount + ": num untraced inputs: " + inpKeysMap.size());
    long end = System.currentTimeMillis();
    System.err.println("iter " + iterCount + " took " + ((end - start) / 1000) + " seconds.");

    if (tracedKeys.size() > 0) {
      trace(iterCount + 1);
    }
  }

  private static void printTraced() {
    for (Map.Entry<Pair,Integer> traced : tracedInputs.entrySet()) {
      System.out.println(traced.getKey().tx + " "
        + traced.getKey().idx + " " + traced.getValue());
    }
  }
}