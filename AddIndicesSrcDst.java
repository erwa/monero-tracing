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

// time java -cp *:. AddIndicesSrcDst SrcDestTxsRingCTAllFiltered3 > SrcDestTxsRingCTAllFilteredIndices3
// Outputs on each line:
//   src dst idx1,key1 idx2,key2 ... (all using integers)
public class AddIndicesSrcDst {

  public static void main(String[] args) throws Exception {
    String srcDstFile = args[0];

    process(srcDstFile);
  }

  private static void process(String file) throws Exception {
    Map<String, Integer> keyMap = Utils.loadKeyMap();
    Map<String, Integer> txMap = Utils.loadTxMap();

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        String src = parts[0];
        String dst = parts[1];
        // keys must be comma-separated!
        Set<String> keys = new HashSet<>(Arrays.asList(parts[2].split(",")));

        parseTx(src, dst, keys, keyMap, txMap);

        count++;
        if (count % 100 == 0) {
            System.err.println("Processed " + count + " dests.");
        }
      }

      System.err.println("Processed " + count + " dests.");
    }
  }

  private static final String BASE_URL = "http://127.0.0.1:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  // keys are txo hashes
  // goal is to find the input indices of the keys
  // At this point, S1-S5 have already been applied,
  // allowing our logic to be simpler (we don't have to worry about
  // printing the wrong index since if there were a conflict, it would
  // have been filtered out by S1-S5).
  // Everything printed as integers
  private static void parseTx(String src, String dst, Set<String> keys, Map<String, Integer> keyMap, Map<String, Integer> txMap) throws Exception {
    System.out.print(txMap.get(src) + " " + txMap.get(dst));

    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(BASE_URL + "transaction/" + dst))
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
                System.out.print(" " + idx + "," + keyMap.get(pubKey));
                found++;
                break;
            }
        }

        if (found == keys.size()) {
            break;
        }
    }
    System.out.println();
  }
}
