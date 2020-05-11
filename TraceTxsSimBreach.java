import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;


// java TraceTxsSimBreach InputsReduced 0.3
public class TraceTxsSimBreach {
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

  private static Map<Pair, List<Integer>> inpKeysMap =
    new HashMap<>();

  private static List<Pair> nonFullyDeduced = new ArrayList<>();

  // takes ~36 sec
  private static void loadTxInputs(String file) throws Exception {
    System.err.println("Loading all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        List<Integer> keys = new ArrayList<>();
        for (int i = 2; i < parts.length; i++) {
          keys.add(Integer.parseInt(parts[i]));
        }
        Pair p = new Pair(tx, idx);
        inpKeysMap.put(p, keys);
        if (keys.size() > 1) {
          nonFullyDeduced.add(p);
        }
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    }
    System.err.println("Loaded " + count + " input keys from file " + file);
    System.err.println("Num non-fully deduced: " + nonFullyDeduced.size());
  }

  private static void simBreach(float percent) {
    int numToBreach = (int) (percent * nonFullyDeduced.size());
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);

    System.err.println("Simulating breaching " + numToBreach
      + " (" + df.format(percent * 100) + "%) of "
      + "non-fully deduced inputs.");
    Collections.shuffle(nonFullyDeduced);

    // now breach the first numToBreach elements
    for (int i = 0; i < numToBreach; i++) {
      Pair p = nonFullyDeduced.get(i);
      List<Integer> keys = inpKeysMap.get(p);
      inpKeysMap.put(p, List.of(keys.get(keys.size() - 1)));
    }
  }

  public static void main(String[] args) throws Exception {
    String file = args[0];

    // 0 to 1
    float breachPercent = Float.parseFloat(args[1]);

    long start = System.currentTimeMillis();
    loadTxInputs(file);
    long time1 = System.currentTimeMillis();
    System.err.println("time to load: " + ((time1 - start) / 1000));

    simBreach(breachPercent);
    long time2 = System.currentTimeMillis();
    System.err.println("time to sim breach: " + ((time2-time1)/1000));

    trace(1);
    long time3 = System.currentTimeMillis();
    System.err.println("time to trace iter: " + ((time3 - time2) / 1000));

    printReducedAnonSet();
    long time4 = System.currentTimeMillis();
    System.err.println("time to print anon set: " + ((time4 - time3) / 1000));
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
    int contradictions = 0;

    Iterator<Map.Entry<Pair, List<Integer>>> iter = inpKeysMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Pair, List<Integer>> entry = iter.next();
      List<Integer> keys = entry.getValue();
      if (keys.size() == 1) {
        // add to traced keys set
        tracedKeys.add(keys.get(0));

        // add to traced inputs
        tracedInputs.put(entry.getKey(), keys.get(0));

        // remove from inputs
        iter.remove();
      } else if (keys.size() == 0) {
        // contradiction, put null as indication
        tracedInputs.put(entry.getKey(), null);

        // remove from inputs
        iter.remove();

        contradictions++;
      }
    }

    System.err.println("num newly traced inputs: " + tracedKeys.size());
    System.err.println("num contradictions: " + contradictions);

    if (tracedKeys.size() > 0) {
      // remove traced keys from all remaining inputs
      for (Map.Entry<Pair, List<Integer>> entry : inpKeysMap.entrySet()) {
        entry.getValue().removeIf(key -> tracedKeys.contains(key));
      }
    }

    System.err.println("end iter " + iterCount + ": num untraced inputs: " + inpKeysMap.size());
    long end = System.currentTimeMillis();
    System.err.println("iter " + iterCount + " took " + ((end - start) / 1000) + " seconds.\n");

    if (tracedKeys.size() > 0) {
      trace(iterCount + 1);
    }
  }

  private static void printReducedAnonSet() {
    // merge traced and non-fully traced
    Map<Pair, List<Integer>> inputAnonSet = new TreeMap<>((Pair p1, Pair p2) -> {
      if (p1.tx != p2.tx) {
        return p1.tx - p2.tx;
      }
      return p1.idx - p2.idx;
    });

    inputAnonSet.putAll(inpKeysMap);
    for (Map.Entry<Pair,Integer> traced : tracedInputs.entrySet()) {
      Integer i = traced.getValue();
      List<Integer> l = new ArrayList();
      if (i != null) {
        l = List.of(i);
      }
      inputAnonSet.put(traced.getKey(), l);
    }

    // print out reduced anon set
    for (Map.Entry<Pair,List<Integer>> entry : inputAnonSet.entrySet()) {
      StringBuilder sb = new StringBuilder();
      sb.append(entry.getKey().tx + " " + entry.getKey().idx + " ");
      sb.append(String.join(" ", // convert List<Integer> to List<String>
        entry.getValue().stream().map(Object::toString)
          .collect(Collectors.toList())));
      System.out.println(sb.toString().trim());
    }
  }
}