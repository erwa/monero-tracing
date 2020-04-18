import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;


public class LoadTxs {

  public static void main(String[] args) throws Exception {
    String file = args[0];

    System.err.println("Loading all txs from file " + file);

    Map<String, Integer> txIntMap = new HashMap<>();

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String tx; (tx = br.readLine()) != null; ) {
        txIntMap.put(tx, count);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " txs.");
        }
      }
    }

    System.err.println("Loaded " + count + " txs from file " + file);
    Thread.sleep(30000);
  }
}