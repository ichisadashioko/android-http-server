package io.github.ichisadashioko.android.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class HeaderField {
    public String name;
    public String value;

    public HeaderField(String name, String value) {
        this.name = name;
        this.value = value;
    }
}

public class App {

    static final int MAXIMUM_REQUEST_MSG_SIZE = 1024 * 5;

    public static void main(String[] args) throws Exception {

        int port = 9090;
        ServerSocket serverSocket = null;

        try {

            serverSocket = new ServerSocket(9090);

            System.out.println("Starting server at http://localhost:" + port);

            while (true) {
                Socket socket = serverSocket.accept();

                (new Thread() {
                    public void run() {
                        try {
                            OutputStream os = socket.getOutputStream();
                            InputStream is = socket.getInputStream();

                            int expected = MAXIMUM_REQUEST_MSG_SIZE;

                            StringBuilder sb = new StringBuilder();

                            // TODO implement parsing on the fly for Content-Length header field
                            while (expected > 0) {
                                int ch = -1;
                                try {
                                    ch = is.read();
                                } catch (IOException e) {
                                    e.printStackTrace(System.err);
                                    ch = -1;
                                }

                                // Control character or EOF (-1) terminates loop
                                if ((ch < 32) || (ch == 127)) {
                                    break;
                                }

                                sb.append((char) ch);
                                expected--;
                            }

                            System.out.println("ts: " + System.currentTimeMillis());
                            System.out.println("requestAddress: " + socket.getInetAddress());

                            System.out.println(sb.toString());

                            is.close();
                            os.close();
                        } catch (Exception ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (serverSocket != null) {
                if (serverSocket.isClosed()) {
                    serverSocket.close();
                }
            }
        }
    }
}
