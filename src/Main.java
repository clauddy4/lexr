import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Main
{
    private static final String FILE_PATH = "\\input.txt";
    static int[] coordinates = new int[]{1,0}; // [0] is line, [1] is position
    static HashMap<Integer, Integer> coordinatesArr = new HashMap<>();
    public static void checkNavigation(char ch) {
        if (ch == '\n') {
            coordinates[0]++;
            coordinates[1] = 0;
            coordinatesArr.put(coordinates[0], coordinates[1]);
        }
        else {
            coordinates[1]++;
            coordinatesArr.replace(coordinates[0], coordinates[1]);
        }
    }

    public static void main(String[] args)
    {
        Path currentRelativePath = Paths.get("");
        String filePath = currentRelativePath.toAbsolutePath().toString() + FILE_PATH;
        String handler = "";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String st;
            while ((st = br.readLine()) != null) {
                handler += st + "\n";
            }
            Lexer lexer = new Lexer();
            for (Character ch : handler.toLowerCase().toCharArray()) {
                checkNavigation(ch);
                lexer.makeIteration(ch, coordinates);
            }
            lexer.output(coordinatesArr);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}