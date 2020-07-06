import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// time java LoadKeyTxMap TxOutputsRingCTAll
public class LoadKeyTxMap {

  public static Map<String, String> loadKeyTxMap(String txKeysFile) {
    Map<String, String> keyTxMap = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(txKeysFile))) {
      int count = 0;
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        String tx = parts[0];
        String[] keys = parts[1].split(",");
        for (String key : keys) {
            keyTxMap.put(key, tx);
        }
        count++;
        if (count % 100 == 0) {
            System.err.println("Processed " + count + " transactions.");
        }
      }
      System.err.println("Size of keyTxMap: " + keyTxMap.size());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return keyTxMap;
  }

  public static void main(String[] args) throws Exception {
    String file = args[0];
    loadKeyTxMap(file);
  }
}
