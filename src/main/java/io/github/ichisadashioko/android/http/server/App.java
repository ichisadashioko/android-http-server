package io.github.ichisadashioko.android.http.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;

enum RequestParsingState {
    NONE,

    PARSE_REQUEST_METHOD, PARSE_HTTP_VERSION, PARSE_REQUEST_URI, PARSE_HEADER_LINES,

    CR, LF, CRLF, CRLFCR,
}

public class App {
    static int CR = '\r';
    static int LF = '\n';
    static int SP = ' ';

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

                        StringBuilder sb = new StringBuilder();

                        String requestMethod = null;
                        String requestUri = null;
                        String httpVersion = null;

                        RequestParsingState state = RequestParsingState.NONE;
                        int b;
                        int readBytes = 0;

                        ArrayList<Byte> byteList = new ArrayList<>();

                        // read request method
                        state = RequestParsingState.PARSE_REQUEST_METHOD;
                        while (true) {
                            b = is.read();
                            readBytes++;
                            System.out.print((char) b);

                            if (b == SP) {
                                break;
                            } else {
                                // TODO check for valid byte
                                sb.append((char) b);
                            }
                        }

                        requestMethod = sb.toString();

                        sb = new StringBuilder();

                        // read request uri
                        state = RequestParsingState.PARSE_REQUEST_URI;
                        while (true) {
                            b = is.read();
                            readBytes++;
                            System.out.print((char) b);

                            if (b == SP) {
                                break;
                            } else {
                                // TODO check for valid byte
                                sb.append((char) b);
                            }
                        }

                        requestUri = sb.toString();

                        sb = new StringBuilder();

                        // read http version
                        state = RequestParsingState.PARSE_HTTP_VERSION;

                        boolean isEndedWithLF = false;

                        while (true) {
                            b = is.read();
                            readBytes++;
                            System.out.print((char) b);

                            if (b == LF) {
                                isEndedWithLF = true;
                                break;
                            } else if (b == CR) {
                                break;
                            } else {
                                // TODO check for valid byte
                                sb.append((char) b);
                            }
                        }

                        httpVersion = sb.toString();

                        if (!isEndedWithLF) {
                            b = is.read();
                            readBytes++;
                            System.out.print((char) b);
                            if (b != LF) {
                                throw new Exception("The first line of request message does not end with CRLF!");
                            }
                        }

                        state = RequestParsingState.PARSE_HEADER_LINES;

                        while (true) {
                            // TODO blocking call
                            b = is.read();
                            readBytes++;
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

                        System.out.println("Method: " + requestMethod);
                        System.out.println("Request-URI: " + requestUri);
                        System.out.println("HTTP-Version: " + httpVersion);
                        System.out.println("number of bytes read: " + readBytes);

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
