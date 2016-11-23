package com.franckrj.respawnirc.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebManager {
    public static String sendRequest(String linkToPage, String requestMethod, String requestParameters, String cookiesInAString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL urlToPage;
            InputStream inputStream;
            StringBuilder buffer;
            String line;

            if (requestMethod.equals("GET") && !requestParameters.isEmpty()) {
                linkToPage = linkToPage + "?"+ requestParameters;
            }

            urlToPage = new URL(linkToPage);
            urlConnection = (HttpURLConnection) urlToPage.openConnection();

            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(10000);

            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setRequestProperty("User-Agent", "RespawnIRC");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Cookie", cookiesInAString);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (requestMethod.equals("POST")) {
                DataOutputStream writer;
                urlConnection.setDoOutput(true);
                urlConnection.setFixedLengthStreamingMode(requestParameters.getBytes().length);

                writer = new DataOutputStream(urlConnection.getOutputStream());
                writer.writeBytes(requestParameters);
                writer.flush();
                writer.close();
            }

            inputStream = urlConnection.getInputStream();
            buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}