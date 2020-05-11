import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;


// java ParseAnonSetData TxInputsAll InputsReduced
public class ParseAnonSetData {
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

  // takes ~36 sec
  private static Map<Pair,Integer> loadTxSizes(String file) throws Exception {
    System.err.println("Loading all tx inputs from file " + file);

    Map<Pair, Integer> szMap = new HashMap<>();
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);

        szMap.put(new Pair(tx, idx), parts.length - 2);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    }

    System.err.println("Loaded " + count + " inputs from file " + file);

    return szMap;
  }

  public static void main(String[] args) throws Exception {
    String oldFile = args[0];
    String newFile = args[1];

    long start = System.currentTimeMillis();
    Map<Pair, Integer> origSzMap = loadTxSizes(oldFile);
    long time1 = System.currentTimeMillis();
    System.err.println("time to load old file: " + ((time1 - start) / 1000));

    Map<Pair, Integer> newSzMap = loadTxSizes(newFile);
    long time2 = System.currentTimeMillis();
    System.err.println("time to load new file: " + ((time2 - time1) / 1000));

    printReducedAnonSet(origSzMap, newSzMap);
    long time3 = System.currentTimeMillis();
    System.err.println("time to parse data: " + ((time3 - time2) / 1000));
  }

  private static void printReducedAnonSet(Map<Pair, Integer> origSzMap,
      Map<Pair, Integer> newSzMap) {
    // num mixins -> anonSz -> count
    // [0, 10]       [1, 11]
    Map<Integer,Map<Integer,Integer>> anonSzMap = new TreeMap<>();

    for (Map.Entry<Pair,Integer> entry : origSzMap.entrySet()) {
      int sz = entry.getValue();
      if (sz <= 11) {
        int mixins = sz - 1;
        if (!anonSzMap.containsKey(mixins)) {
          anonSzMap.put(mixins, new TreeMap<>());
        }
        Map<Integer,Integer> countMap = anonSzMap.get(mixins);

        int anonSz = newSzMap.get(entry.getKey());
        if (!countMap.containsKey(anonSz)) {
          countMap.put(anonSz, 0);
        }
        countMap.put(anonSz, countMap.get(anonSz) + 1);
      }
    }

    // print out anon set sizes
    for (Map.Entry<Integer,Map<Integer,Integer>> entry : anonSzMap.entrySet()) {
      System.out.print(entry.getKey());

      // first sum values
      int sum = 0;
      for (int szCount : entry.getValue().values()) {
        sum += szCount;
      }

      // now print out percentages
      for (int i = 1; i <= 11; i++) {
        if (entry.getValue().containsKey(i)) {
          System.out.print("," + (entry.getValue().get(i) * 1.0 / sum));
        } else {
          System.out.print("," + 0.0);
        }
      }

      System.out.println();
    }
  }
}