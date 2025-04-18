package ui;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ResponseException extends RuntimeException {
    private final int code;

    public ResponseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
