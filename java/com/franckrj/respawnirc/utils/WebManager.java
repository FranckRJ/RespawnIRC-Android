package com.franckrj.respawnirc.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class WebManager {
    private static String userAgentString = "ResdroidIRC";

    public static void generateNewUserAgent() {
        String newUserAgent = "";
        String baseForUserAgent = "RespawnIRC jeuxvideo.com bonjour logiciel";
        Random rand = new Random();
        int newSize = rand.nextInt(20) + 30;

        for (int i = 0; i < newSize; ++i) {
            newUserAgent += baseForUserAgent.charAt(rand.nextInt(baseForUserAgent.length()));
        }

        userAgentString = newUserAgent;
    }

    public static String sendRequest(String linkToPage, String requestMethod, String requestParameters, String cookiesInAString, WebInfos currentInfos) {
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
            currentInfos.currentUrl = urlConnection.getURL().toString();

            urlConnection.setInstanceFollowRedirects(currentInfos.followRedirects);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(7500);

            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setRequestProperty("User-Agent", userAgentString);
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

            if (currentInfos.followRedirects) {
                if (currentInfos.currentUrl.equals(urlConnection.getURL().toString())) {
                    currentInfos.currentUrl = "";
                } else {
                    currentInfos.currentUrl = urlConnection.getURL().toString();
                }
            } else {
                currentInfos.currentUrl = urlConnection.getHeaderField("Location");
            }

            if (currentInfos.currentUrl == null) {
                currentInfos.currentUrl = "";
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

    public static class WebInfos {
        public boolean followRedirects = false;
        public String currentUrl = "";
    }
}
