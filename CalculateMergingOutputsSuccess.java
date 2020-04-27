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


// Count percentage of inputs for which merging outputs heuristic
// is successful.
// time java CalculateMergingOutputsSuccess TxInputsAll TracedInputsAllWithKeys DestKeysRingCT
public class CalculateMergingOutputsSuccess {
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
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
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

  private static Map<Pair, Integer> inputMixins = new HashMap<>();

  private static void loadInputMixins(String file) throws Exception {
    System.err.println("Loading all input mixins from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        int numMixins = parts.length - 3;
        if (numMixins > 10) {
          numMixins = 10;
        }
        inputMixins.put(new Pair(tx, idx), numMixins);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " input mixins.");
        }
      }
    }
    System.err.println("Loaded " + count + " input keys from file " + file);
  }



  public static void main(String[] args) throws Exception {
    String inputMixinsFile = args[0];
    String inputKeysFile = args[1];
    String destKeysFile = args[2];

    loadInputMixins(inputMixinsFile);
    loadTxInputs(inputKeysFile);
    processDestKeys(destKeysFile);
  }

  private static void processDestKeys(String file) throws Exception {
    Map<Integer, Integer> mixinTraced = new TreeMap<>();
    Map<Integer, Integer> mixinSuccess = new TreeMap<>();

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        int key = Integer.parseInt(parts[2]);
        Pair input = new Pair(tx, idx);

        if (inputKeysMap.containsKey(input)) {
          int numMixins = inputMixins.get(input);
          if (!mixinTraced.containsKey(numMixins)) {
            mixinTraced.put(numMixins, 0);
          }

          mixinTraced.put(numMixins, mixinTraced.get(numMixins) + 1);

          if (!mixinSuccess.containsKey(numMixins)) {
            mixinSuccess.put(numMixins, 0);
          }

          if (inputKeysMap.get(input) == key) {
            mixinSuccess.put(numMixins, mixinSuccess.get(numMixins) + 1);
          }
        }

        count++;
        if (count % 100000 == 0) {
          System.err.println("Processed " + count + " inputs from " + file);
        }
      }
    }

    System.out.println("Total number of dest inputs processed: " + count);
    for (Map.Entry<Integer,Integer> entry : mixinSuccess.entrySet()) {
      int mixins = entry.getKey();
      int total = mixinTraced.get(mixins);
      int correct = entry.getValue();
      System.out.print(mixins + " mixins: "
        + total + " traced, " + correct + " newest correct (");
      System.out.println((correct * 1.0) / total + ")");
    }
  }
}