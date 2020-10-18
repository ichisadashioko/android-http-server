package io.github.ichisadashioko.android.http.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class App {

    public static int MAXIMUM_REQUEST_MSG_SIZE = 1024 * 5;
    public static final int CR = '\r';
    public static final int LF = '\n';
    public static final int SP = ' ';
    public static final int HT = '\t';

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

                            // TODO parse input stream

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
