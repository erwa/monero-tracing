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


public class Utils {

  public static Map<String, Integer> loadKeyMap() {
    String file = "InputKeysAll";
    System.err.println("loading keys from " + file);
    Map<String, Integer> keyMap = new HashMap<>();

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String line; (line = br.readLine()) != null; ) {
        keyMap.put(line, count);
        count++;
        if (count % 10000 == 0) {
            System.err.println("Processed " + count + " keys.");
        }
      }

      System.err.println("Done loading keyMap. Size: " + keyMap.size());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return keyMap;
  }

  public static Map<Integer, String> loadKeyIdToHashMap() {
    String file = "InputKeysAll";
    System.err.println("loading keys from " + file);
    Map<Integer, String> keyMap = new HashMap<>();

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String line; (line = br.readLine()) != null; ) {
        keyMap.put(count, line);
        count++;
        if (count % 10000 == 0) {
            System.err.println("Processed " + count + " keys.");
        }
      }

      System.err.println("Done loading keyMap. Size: " + keyMap.size());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return keyMap;
  }

  public static Map<String, String> loadKeyTxMap() {
		String txKeysFile = "TxOutputsRingCTAll";
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
            System.err.println("Processed " + count + " transactions.");        }
      }
      System.err.println("Size of keyTxMap: " + keyTxMap.size());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return keyTxMap;
  }

  public static Map<String, Integer> loadTxMap() {
		String file = "TxHashesAll";
    System.err.println("loading txs from " + file);

		Map<String, Integer> txMap = new HashMap<>();
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String line; (line = br.readLine()) != null; ) {
        txMap.put(line, count);
        count++;
        if (count % 10000 == 0) {
            System.err.println("Processed " + count + " keys.");
        }
      }

      System.err.println("Done loading txMap. Size: " + txMap.size());
      System.err.println("Count = " + count);
    } catch (Exception e) {
			throw new RuntimeException(e);
		}
    return txMap;
  }	

  public static Map<Integer, String> loadTxIdToHashMap() {
		String file = "TxHashesAll";
    System.err.println("loading txs from " + file);

		Map<Integer, String> txMap = new HashMap<>();
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String line; (line = br.readLine()) != null; ) {
        txMap.put(count, line);
        count++;
        if (count % 10000 == 0) {
            System.err.println("Processed " + count + " keys.");
        }
      }

      System.err.println("Done loading txMap. Size: " + txMap.size());
      System.err.println("Count = " + count);
    } catch (Exception e) {
			throw new RuntimeException(e);
		}
    return txMap;
  }	


  public static Set<Pair> loadTracedInputs() {
    String file = "TracedInputsAllWithKeys";
    System.err.println("Loading all traced inputs from file " + file);
    int count = 0;
    Set<Pair> tracedInputs = new HashSet<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        tracedInputs.add(new Pair(tx, idx));
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.err.println("Loaded " + count + " inputs from file " + file);
    return tracedInputs;
  }


  public static Map<Integer, Integer> loadTracedTxTypeMap() throws Exception {
    String file = "TracedTxTypeMap";
    System.err.println("loading tx type map from " + file);
    Map<Integer, Integer> txTypeMap = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        txTypeMap.put(Integer.parseInt(parts[0]),
          Integer.parseInt(parts[1]));
      }
    }
    System.err.println("done loading tx type map from " + file);
    return txTypeMap;
  }


  public static Map<Pair, Integer> loadTracedInputKeyMap() {
    String file = "TracedInputsAllWithKeys";
    System.err.println("Loading all tx inputs from file " + file);
    int count = 0;
  	Map<Pair, Integer> inputKeyMap = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {      for (String txInput; (txInput = br.readLine()) != null; ) {
        String[] parts = txInput.split(" ");
        int tx = Integer.parseInt(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        int key = Integer.parseInt(parts[2]);
        inputKeyMap.put(new Pair(tx, idx), key);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " inputs.");
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.err.println("Loaded " + count + " input keys from file " + file);
		return inputKeyMap;
  }


  private static final String BASE_URL = "http://localhost:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  private static String getTx(String tx) throws Exception {
    String url = BASE_URL + "transaction/" + tx;
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build();

    return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
  }

  public static int getNumMixins(String tx) throws Exception {
    String data = getTx(tx);
    int mixinStart = data.indexOf("mixin\"");
    int mixinEnd = data.indexOf(",", mixinStart);

    // minus one is because "mixin" is ring size and we want number of mixins
    return Integer.parseInt(data.substring(mixinStart + 7, mixinEnd)) - 1;
  }

  public static int getType(String tx) throws Exception {
    // find tx_version
    String data = getTx(tx);
    int txVersionStart = data.indexOf("tx_version\"");
    data = data.substring(txVersionStart);
    int txVersion = Integer.parseInt(data.substring(12, data.indexOf(",")));

    if (txVersion == 1) {
      // clear -> clear
      return 1;
    }
    // else txVersion > 1

    // find xmr_inputs (should appear after tx_version because keys are sorted alphabetically)
    int xmrInputsStart = data.indexOf("xmr_inputs");

    // there should be an xmr_outputs key after xmr_inputs, so searching for , should be safe
    long xmrInputs = Long.parseLong(data.substring(xmrInputsStart + 12, data.indexOf(",", xmrInputsStart)));
    if (xmrInputs == 0) {
      // hidden -> hidden
      return 3;
    }
    // some clear -> hidden
    return 2;
  }
}
