import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;


public class GetAllInputKeys {
  private static final String BASE_URL = "http://127.0.0.1:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

  public static void main(String[] args) throws Exception {
    String file = args[0];

    System.err.println("Getting all input keys from txs in file " + file);

    int count = 0;
    int i = 0;
    Set<String> inputKeys = new HashSet<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String tx; (tx = br.readLine()) != null; ) {
        parseTx(tx, inputKeys);
        i++;
        if (i % 1000 == 0) {
            System.err.println("Processed " + i + " transactions.");
        }
      }
    }

    System.err.println("Total number of input keys from txs in file " + file + ": " + inputKeys.size());

    for (String key : inputKeys) {
        System.out.println(key);
    }

    System.err.println("Total number of input keys from txs in file " + file + ": " + inputKeys.size());
  }

  private static void parseTx(String tx, Set<String> inputKeys) throws Exception {
    String url = BASE_URL + "transaction/" + tx;
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build();
    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    int inputStart = data.indexOf("inputs\":[{");
    int inputEnd = data.indexOf("}]}]", inputStart);
    String inputs = data.substring(inputStart, inputEnd);

    int currIdx = 0;
    while (true) {
        currIdx = inputs.indexOf("public_key", currIdx);
        if (currIdx == -1) {
            break;
        }
        inputKeys.add(inputs.substring(currIdx + 13, currIdx + 77));
        currIdx += 77;
    }
  }
}