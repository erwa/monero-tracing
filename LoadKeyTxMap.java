import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LoadKeyTxMap {
  private static final Map<String, String> keyTxMap = new HashMap<>();

  public static void main(String[] args) throws Exception {
    String file = args[0];

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
      System.out.println("Size of keyTxMap: " + keyTxMap.size());
    }
  }
}