package com.translation.util;

import java.io.*;

public class ConvertUtil {
    public static ByteArrayOutputStream toOutputStream(InputStream in) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = in.read()) != -1) {
            outputStream.write(ch);
        }
        return outputStream;
    }

    //outputStream转inputStream
    public static ByteArrayInputStream toInputStream(OutputStream out) {
        ByteArrayOutputStream outputStream = (ByteArrayOutputStream) out;
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public static void toOutputStream(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
