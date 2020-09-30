package io.github.ichisadashioko.android.http.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.HashMap;

enum RequestParsingState {
    DEAD,

    PARSE_REQUEST_METHOD, PARSE_HTTP_VERSION, PARSE_REQUEST_URI, PARSE_HEADER_FIELDS_KEY, PARSE_HEADER_FIELDS_VALUE,

    CR, LF, CRLF, CRLFCR,
}

enum ParseLWSState {
    STARTED, ENDED, DEAD,

    CR, NO_MORE_NEWLINE,

    SP_OR_HT,
}

class HeaderField {
    public String name;
    public String value;

    public HeaderField(String name, String value) {
        this.name = name;
        this.value = value;
    }
}

public class App {
    static int CR = '\r';
    static int LF = '\n';
    static int SP = ' ';
    static int HT = '\t';

    // https://tools.ietf.org/html/rfc7230#page-84
    public static boolean IsRFC7230_tchar(int ch) {
        if ((ch > 47) && (ch < 58)) {
            // numbers
            return true;
        }

        if ((ch > 64) && (ch < 91)) {
            // upper-case letters
            return true;
        }

        if ((ch > 96) && (ch < 123)) {
            return true;
        }

        char[] cs = { '!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|', '~' };

        for (int i = 0; i < cs.length; i++) {
            if (ch == cs[i]) {
                return true;
            }
        }

        return false;
    }

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

                        RequestParsingState state = RequestParsingState.DEAD;
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

                        HashMap<String, String> header = new HashMap<>();

                        while (true) {
                            String fieldKey;
                            String fieldValue;

                            sb = new StringBuilder();

                            state = RequestParsingState.PARSE_HEADER_FIELDS_KEY;

                            while (true) {
                                b = is.read();
                                readBytes++;
                                System.out.print((char) b);

                                if (b == ':') {
                                    break;
                                } else {
                                    sb.append((char) b);
                                }
                            }

                            fieldKey = sb.toString();

                            sb = new StringBuilder();

                            ParseLWSState parseLWSState = ParseLWSState.STARTED;

                            while (true) {
                                b = is.read();
                                readBytes++;
                                System.out.print((char) b);

                                if (parseLWSState == ParseLWSState.STARTED) {
                                    if (b == CR) {
                                        parseLWSState = ParseLWSState.CR;
                                    } else if (b == LF) {
                                        parseLWSState = ParseLWSState.NO_MORE_NEWLINE;
                                    }else if(b == SP){
                                        parseLWSState = ParseLWSState.SP_OR_HT;
                                    }else if(b == HT){
                                        parseLWSState = ParseLWSState.SP_OR_HT;
                                    }else{
                                        // TODO dead?
                                        parseLWSState = ParseLWSState.ENDED;
                                    }
                                }
                            }

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
                                    // end of request header fields
                                    break;
                                }
                            } else if (b < 1) {
                                break;
                            } else {
                                state = RequestParsingState.DEAD;
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
}}
