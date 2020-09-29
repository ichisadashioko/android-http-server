package io.github.ichisadashioko.android.http.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;

enum RequestParsingState {
    NONE,

    PARSE_REQUEST_METHOD,
    PARSE_HTTP_VERSION,
    PARSE_HEADER_LINE,

    CR, LF, CRLF, CRLFCR,
}

public class App {
    static int CR = '\r';
    static int LF = '\n';

    public static void main(String[] args) throws Exception {

        int port = 9090;
        ServerSocket serverSocket = new ServerSocket(9090);

        System.out.println("Starting server at http://localhost:" + port);

        while (true) {
            Socket socket = serverSocket.accept();

            (new Thread() {
                public void run() {
                    try {
                        OutputStream os = socket.getOutputStream();
                        InputStream is = socket.getInputStream();

                        RequestParsingState state = RequestParsingState.NONE;
                        int b;

                        ArrayList<Byte> byteList = new ArrayList<>();

                        while (true) {
                            // TODO blocking call
                            b = is.read();
                            System.out.print((char) b);

                            // TODO read and parse headers

                            if (b == CR) {
                                if (state == RequestParsingState.CRLF) {
                                    state = RequestParsingState.CRLFCR;
                                } else {
                                    state = RequestParsingState.CR;
                                }
                            } else if (b == LF) {
                                if (state == RequestParsingState.CR) {
                                    state = RequestParsingState.CRLF;
                                } else if (state == RequestParsingState.CRLFCR) {
                                    // end of request headers
                                    break;
                                }
                            } else if (b < 1) {
                                break;
                            } else {
                                state = RequestParsingState.NONE;
                            }
                        }

                        is.close();
                        os.close();
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }).start();
        }
    }
}
