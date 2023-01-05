import java.io.File;

public class Demo {
    public static void main(String... args) {
        File dir = new File("root");
        showFiles(dir.listFiles());
    }

    public static void showFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Directory: " + file.getAbsolutePath());
                showFiles(file.listFiles()); // Calls same method again.
            } else {
                System.out.println("File: " + file.getAbsolutePath());
            }
        }
    }
}
