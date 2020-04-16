import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetTxOutputs {
  private static final String BASE_URL = "http://127.0.0.1:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  public static void main(String[] args) throws Exception {
    int start = Integer.parseInt(args[0]);
    int end = Integer.parseInt(args[1]);

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
    JSONArray txs = new JSONArray(data.substring(txsStart + 5, data.indexOf("]", txsStart) + 1));
    JSONObject currTx;
    for (Object tx : txs) {
        currTx = (JSONObject) tx;
        if (currTx.getBoolean("coinbase")) {
            continue;
        }

        parseTx(currTx.getString("tx_hash"));
    }
  }

  private static void parseTx(String hash) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(BASE_URL + "transaction/" + hash))
            .build();

    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

    int outputStart = data.indexOf("outputs\":[{");
    String outputs = data.substring(outputStart, data.indexOf("]", outputStart));
    int currIdx = 0;
    List<String> keys = new ArrayList<>();
    while (true) {
        currIdx = outputs.indexOf("public_key", currIdx);
        if (currIdx == -1) {
            break;
        }
        keys.add(outputs.substring(currIdx + 13, currIdx + 77));
        currIdx = currIdx + 77;
    }
    if (keys.size() > 1) {
        System.out.println(hash + " " +  String.join(",", keys));
    }
  }
}