import java.io.File;
import java.io.IOException;

public class FileDirectories {
    public static void main(String[] args) throws IOException {
        File[] files = new File("root").listFiles();
        showFiles(files);
    }

    public static void showFiles(File[] files) {
        try{
            for (File file : files) {
                if (file.isDirectory()) {
                    String fileName = "Directory: " + file.getPath();
                    System.out.println(fileName);
                    showFiles(file.listFiles()); // Calls same method again.
                } else {
                    System.out.println();
                    System.out.println("File: " + file.getName());
                    System.out.println("Path: " + file.getAbsolutePath());
                    System.out.println("Directory: " + file.getParentFile().getName());
                }
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }




        /*Iterator<File> it = FileUtils.iterateFiles(new File("C://Search Files//"), null, false);
        while(it.hasNext()) {
            System.out.println(((File) it.next()).getName());
        }*/
    }
}
