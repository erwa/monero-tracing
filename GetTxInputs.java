import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;


public class GetTxInputs {
  private static final String BASE_URL = "http://127.0.0.1:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  private static Map<String, Integer> loadKeyIntMap(String keysFile) throws Exception {
    System.err.println("Loading all input keys from file " + keysFile);
    int count = 0;
    Map<String, Integer> keyIntMap = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(keysFile))) {
      for (String key; (key = br.readLine()) != null; ) {
        keyIntMap.put(key, count);
        count++;
        if (count % 100000 == 0) {
            System.err.println("Loaded " + count + " keys.");
        }
      }
    }
    System.err.println("Loaded " + count + " input keys from file " + keysFile);
    return keyIntMap;
  }

  public static void main(String[] args) throws Exception {
    String txsFile = args[0];
    String keysFile = args[1];

    Map<String, Integer> keyIntMap = loadKeyIntMap(keysFile);
    parseTxs(txsFile, keyIntMap);
  }

  private static void parseTxs(String txsFile, Map<String, Integer> keyIntMap) throws Exception {
    System.err.println("Processing txs from file " + txsFile);

    int idx = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(txsFile))) {
      for (String tx; (tx = br.readLine()) != null; ) {
        parseTx(tx, idx, keyIntMap);
        idx++;
        if (idx % 1000 == 0) {
            System.err.println("Processed " + idx + " txs.");
        }
      }
    }

    System.err.println("Processed " + idx + " txs from file " + txsFile);
  }

  private static void parseTx(String tx, int txIdx, Map<String, Integer> keyIntMap) throws Exception {
    String url = BASE_URL + "transaction/" + tx;
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build();
    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    int inputStart = data.indexOf("inputs\":[{");
    int inputEnd = data.indexOf("}]}]", inputStart);
    // need to include }] in substring so matching works later
    String inputs = data.substring(inputStart, inputEnd + 2);

    int currIdx = 0;
    int inpIdx = 0;
    String mixin;
    int mixinIdx;
    while (true) {
        currIdx = inputs.indexOf("mixins", currIdx);
        if (currIdx == -1) {
            break;
        }
        System.out.print(txIdx + " " + inpIdx);
        mixin = inputs.substring(currIdx, inputs.indexOf("}]", currIdx));
        mixinIdx = 0;
        while (true) {
          mixinIdx = mixin.indexOf("public_key", mixinIdx);
          if (mixinIdx == -1) {
            break;
          }
          System.out.print(" " + keyIntMap.get(mixin.substring(mixinIdx + 13, mixinIdx + 77)));
          mixinIdx += 77;
        }
        System.out.println();
        currIdx += mixin.length();
        inpIdx++;
    }
  }
}