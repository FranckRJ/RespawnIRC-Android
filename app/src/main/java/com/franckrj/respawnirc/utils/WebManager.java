package com.franckrj.respawnirc.utils;

import static java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class WebManager {
    public static final String userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0";

    /**
     * En cas d'erreur, cette variable est renseignée avec l'ID strings.xml de l'erreur.
     */
    public static int errorStringId = 0;

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
        int errorCode = 0;

        try {
            URL urlToPage;
            InputStream inputStream;
            StringBuilder buffer = new StringBuilder();
            String line;
            String cookie, cfBm, cfClearance;

            if (requestMethod.equals("GET") && !requestParameters.isEmpty()) {
                linkToPage = linkToPage + "?" + requestParameters;
            }

            urlToPage = new URL(linkToPage);
            urlConnection = (HttpURLConnection) urlToPage.openConnection();
            currentInfos.currentUrl = urlConnection.getURL().toString();

            urlConnection.setInstanceFollowRedirects(currentInfos.followRedirects);

            if (currentInfos.useBiggerTimeoutTime) {
                urlConnection.setConnectTimeout(15_000);
                urlConnection.setReadTimeout(15_000);
            } else {
                urlConnection.setConnectTimeout(5_000);
                urlConnection.setReadTimeout(5_000);
            }

            /* On nettoie nos cookies potentiellement expirés. */
            Utils.cleanExpiredCookies();

            // On ajoute les cookies...
            cookie = Utils.buildCloudflareCookieString();
            if(cookie.isEmpty()) {
                cookie = currentInfos.cookiesInAString;
            }
            else {
                cookie += "; " + currentInfos.cookiesInAString;
            }

            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setRequestProperty("User-Agent", userAgentString);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Cookie", cookie);

            if (requestMethod.equals("POST")) {
                DataOutputStream wr = null;
                BufferedWriter writer = null;

                if(currentInfos.currentUrl.contains("https://www.jeuxvideo.com/forums/message/") ||
                   currentInfos.currentUrl.contains("https://www.jeuxvideo.com/forums/topic/"))
                {
                    String firstLine = requestParameters.substring(2, requestParameters.indexOf("\r"));

                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestProperty("Accept-Language", "fr");
                    urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + firstLine);
                    urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                    urlConnection.setRequestProperty("Pragma", "no-cache");
                    urlConnection.setRequestProperty("Cache-Control", "no-cache");
                }
                else
                {
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }

                try {
                    urlConnection.setDoOutput(true);
                    urlConnection.setFixedLengthStreamingMode(requestParameters.getBytes().length);

                    wr = new DataOutputStream(urlConnection.getOutputStream());
                    writer = new BufferedWriter(new OutputStreamWriter(wr, StandardCharsets.UTF_8));
                    writer.write(requestParameters);
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

            errorCode = urlConnection.getResponseCode();

            // CloudFlare positionne un en-tête "cf-mitigated" lorsqu'un captcha est déclenché.
            String cfHeader = urlConnection.getHeaderField("cf-mitigated");
            if(cfHeader != null && cfHeader.equals("challenge")) {
                // CloudFlare'd. Pas de chance...
                errorStringId = Utils.handleRequestError(1);
                return null;
            }

            // Le cookie __cf_bm est mis à jour régulièrement via la navigation normale.
            for(int i = 0;; i++) {
                String headerName = urlConnection.getHeaderFieldKey(i);
                String headerValue = urlConnection.getHeaderField(i);
                if(headerName == null && headerValue == null) {
                    break;
                }

                if(headerName != null && headerName.equals("Set-Cookie")) {
                    Utils.saveCloudflareCookies(headerValue, true);
                }
            }


            inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            // Si on est passé ici avec succès alors la requête est OK.
            errorStringId = 0;

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
        } catch (SocketTimeoutException e) {
            errorStringId = Utils.handleRequestError(HTTP_GATEWAY_TIMEOUT);
            return null;
        } catch (IOException e) {
            if(errorCode != 0) {
                errorStringId = Utils.handleRequestError(errorCode);
            }
            else
            {
                errorStringId = Utils.handleRequestError(9999); // Erreur générique.
            }
            return null;
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
