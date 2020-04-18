import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


// java GetZeroMixinTxns TxInputsAll
public class GetZeroMixinTxns {
  private static Set<Integer> zeroMixinTxns = new TreeSet<>();

  private static void process(String file) throws Exception {
    System.err.println("Reading all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        if (parts.length == 3) {
          // zero mixin (tx, inputIdx, singleKey)
          zeroMixinTxns.add(Integer.parseInt(parts[0]));
        }
        count++;
        if (count % 100000 == 0) {
            System.err.println("Processed " + count + " lines.");
        }
      }
    }
    for (int tx : zeroMixinTxns) {
      System.out.println(tx);
    }
    System.err.println("Num zero mixin txns: " + zeroMixinTxns.size());
  }

  public static void main(String[] args) throws Exception {
    String file = args[0];

    long start = System.currentTimeMillis();
    process(file);
    long time1 = System.currentTimeMillis();

    System.err.println("time taken: " + ((time1 - start) / 1000));
  }

}