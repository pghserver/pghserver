package com.pghserver.api.type;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record ResponseStatus(int code, @NotNull String string) {
    private static final Map<Integer, String> codeStrings = new HashMap<>();

    static {
        // Informational
        codeStrings.put(100, "Continue");
        codeStrings.put(101, "Switching Protocols");
        codeStrings.put(102, "Processing");
        codeStrings.put(103, "Early Hints");

        // Successful
        codeStrings.put(200, "OK");
        codeStrings.put(201, "Created");
        codeStrings.put(202, "Accepted");
        codeStrings.put(203, "Non-Authoritative Information");
        codeStrings.put(204, "No Content");
        codeStrings.put(205, "Reset Content");
        codeStrings.put(206, "Partial Content");
        codeStrings.put(207, "Multi-Status");
        codeStrings.put(208, "Already Reported");
        codeStrings.put(226, "IM Used");

        // Redirection
        codeStrings.put(300, "Multiple Choices");
        codeStrings.put(301, "Moved Permanently");
        codeStrings.put(302, "Moved Temporarily");
        codeStrings.put(303, "See Other");
        codeStrings.put(304, "Not Modified");
        codeStrings.put(305, "Use Proxy");
        codeStrings.put(307, "Temporary Redirect");
        codeStrings.put(308, "Permanent Redirect");

        // Client error
        codeStrings.put(400, "Bad Request");
        codeStrings.put(401, "Unauthorized");
        codeStrings.put(402, "Payment Required");
        codeStrings.put(403, "Forbidden");
        codeStrings.put(404, "Not Found");
        codeStrings.put(405, "Method Not Allowed");
        codeStrings.put(406, "Not Acceptable");
        codeStrings.put(407, "Proxy Authentication Required");
        codeStrings.put(408, "Request Timeout");
        codeStrings.put(409, "Conflict");
        codeStrings.put(410, "Gone");
        codeStrings.put(411, "Length Required");
        codeStrings.put(412, "Precondition Failed");
        codeStrings.put(413, "Content Too Large");
        codeStrings.put(414, "URI Too Long");
        codeStrings.put(415, "Unsupported Media Type");
        codeStrings.put(416, "Range Not Satisfiable");
        codeStrings.put(417, "Expectation Failed");
        codeStrings.put(418, "I'm a teapot");
        codeStrings.put(421, "Misdirected Request");
        codeStrings.put(422, "Unprocessable Content");
        codeStrings.put(423, "Locked");
        codeStrings.put(424, "Failed Dependency");
        codeStrings.put(425, "Too Early");
        codeStrings.put(426, "Upgrade Required");
        codeStrings.put(428, "Precondition Required");
        codeStrings.put(429, "Too Many Requests");
        codeStrings.put(431, "Request Header Fields Too Large");
        codeStrings.put(451, "Unavailable For Legal Reasons");

        // Server error
        codeStrings.put(500, "Internal Server Error");
        codeStrings.put(501, "Not Implemented");
        codeStrings.put(502, "Bad Gateway");
        codeStrings.put(503, "Service Unavailable");
        codeStrings.put(504, "Gateway Timeout");
        codeStrings.put(505, "HTTP Version Not Supported");
        codeStrings.put(506, "Variant Also Negotiates");
        codeStrings.put(507, "Insufficient Storage");
        codeStrings.put(508, "Loop Detected");
        codeStrings.put(510, "Not Extended");
        codeStrings.put(511, "Network Authentication Required");
    }

    // Informational
    public static final @NotNull ResponseStatus CONTINUE = ofCode(100);
    public static final @NotNull ResponseStatus SWITCHING_PROTOCOLS = ofCode(101);
    public static final @NotNull ResponseStatus PROCESSING = ofCode(102);
    public static final @NotNull ResponseStatus EARLY_HINTS = ofCode(103);

    // Successful
    public static final @NotNull ResponseStatus OK = ofCode(200);
    public static final @NotNull ResponseStatus CREATED = ofCode(201);
    public static final @NotNull ResponseStatus ACCEPTED = ofCode(202);
    public static final @NotNull ResponseStatus NON_AUTHORITATIVE_INFORMATION = ofCode(203);
    public static final @NotNull ResponseStatus NO_CONTENT = ofCode(204);
    public static final @NotNull ResponseStatus RESET_CONTENT = ofCode(205);
    public static final @NotNull ResponseStatus PARTIAL_CONTENT = ofCode(206);
    public static final @NotNull ResponseStatus MULTI_STATUS = ofCode(207);
    public static final @NotNull ResponseStatus ALREADY_REPORTED = ofCode(208);
    public static final @NotNull ResponseStatus IM_USED = ofCode(226);

    // Redirection
    public static final @NotNull ResponseStatus MULTIPLE_CHOICES = ofCode(300);
    public static final @NotNull ResponseStatus MOVED_PERMANENTLY = ofCode(301);
    public static final @NotNull ResponseStatus FOUND = ofCode(302);
    public static final @NotNull ResponseStatus SEE_OTHER = ofCode(303);
    public static final @NotNull ResponseStatus NOT_MODIFIED = ofCode(304);
    public static final @NotNull ResponseStatus USE_PROXY = ofCode(305);
    public static final @NotNull ResponseStatus TEMPORARY_REDIRECT = ofCode(307);
    public static final @NotNull ResponseStatus PERMANENT_REDIRECT = ofCode(308);

    // Client error
    public static final @NotNull ResponseStatus BAD_REQUEST = ofCode(400);
    public static final @NotNull ResponseStatus UNAUTHORIZED = ofCode(401);
    public static final @NotNull ResponseStatus PAYMENT_REQUIRED = ofCode(402);
    public static final @NotNull ResponseStatus FORBIDDEN = ofCode(403);
    public static final @NotNull ResponseStatus NOT_FOUND = ofCode(404);
    public static final @NotNull ResponseStatus METHOD_NOT_ALLOWED = ofCode(405);
    public static final @NotNull ResponseStatus NOT_ACCEPTABLE = ofCode(406);
    public static final @NotNull ResponseStatus PROXY_AUTHENTICATION_REQUIRED = ofCode(407);
    public static final @NotNull ResponseStatus REQUEST_TIMEOUT = ofCode(408);
    public static final @NotNull ResponseStatus CONFLICT = ofCode(409);
    public static final @NotNull ResponseStatus GONE = ofCode(410);
    public static final @NotNull ResponseStatus LENGTH_REQUIRED = ofCode(411);
    public static final @NotNull ResponseStatus PRECONDITION_FAILED = ofCode(412);
    public static final @NotNull ResponseStatus CONTENT_TOO_LARGE = ofCode(413);
    public static final @NotNull ResponseStatus URI_TOO_LONG = ofCode(414);
    public static final @NotNull ResponseStatus UNSUPPORTED_MEDIA_TYPE = ofCode(415);
    public static final @NotNull ResponseStatus RANGE_NOT_SATISFIABLE = ofCode(416);
    public static final @NotNull ResponseStatus EXPECTATION_FAILED = ofCode(417);
    public static final @NotNull ResponseStatus IM_A_TEAPOT = ofCode(418);
    public static final @NotNull ResponseStatus MISDIRECTED_REQUEST = ofCode(421);
    public static final @NotNull ResponseStatus UNPROCESSABLE_CONTENT = ofCode(422);
    public static final @NotNull ResponseStatus LOCKED = ofCode(423);
    public static final @NotNull ResponseStatus FAILED_DEPENDENCY = ofCode(424);
    public static final @NotNull ResponseStatus TOO_EARLY = ofCode(425);
    public static final @NotNull ResponseStatus UPGRADE_REQUIRED = ofCode(426);
    public static final @NotNull ResponseStatus PRECONDITION_REQUIRED = ofCode(428);
    public static final @NotNull ResponseStatus TOO_MANY_REQUESTS = ofCode(429);
    public static final @NotNull ResponseStatus REQUEST_HEADER_FIELDS_TOO_LARGE = ofCode(431);
    public static final @NotNull ResponseStatus UNAVAILABLE_FOR_LEGAL_REASONS = ofCode(451);

    // Server error
    public static final @NotNull ResponseStatus INTERNAL_SERVER_ERROR = ofCode(500);
    public static final @NotNull ResponseStatus NOT_IMPLEMENTED = ofCode(501);
    public static final @NotNull ResponseStatus BAD_GATEWAY = ofCode(502);
    public static final @NotNull ResponseStatus SERVICE_UNAVAILABLE = ofCode(503);
    public static final @NotNull ResponseStatus GATEWAY_TIMEOUT = ofCode(504);
    public static final @NotNull ResponseStatus HTTP_VERSION_NOT_SUPPORTED = ofCode(505);
    public static final @NotNull ResponseStatus VARIANT_ALSO_NEGOTIATES = ofCode(506);
    public static final @NotNull ResponseStatus INSUFFICIENT_STORAGE = ofCode(507);
    public static final @NotNull ResponseStatus LOOP_DETECTED = ofCode(508);
    public static final @NotNull ResponseStatus NOT_EXTENDED = ofCode(510);
    public static final @NotNull ResponseStatus NETWORK_AUTHENTICATION_REQUIRED = ofCode(511);

    /**
     * Creates response status data from a status code, using a predefined list of documented HTTP status codes.
     *
     * @param code Status code
     * @return Response status data
     */
    public static @NotNull ResponseStatus ofCode(int code) {
        String string = codeStrings.get(code);
        if (string == null) throw new IllegalArgumentException("Undocumented HTTP status code " + code);
        return new ResponseStatus(code, string);
    }

    @Override
    public @NotNull String toString() {
        return code() + " " + string();
    }
}
