package com.franckrj.respawnirc.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;

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

    public static String sendRequestWithMultipleTrys(String linkToPage, String requestMethod, String requestParameters, WebInfos currentInfos, int maxNumberOfTrys) {
        int numberOfTrys = 0;
        String pageContent;

        do {
            ++numberOfTrys;
            pageContent = sendRequest(linkToPage, requestMethod, requestParameters, currentInfos);

            try {
                if (currentInfos.isCancelled != null) {
                    if (currentInfos.isCancelled.call()) {
                        break;
                    }
                }
            } catch (Exception e) {
                break;
            }
        } while (currentInfos.currentUrl.equals(linkToPage) && numberOfTrys < maxNumberOfTrys);

        return pageContent;
    }

    public static String sendRequest(String linkToPage, String requestMethod, String requestParameters, WebInfos currentInfos) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL urlToPage;
            InputStream inputStream;
            StringBuilder buffer = new StringBuilder();
            String line;

            if (requestMethod.equals("GET") && !requestParameters.isEmpty()) {
                linkToPage = linkToPage + "?"+ requestParameters;
            }

            urlToPage = new URL(linkToPage);
            urlConnection = (HttpURLConnection) urlToPage.openConnection();
            currentInfos.currentUrl = urlConnection.getURL().toString();

            urlConnection.setInstanceFollowRedirects(currentInfos.followRedirects);

            if (currentInfos.useBiggerTimeoutTime) {
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);
            } else {
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
            }

            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setRequestProperty("User-Agent", userAgentString);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Cookie", currentInfos.cookiesInAString);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (requestMethod.equals("POST")) {
                DataOutputStream writer = null;

                try {
                    urlConnection.setDoOutput(true);
                    urlConnection.setFixedLengthStreamingMode(requestParameters.getBytes().length);

                    writer = new DataOutputStream(urlConnection.getOutputStream());
                    writer.writeBytes(requestParameters);
                    writer.flush();
                } catch (Exception e) {
                    //rien
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (Exception e) {
                            //rien
                        }
                    }
                }
            }

            inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");

                if (currentInfos.isCancelled != null) {
                    if (currentInfos.isCancelled.call()) {
                        return null;
                    }
                }
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
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    //rien
                }
            }
        }
    }

    public static class WebInfos {
        public Callable<Boolean> isCancelled = null;
        public boolean followRedirects = false;
        public String currentUrl = "";
        public String cookiesInAString = "";
        public boolean useBiggerTimeoutTime = true;
    }
}
