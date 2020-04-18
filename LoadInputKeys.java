import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;


public class LoadInputKeys {

  public static void main(String[] args) throws Exception {
    String file = args[0];

    System.err.println("Loading all input keys from file " + file);

    Map<String, Integer> keyIntMap = new HashMap<>();

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String key; (key = br.readLine()) != null; ) {
        keyIntMap.put(key, count);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " keys.");
        }
      }
    }

    System.err.println("Loaded " + count + " input keys from file " + file);
    Thread.sleep(30000);
  }
}