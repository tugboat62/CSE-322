import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static final int PORT = 5051;

    public static void main(String[] args) throws IOException {
        while (true) {
            Socket socket = new Socket("localhost", PORT);

            System.out.println("Please enter a file name : ");
            Scanner sc = new Scanner(System.in);
            String fileName = sc.next();

            new ClientWorker(socket, fileName);
        }

    }
}

class ClientWorker implements Runnable {
    Socket socket;
    OutputStream out;
    InputStream in;
    Thread t;
    String fileName;

    public ClientWorker(Socket socket, String fileName) throws IOException {
        this.socket = socket;
        this.fileName = fileName;
        this.in = this.socket.getInputStream();
        this.out = this.socket.getOutputStream();
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        PrintWriter pr = new PrintWriter(out);

        try {
            File file = new File(fileName);

            if (file.exists()) {
                String input = "UPLOAD " + fileName;
                pr.write(input);
                pr.write("\r\n");
                pr.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(this.in));

                String gotit;
                gotit = br.readLine();

                if (gotit.equals("OK")) {
                    byte[] myChunk = new byte[1024];
                    int count;

                    BufferedInputStream newin = new BufferedInputStream(new FileInputStream(file));

                    while ((count = newin.read(myChunk)) > 0) {
                        out.write(myChunk, 0, count);
                        out.flush();
                    }

                    newin.close();
                    out.close();
                    socket.close();
                }
            } else {
                System.out.println(fileName + " NOT FOUND");
                String input = "NO " + fileName;
                pr.write(input);
                pr.write("\r\n");
                pr.flush();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
