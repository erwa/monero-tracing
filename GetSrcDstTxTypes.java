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


// java GetSrcDstTxTypes SrcDestTxsRingCTAllFiltered
// SrcDestTxsRingCTAllFiltered has format
// src_hash dest_hash txo1 txo2 ...
public class GetSrcDstTxTypes {

  public static void main(String[] args) throws Exception {
    String srcDestFile = args[0];
    process(srcDestFile);
  }

  private static void process(String srcDestFile) throws Exception {
    System.err.println("Reading from " + srcDestFile);

    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(srcDestFile))) {
      for (String line; (line = br.readLine()) != null; ) {
        // each line is "src_hash dest_hash txo1 txo2 ..."
        String[] parts = line.split(" ");
        String src = parts[0];
        String dst = parts[1];

        int srcType = getType(src);
        int dstType = getType(dst);

        System.out.println(srcType + " " + dstType);

        count++;
        if (count % 100 == 0) {
          System.err.println("Processed " + count + " lines from " + srcDestFile);
        }
      }
    }
  }

  private static final String BASE_URL = "http://localhost:8081/api/";

  // one instance, reuse
  private static final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();


  private static int getType(String tx) throws Exception {
    String url = BASE_URL + "transaction/" + tx;
    HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build();

    String data = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

    // find tx_version
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
    // clear -> hidden
    return 2;
  }
}
