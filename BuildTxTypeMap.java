import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


// java BuildTxTypeMap TracedTxVersionXmrInputs
public class BuildTxTypeMap {

  public static void main(String[] args) throws Exception {
    String file = args[0];

    long start = System.currentTimeMillis();
    buildTxTypeMap(file);
    long time1 = System.currentTimeMillis();
    System.err.println("time to build tx type map: " + ((time1 - start) / 1000));

    writeMap();
    long time2 = System.currentTimeMillis();
    System.err.println("time to write tx type map: " + ((time2 - time1) / 1000));
  }

  // 1 = fully clear
  // 2 = clear -> hidden
  // 3 = hidden -> hidden
  private static Map<Integer, Integer> txTypeMap = new TreeMap<>();

  private static void buildTxTypeMap(String file) throws Exception {
    System.err.println("Processing all tx inputs from file " + file);
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      int prevTx = -1;
      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        if (tx != prevTx) {
          int txVersion = Integer.parseInt(parts[2]);
          long xmrInputs = Long.parseLong(parts[3]);
          processTxInput(tx, txVersion, xmrInputs);
        }

        count++;
        if (count % 1000 == 0) {
            System.err.println("Processed " + count + " inputs.");
        }
        prevTx = tx;
      }
    }
    System.err.println("Processed " + count + " inputs from file " + file);
  }

  private static void processTxInput(int tx, int txVersion, long xmrInputs) throws Exception {
    int type = 1;
    if (txVersion > 1) {
      type = 2;

      if (xmrInputs == 0) {
        type = 3;
      }
    }

    txTypeMap.put(tx, type);
  }

  private static void writeMap() {
    for (Map.Entry<Integer,Integer> entry : txTypeMap.entrySet()) {
      System.out.println(entry.getKey() + " " + entry.getValue());
    }
  }
}