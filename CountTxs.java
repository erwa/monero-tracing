import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


public class CountTxs {
  private static final String BASE_URL = "http://127.0.0.1:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  public static void main(String[] args) throws Exception {
    int start = Integer.parseInt(args[0]);
    int end = Integer.parseInt(args[1]);

    System.err.println("Count total number of txs in blocks in range [" + start + ", " + end + ")");

    int count = 0;
    for (int i = start; i < end; i++) {
        if (i % 100 == 0) {
            System.err.println("Processing block " + i);
        }
        count += parseBlock(i);
    }

    System.out.println("Total number of non-coinbase txs in blocks in [" + start + ", " + end + "): "
        + count);
  }

  private static int parseBlock(int block) throws Exception {
    String url = BASE_URL + "block/" + block;
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build();
    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    int txsStart = data.indexOf("txs\":[{");
    String txs = data.substring(txsStart + 5, data.indexOf("]", txsStart) + 1);
    int count = 0;
    for (int i = 0; i < txs.length(); i++) {
        if (txs.charAt(i) == '{') {
            count++;
        }
    }

    // ignore coinbase txn
    return count - 1;
  }
}