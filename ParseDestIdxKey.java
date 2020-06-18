import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

// time java -cp *:. ParseDestIdxKey InputKeysAll TxHashesAll SrcDestTxsRingCTAll
// time java -cp *:. ParseDestIdxKey InputKeysAll TxHashesAll SrcDestTxsRingCTAllFiltered
// Outputs on each line: dst idx key (all using integers)
// This is needed so we can check whether merging output heuristic is correct.
// 1 hr 5 min (for SrcDestTxsRingCTAll (non-filtered))
public class ParseDestIdxKey {
  private static final Map<String, Integer> keyMap = new HashMap<>();
  private static final Map<String, Integer> txMap = new HashMap<>();

  private static final String BASE_URL = "http://127.0.0.1:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  private static final void loadKeyMap(String file) throws Exception {
    System.err.println("loading keys from " + file);

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        keyMap.put(line, count);
        count++;
        if (count % 10000 == 0) {
            System.err.println("Processed " + count + " keys.");
        }
      }

      System.err.println("Done loading keyMap. Size: " + keyMap.size());
      System.err.println("Count = " + count);
    }
  }

  private static final void loadTxMap(String file) throws Exception {
    System.err.println("loading txs from " + file);

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        txMap.put(line, count);
        count++;
        if (count % 10000 == 0) {
            System.err.println("Processed " + count + " keys.");
        }
      }

      System.err.println("Done loading txMap. Size: " + keyMap.size());
      System.err.println("Count = " + count);
    }
  }

  public static void main(String[] args) throws Exception {
    String keyMapFile = args[0];
    String txFile = args[1];
    String srcDestFile = args[2];

    loadKeyMap(keyMapFile);
    loadTxMap(txFile);
    process(srcDestFile);
  }

  private static void process(String file) throws Exception {
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        String dst = parts[1];
        // keys must be comma-separated!
        Set<String> keys = new HashSet<>(Arrays.asList(parts[2].split(",")));

        parseTx(dst, keys);

        count++;
        if (count % 100 == 0) {
            System.err.println("Processed " + count + " dests.");
        }
      }

      System.err.println("Processed " + count + " dests.");
    }
  }

  // hash is dst, keys are txo hashes
  private static void parseTx(String hash, Set<String> keys) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(BASE_URL + "transaction/" + hash))
            .build();

    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

    int inputStart = data.indexOf("inputs\":[{");
    int inputEnd = data.indexOf("}]}]", inputStart);
    JSONArray inputs = JSON.parseArray(data.substring(inputStart + 8, inputEnd + 4));

    int found = 0;
    for (int idx = 0; idx < inputs.size(); idx++) {
        JSONObject inp = inputs.getJSONObject(idx);
        JSONArray mixins = inp.getJSONArray("mixins");
        for (Object mixin : mixins) {
            JSONObject mi = (JSONObject) mixin;
            String pubKey = mi.getString("public_key");
            if (keys.contains(pubKey)) { // found a real input
                // print and break, to continue onto next input
                System.out.println(txMap.get(hash) + " "
                    + idx + " " + keyMap.get(pubKey));
                found++;
                break;
            }
        }

        if (found == keys.size()) {
            break;
        }
    }
  }
}