import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


// javac -cp .:commons-lang3-3.10/* CountTxs2.java
// java -cp .:commons-lang3-3.10/* CountTxs2 1220517 2077095
public class CountTxs2 {
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

    // ignore coinbase txn
    return StringUtils.countMatches(data, "coinbase") - 1;
  }
}