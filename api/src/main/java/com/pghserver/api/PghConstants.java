package com.pghserver.api;

import java.nio.charset.StandardCharsets;

public class PghConstants {
    public static final String HTTP_VERSION = "HTTP/1.1";
    public static final String CRLF = "\r\n";
    public static final byte[] CRLF_UTF8 = CRLF.getBytes(StandardCharsets.UTF_8);
}
