import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


// Counts the number of traced txs and inputs by type >= some txId
// time java CountTracedTypesAfter 890806
//
// TracedInputsAll has format
//   txId idx
//
public class CountTracedTypesAfter {
  private static void count(int start) throws Exception {
    Map<Integer, Integer> txTypeMap = Utils.loadTracedTxTypeMap();

    String file = "TracedInputsAll";

    System.err.println("Processing traced inputs from file " + file);
    Set<Integer> seenTxs = new HashSet<>();
    Map<Integer, Integer> txTypeCounts = new TreeMap<>();
    Map<Integer, Integer> inputTypeCounts = new TreeMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      int count = 0;
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);

        if (tx >= start) {
          int type = txTypeMap.get(tx);
          if (!seenTxs.contains(tx)) {
            txTypeCounts.compute(type, (t, c) -> (c == null) ? 1 : c + 1);
            seenTxs.add(tx);
          }

          inputTypeCounts.compute(type, (t, c) -> (c == null) ? 1 : c + 1);
        }

        count++;
        if (count % 1000 == 0) {
          System.err.println("Processed " + count + " lines from " + file);
        }
      }
    }

    // print stats
    for (Map.Entry<Integer, Integer> entry : txTypeCounts.entrySet()) {
      System.out.println("Num type " + entry.getKey() + " txs after tx " + start + ": " + entry.getValue());
    }
    for (Map.Entry<Integer, Integer> entry : inputTypeCounts.entrySet()) {
      System.out.println("Num type " + entry.getKey() + " inputs after tx " + start + ": " + entry.getValue());
    }
  }

  public static void main(String[] args) throws Exception {
    count(Integer.parseInt(args[0]));
  }

}
