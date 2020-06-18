import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// time java CountDupDestTxs SrcDestTxsRingCTAll
public class CountDupDestTxs {
  private static final Set<String> dups = new HashSet<>();
  private static final Set<String> unique = new HashSet<>();

  public static void main(String[] args) throws Exception {
    String srcDestFile = args[0];
    process(srcDestFile);
  }

  private static void process(String file) throws Exception {
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for (String line; (line = br.readLine()) != null; ) {
        String[] parts = line.split(" ");
        String src = parts[1];
        if (dups.contains(src) || unique.contains(src)) {
          unique.remove(src);
          dups.add(src);
        } else {
          unique.add(src);
        }

        count++;
        if (count % 100 == 0) {
            System.err.println("Processed " + count + " dests.");
        }
      }

      System.err.println("Processed " + count + " dests.");
    }

    System.out.println("Num unique: " + unique.size());
    System.out.println("Num dup: " + dups.size());
  }
}
