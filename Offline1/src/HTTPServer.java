import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;

public class HTTPServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5051);
        try {
            FileWriter fw = new FileWriter("log.txt", false);
            PrintWriter pw = new PrintWriter(fw, false);
            pw.flush();
            pw.close();
            fw.close();
        } catch (Exception exception) {
            System.out.println("Exception caught");
        }
        while (true) {
            FileWriter logFile = new FileWriter("log.txt", true);
            Socket clientSocket = serverSocket.accept();
            new HTTPClientHandler(clientSocket, logFile);
        }
    }
}

class HTTPClientHandler implements Runnable {
    Socket clientSocket;
    OutputStream out;
    InputStream in;
    Thread t;
    FileWriter logFile;

    HTTPClientHandler(Socket socket, FileWriter logFile) throws IOException {
        this.clientSocket = socket;
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
        this.logFile = logFile;
        t = new Thread(this);
        t.start();
    }

    public void run() {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.in));
            PrintWriter pr = new PrintWriter(clientSocket.getOutputStream());

            String type;
            String request = br.readLine();
            String content;
            if (request != null && request.startsWith("GET")) {
                System.out.println(request);
                logFile.write(request);
                logFile.write("\n");
                logFile.write("Response:");
                logFile.write("\n");
                int first = request.indexOf("/");
                first++;
                StringBuilder sb = new StringBuilder();
                while (request.charAt(first) != ' ') {
                    sb.append(request.charAt(first));
                    first++;
                }
                String fileName = sb.toString();
                if (fileName.contains(".") && fileName.contains("root")) {
                    File file = new File(fileName);
                    pr.write("HTTP/1.1 200 OK\r\n");
                    logFile.write("HTTP/1.1 200 OK\r\n");
                    pr.write("Server: Java HTTP Server: 1.1\r\n");
                    logFile.write("Server: Java HTTP Server: 1.1\r\n");
                    pr.write("Date: " + new Date() + "\r\n");
                    logFile.write("Date: " + new Date() + "\r\n");
                    if (fileName.contains(".jpg") || fileName.contains(".png")) {
                        byte[] bytes = Files.readAllBytes(file.getAbsoluteFile().toPath());

                        String base64EncodedImageBytes = Base64.getEncoder().encodeToString(bytes);
                        pr.write("Content-type: html\r\n");
                        logFile.write("Content-type: image/jpeg\r\n");
                        content = "<html><img src=\"data:image/extension;base64," + base64EncodedImageBytes +
                                "\" width=\"1000\" height=\"500\"/></html>";
                        pr.write("Content-Length: " + content.getBytes().length + "\r\n");
                        logFile.write("Content-Length: " + file.length() + "\r\n");
                        logFile.write("\n");
                        pr.write("\r\n");
                        pr.write(content);
                        pr.flush();
                    } else if (fileName.contains(".txt")) {
                        content = readFileData(file, (int) file.length());
                        pr.write("Content-type: text/plain" + "\r\n");
                        logFile.write("Content-type: text/plain" + "\r\n");
                        pr.write("Content-Length: " + content.length() + "\r\n");
                        logFile.write("Content-Length: " + content.length() + "\r\n");
                        logFile.write("\n");
                        pr.write("\r\n");
                        pr.write(content);
                        pr.flush();
                    } else {
                        pr.write("Content-type: application/x-force-download" + "\r\n");
                        logFile.write("Content-type: application/x-force-download" + "\r\n");
                        pr.write("Content-Length: " + file.length() + "\r\n");
                        logFile.write("Content-Length: " + file.length() + "\r\n");
                        logFile.write("\n");
                        pr.write("\r\n");
//                        pr.write(file.toString());
                        pr.flush();
                        send(fileName);
                    }
                } else {
                    type = "html";
                    if (fileName.equals("")) {
                        fileName = "root";
                        pr.write("HTTP/1.1 200 OK\r\n");
                        logFile.write("HTTP/1.1 200 OK\r\n");
                        content = "<html><b><i><a href=\"" + fileName + "\">root</a></i></b></html>";
                    } else {
                        try {
                            File[] files = new File(fileName).listFiles();
                            content = "<html>";
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    String dirName = file.getName();
                                    String path = file.getParentFile().getName();
                                    content += "<b><i><a href=\"" + path + "/" + dirName + "\">" + dirName + "</a></i></b><br>";
                                } else {
                                    String fileName1 = file.getName();
                                    String path = file.getParentFile().getName();
                                    content += "<a href=\"" + path + "/" + fileName1 + "\">" + fileName1 + "</a><br>";
                                }
                            }
                            content += "</html>";
                            pr.write("HTTP/1.1 200 OK\r\n");
                            logFile.write("HTTP/1.1 200 OK\r\n");
                        } catch (NullPointerException e) {
                            System.out.println("Exception caught");
                            pr.write("HTTP/1.1 404 Not Found\r\n");
                            logFile.write("HTTP/1.1 404 Not Found\r\n");
                            content = "<html><b><i>404 Not Found</i></b></html>";
                        }
                    }
                    pr.write("Server: Java HTTP Server: 1.1\r\n");
                    logFile.write("Server: Java HTTP Server: 1.1\r\n");

                    pr.write("Date: " + new Date() + "\r\n");
                    logFile.write("Date: " + new Date() + "\r\n");
                    pr.write("Content-Type: " + type + "\r\n");
                    logFile.write("Content-Type: " + type + "\r\n");
                    pr.write("Content-Length: " + content.length() + "\r\n");
                    logFile.write("Content-Length: " + content.length() + "\r\n");
                    logFile.write("\n");
                    pr.write("\r\n");
                    pr.write(content);
                    pr.flush();
                }
            } else if (request != null && request.startsWith("UPLOAD")) {
                String[] files = request.split(" ");
                System.out.println(request);
                logFile.write(request);
                logFile.write("\n");
                logFile.write("Response:");
                logFile.write("\n");
                if (files.length != 2) {
                    pr.write("HTTP/1.1 400 Bad Request\r\n");
                    logFile.write("HTTP/1.1 400 Bad Request\r\n");
                    pr.write("Server: Java HTTP Server: 1.1\r\n");
                    logFile.write("Server: Java HTTP Server: 1.1\r\n");
                    pr.write("Date: " + new Date() + "\r\n");
                    logFile.write("Date: " + new Date() + "\r\n");
                    pr.write("Content-Type: " + "text" + "\r\n");
                    logFile.write("Content-Type: " + "text" + "\r\n");
                    pr.write("Content-Length: " + 0 + "\r\n");
                    logFile.write("Content-Length: " + 0 + "\r\n");
                    logFile.write("\n");
                    pr.write("\r\n");
                    pr.flush();
                } else {
                    if (files[1].contains(".txt") || files[1].contains(".jpg") || files[1].contains(".png") || files[1].contains(".mp4")) {
                        pr.write("OK");
                        logFile.write("OK");
                        logFile.write("\n");
                        pr.write("\r\n");
                        pr.flush();

                        String workingDir = System.getProperty("user.dir");
                        String absoluteFilePath = workingDir + "/root/" + files[1];
                        File file = new File(absoluteFilePath);

                        byte[] myChunk = new byte[1024];
                        int count;

                        FileOutputStream fos = new FileOutputStream(file);

                        while ((count = in.read(myChunk)) > 0) {
                            fos.write(myChunk, 0, count);
                            fos.flush();
                        }
                        fos.close();
                        in.close();
                        logFile.write("File uploaded successfully\n");
                        logFile.write("File name: "+files[1]+"\n");
                        logFile.write("File size: "+file.length()+"\n\n");
                    } else {
                        pr.write("HTTP/1.1 400 Bad Request\r\n");
                        logFile.write("HTTP/1.1 400 Bad Request\r\n");
                        pr.write("Server: Java HTTP Server: 1.1\r\n");
                        logFile.write("Server: Java HTTP Server: 1.1\r\n");
                        pr.write("Date: " + new Date() + "\r\n");
                        logFile.write("Date: " + new Date() + "\r\n");
                        pr.write("Content-Type: " + "application/big-data" + "\r\n");
                        logFile.write("Content-Type: " + "application/big-data" + "\r\n");
                        pr.write("Content-Length: too big\r\n");
                        logFile.write("Content-Length: too big\r\n");
                        logFile.write("\n");
                        pr.write("\r\n");
                        pr.flush();
                    }
                }
            }
            clientSocket.close();
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String readFileData(File file, int length) {
        char[] charArray = new char[length];
        try {
            FileReader fileReader = new FileReader(file);
            fileReader.read(charArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(charArray);
    }

    private void send(String path) throws IOException, InterruptedException {
        byte[] myChunk = new byte[1024];
        int count;
        File fileCheck = new File(path);
        BufferedInputStream newin = new BufferedInputStream(new FileInputStream(fileCheck));
        out.write((int) fileCheck.length());
        while ((count = newin.read(myChunk)) > 0) {
            out.write(myChunk, 0, count);
            out.flush();
            Thread.sleep(100);
        }
    }
}