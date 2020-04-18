import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;


// java GetTxTimestamps TxHashesAll
public class GetTxTimestamps {
  private static final String BASE_URL = "http://127.0.0.1:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  public static void main(String[] args) throws Exception {
    String txsFile = args[0];
    parseTxs(txsFile);
  }

  private static void parseTxs(String txsFile) throws Exception {
    System.err.println("Processing txs from file " + txsFile);

    int idx = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(txsFile))) {
      for (String tx; (tx = br.readLine()) != null; ) {
        parseTx(tx);
        idx++;
        if (idx % 1000 == 0) {
            System.err.println("Processed " + idx + " txs.");
        }
      }
    }

    System.err.println("Processed " + idx + " txs from file " + txsFile);
  }

  private static void parseTx(String tx) throws Exception {
    String url = BASE_URL + "transaction/" + tx;
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build();
    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    int tsStart = data.indexOf("timestamp\"");
    System.out.println(data.substring(tsStart + 11, tsStart + 21));
  }
}