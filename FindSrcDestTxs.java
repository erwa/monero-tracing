import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

// time java -cp *:. FindSrcDestTxs 2037553 2037653 TxOutputsRingCTAll
public class FindSrcDestTxs {
  private static final Map<String, String> keyTxMap = new HashMap<>();

  private static final String BASE_URL = "http://127.0.0.1:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  private static final void loadKeyTxMap(String file) throws Exception {
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
        if (count % 1000 == 0) {
            System.err.println("Processed " + count + " transactions.");
        }
      }
      System.err.println("Done loading keyTxMap. Size: " + keyTxMap.size());
    }
  }

  public static void main(String[] args) throws Exception {
    int start = Integer.parseInt(args[0]);
    int end = Integer.parseInt(args[1]);

    String file = args[2];
    loadKeyTxMap(file);

    System.err.println("Getting tx outputs for blocks in [" + start + ", " + end + ")");
    for (int i = start; i < end; i++) {
        if (i % 10 == 0) {
            System.err.println("Processing block " + i);
        }
        parseBlock(i);
    }

    System.err.println("Done getting tx outputs for blocks in [" + start + ", " + end + ")");
  }

  private static void parseBlock(int block) throws Exception {
    String url = BASE_URL + "block/" + block;
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build();
    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    int txsStart = data.indexOf("txs\":[{");
    data = data.substring(txsStart + 5, data.indexOf("]", txsStart) + 1);

    // first txn is always coinbase
    int currIdx = data.indexOf("}");
    while (true) {
        currIdx = data.indexOf("tx_hash\"", currIdx);
        if (currIdx == -1) {
            break;
        }
        parseTx(data.substring(currIdx + 10, currIdx + 74));
        currIdx += 74;
    }
  }

  private static class KeyIdxs {
    public Set<String> keys = new HashSet<>();
    public Set<Integer> idxs = new HashSet<>();
    public boolean valid = true;
  }

  private static void parseTx(String hash) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(BASE_URL + "transaction/" + hash))
            .build();

    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

    int inputStart = data.indexOf("inputs\":[{");
    int inputEnd = data.indexOf("}]}]", inputStart);
    JSONArray inputs = JSON.parseArray(data.substring(inputStart + 8, inputEnd + 4));

    Map<String, KeyIdxs> txToKeyIdxsMap = new HashMap<>();
    int idx = 0;
    for (Object input : inputs) {
        JSONObject inp = (JSONObject) input;
        JSONArray mixins = inp.getJSONArray("mixins");
        for (Object mixin : mixins) {
            JSONObject mi = (JSONObject) mixin;
            String pubKey = mi.getString("public_key");
            String tx = keyTxMap.get(pubKey);
            if (tx != null) {
                if (!txToKeyIdxsMap.containsKey(tx)) {
                    txToKeyIdxsMap.put(tx, new KeyIdxs());
                }
                KeyIdxs ki = txToKeyIdxsMap.get(tx);
                if (ki.keys.contains(pubKey)) { // same key ref'd multiple times in inputs
                    ki.valid = false;
                }
                ki.keys.add(pubKey);
                if (ki.idxs.contains(idx)) { // same txn ref'd multiple times in one input
                    ki.valid = false;
                }
                ki.idxs.add(idx);
            }
        }
        idx++;
    }

    for (Map.Entry<String, KeyIdxs> entry : txToKeyIdxsMap.entrySet()) {
        KeyIdxs ki = entry.getValue();
        if (ki.valid && ki.keys.size() > 1) {
            System.out.println(entry.getKey() + " " + hash + " " + String.join(",", ki.keys));
        }
    }
  }
}