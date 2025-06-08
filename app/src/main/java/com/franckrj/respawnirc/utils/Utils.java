package com.franckrj.respawnirc.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.text.Spannable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.ColorInt;
import androidx.emoji.text.EmojiCompat;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.WebBrowserActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Utils {
    public static String colorToString(@ColorInt int colorValue) {
        return String.format("#%06X", 0xFFFFFF & colorValue);
    }

    public static String colorToStringWithAlpha(@ColorInt int colorValue) {
        return String.format("#%08X", colorValue);
    }

    public static int roundToInt(double valToRound) {
        return (int) (valToRound + 0.5);
    }

    public static long roundToLong(double valToRound) {
        return (long) (valToRound + 0.5);
    }

    public static boolean stringsAreEquals(String str1, String str2) {
        return (Objects.equals(str1, str2));
    }

    public static String truncateString(String baseString, int maxSize, String endingPartIfCuted) {
        if (baseString.length() > maxSize) {
            baseString = baseString.substring(0, maxSize - endingPartIfCuted.length()) + endingPartIfCuted;
        }

        return baseString;
    }

    public static boolean stringIsEmptyOrNull(String thisString) {
        //noinspection SimplifiableIfStatement
        if (thisString == null) {
            return true;
        }
        return thisString.isEmpty();
    }

    public static void showSoftKeyboard(Activity forThisActivity) {
        InputMethodManager inputManager = (InputMethodManager) forThisActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void hideSoftKeyboard(Activity fromThisActivity) {
        InputMethodManager inputManager = (InputMethodManager) fromThisActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = fromThisActivity.getCurrentFocus();
        if (inputManager != null && focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void replaceSpanByAnotherSpan(Spannable inThisSpan, Object oldSpan, Object newSpan) {
        int start = inThisSpan.getSpanStart(oldSpan);
        int end = inThisSpan.getSpanEnd(oldSpan);
        int flags = inThisSpan.getSpanFlags(oldSpan);
        inThisSpan.setSpan(newSpan, start, end, flags);
        inThisSpan.removeSpan(oldSpan);
    }

    public static String encodeStringToUrlString(String baseString) {
        try {
            baseString = URLEncoder.encode(baseString, "UTF-8");
        } catch (Exception e) {
            baseString = "";
        }
        return baseString;
    }

    public static String decodeUrlStringToString(String baseString) {
        try {
            baseString = URLDecoder.decode(baseString, "UTF-8");
        } catch (Exception e) {
            baseString = "";
        }
        return baseString;
    }

    public static ArrayList<String> mapStringArrayList(List<String> baseList, StringModifier mapFunction) {
        ArrayList<String> newList = new ArrayList<>();

        for (String currentString : baseList) {
            newList.add(mapFunction.changeString(currentString));
        }

        return newList;
    }

    public static String imageLinkToFileName(String link) {
        if (link.startsWith("https://image.noelshack.com/minis/")) {
            return "nlsk_mini/" + link.substring(("https://image.noelshack.com/minis/").length()).replace("/", "_");
        } else if (link.startsWith("https://image.noelshack.com/fichiers-xs/")) {
            return "nlsk_xs/" + link.substring(("https://image.noelshack.com/fichiers-xs/").length()).replace("/", "_");
        } else if (link.startsWith("https://image.noelshack.com/fichiers-md/")) {
            return "nlsk_md/" + link.substring(("https://image.noelshack.com/fichiers-md/").length()).replace("/", "_");
        } else if (link.startsWith("https://image.noelshack.com/fichiers/")) {
            return "nlsk_big/" + link.substring(("https://image.noelshack.com/fichiers/").length()).replace("/", "_");
        } else if (link.startsWith("https://image.jeuxvideo.com/avatar")) {
            return "vtr_sm/" + link.substring(("https://image.jeuxvideo.com/avatar").length()).replace("/", "_");
        } else if (link.startsWith("http://image.jeuxvideo.com/avatar")) {
            return "vtr_sm/" + link.substring(("http://image.jeuxvideo.com/avatar").length()).replace("/", "_");
        } else {
            return "";
        }
    }

    public static void openCorrespondingBrowser(PrefsManager.LinkType linkTypeToOpenInternalBrowser, String link, Activity parentActivity) {
        boolean itsAJVCLink = link.matches("(?i)^http(s)?://((www|m)\\.)?jeuxvideo\\.com$") ||
                link.matches("(?i)^http(s)?://((www|m)\\.)?jeuxvideo\\.com/.*");

        if (linkTypeToOpenInternalBrowser.type == PrefsManager.LinkType.ALL_LINKS ||
                (linkTypeToOpenInternalBrowser.type == PrefsManager.LinkType.JVC_LINKS_ONLY && itsAJVCLink)) {
            openLinkInInternalBrowser(link, parentActivity);
        } else {
            openLinkInExternalBrowser(link, parentActivity);
        }
    }

    public static void openLinkInExternalBrowser(String link, Activity parentActivity) {
        try {
            Intent browserIntent;

            if (JVCParser.checkIfItsOpennableFormatedLink(JVCParser.formatThisUrlToClassicJvcUrl(link))) {
                browserIntent = Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(link)), parentActivity.getString(R.string.chooseBrowser));
            } else {
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            }

            parentActivity.startActivity(browserIntent);
        } catch (Exception e) {
            //rien
        }
    }

    public static void openLinkInInternalBrowser(String link, Activity parentActivity) {
        Intent newBrowserIntent = new Intent(parentActivity, WebBrowserActivity.class);
        newBrowserIntent.putExtra(WebBrowserActivity.EXTRA_URL_LOAD, link);
        parentActivity.startActivity(newBrowserIntent);
    }

    public static void shareThisLink(String link, Activity parentActivity) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, link);
        parentActivity.startActivity(Intent.createChooser(sharingIntent, parentActivity.getString(R.string.share)));
    }

    public static void putStringInClipboard(String textToCopy, Activity fromThisActivity) {
        ClipboardManager clipboard = (ClipboardManager) fromThisActivity.getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(textToCopy, textToCopy);
            clipboard.setPrimaryClip(clip);
        }
    }

    public static void insertStringInEditText(EditText currentEditText, String stringToInsert, int posOfCenterOfString) {
        int currentCursorPos = currentEditText.getSelectionStart();
        int currentEndOfSelec = currentEditText.getSelectionEnd();
        if (currentCursorPos == -1) {
            currentCursorPos = 0;
        }
        if (posOfCenterOfString < 0) {
            posOfCenterOfString = stringToInsert.length();
        }
        if (currentEndOfSelec > currentCursorPos && posOfCenterOfString < stringToInsert.length()) {
            String firstStringToAdd = stringToInsert.substring(0, posOfCenterOfString);
            String secondStringToAdd = stringToInsert.substring(posOfCenterOfString);
            currentEditText.getText().insert(currentEndOfSelec, secondStringToAdd);
            currentEditText.getText().insert(currentCursorPos, firstStringToAdd);
            currentEditText.setSelection(currentEndOfSelec + posOfCenterOfString);
        } else {
            currentEditText.getText().insert(currentCursorPos, stringToInsert);
            currentEditText.setSelection(currentCursorPos + posOfCenterOfString);
        }
    }

    public static CharSequence applyEmojiCompatIfPossible(CharSequence baseMessage) {
        if (EmojiCompat.get().getLoadState() == EmojiCompat.LOAD_STATE_SUCCEEDED) {
            return EmojiCompat.get().process(baseMessage);
        } else {
            return baseMessage;
        }
    }

    public static String makeMultipartFormFromMap(Map<String, String> formData)
    {
        Random random = new Random();
        StringBuilder res = new StringBuilder();
        String boundary = String.format("------geckoformboundary%x%x", random.nextLong(), random.nextLong());
        if(formData != null && !formData.isEmpty())
        {
            for(String key : formData.keySet())
            {
                // Un formulaire peut avoir plusieurs champs avec la même clef.
                // Par exemple, pour chaque réponse de sondage, on a un élément
                // responsesSurvey[] dans le formulaire.
                //
                // On utilise "\r\n" comme délimiteur entre les différentes
                // valeurs pour une même clé. Ainsi, si on a un champ
                // responsesSurvey[] avec "reponseA\r\nreponseB", on crée
                // deux responsesSurvey[] avec le premier "reponseA" puis
                // le second "reponseB".
                String value = formData.get(key);
                if(value != null)
                {
                    String[] valuesWithSameKey = value.split("\r\n");
                    for (String s : valuesWithSameKey) {
                        res.append(String.format("%s\r\nContent-Disposition: form-data; name=\"%s\"\r\n\r\n%s\r\n",
                                boundary, key, s));
                    }
                }
            }

            res.append(boundary).append("--\r\n");
        }

        return res.toString();
    }

    public static Map<String, String> prepareMultipartFormForMessage(String msgToSend, String forumId, String topicId, String group, String messageId, JVCParser.AjaxInfos ajaxInfos, JVCParser.FormSession formSession)
    {
        Map<String, String> res = new LinkedHashMap<>();
        if(ajaxInfos != null && formSession != null)
        {
            res.put("text", msgToSend);
            res.put("topicId", topicId);
            res.put("forumId", forumId);
            res.put("group", group);
            res.put("messageId", messageId);
            res.put("fs_session", formSession.session);
            res.put("fs_timestamp", formSession.timestamp);
            res.put("fs_version", formSession.fs_version);
            res.put(formSession.keyHash, formSession.valueHash);
            res.put("ajax_hash", ajaxInfos.newHash);
        }

        return res;
    }

    public static Map<String, String> prepareMultipartFormForTopic(String topicTitle, String message, String surveyQuestion, List<String> surveyAnswers, String forumId, String group, JVCParser.AjaxInfos ajaxInfos, JVCParser.FormSession formSession)
    {
        Map<String, String> res = new LinkedHashMap<>();
        if(ajaxInfos != null && formSession != null)
        {
            res.put("topicTitle", topicTitle);

            if(stringIsEmptyOrNull(surveyQuestion) || surveyAnswers == null || surveyAnswers.isEmpty())
            {
                // Même lorsqu'il n'y a pas de sondage, on doit
                // envoyer une question vide et deux réponses vides...
                // Urgh.
                res.put("submitSurvey", "false");
                res.put("answerSurvey", ""); // Aucune erreur, answerSurvey est la QUESTION du sondage pour JVC...
                res.put("responsesSurvey[]", "\r\n"); // Les réponses sont divisées par un \n.
            }
            else
            {
                // Sondage présent...
                StringBuilder answers = new StringBuilder();
                res.put("submitSurvey", "true");
                res.put("answerSurvey", surveyQuestion); // Aucune erreur, answerSurvey est la QUESTION du sondage pour JVC...

                // Les réponses sont divisées par un \r\n pour la transformation en multipart dans makeMultipartFormFromMap().
                answers.append(surveyAnswers.get(0));
                for(int i = 1; i < surveyAnswers.size(); i++)
                {
                    answers.append("\r\n").append(surveyAnswers.get(i));
                }

                res.put("responsesSurvey[]", answers.toString());
            }

            res.putAll(prepareMultipartFormForMessage(message, forumId, "0", group, "null", ajaxInfos, formSession));
        }

        return res;
    }

    public static String processJSONResponse(String pageContent)
    {
        String res = pageContent;

        // Si le premier caractère est une accolade, c'est probablement
        // du JSON. On vérifie.
        if(pageContent != null && !pageContent.isEmpty() && pageContent.charAt(0) == '{') {

            try {
                JSONObject json = new JSONObject(pageContent);
                if (json.has("redirectUrl")) // Création de topic ou post normal.
                {
                    String cleanUrl = json.getString("redirectUrl").replaceAll("\\\\", "");
                    res = "respawnirc:move:https://www.jeuxvideo.com" + cleanUrl;
                } else if (json.has("html")) // Modification de post.
                {
                    res = "respawnirc:move:";
                } else // Erreurs...
                {
                    res = "respawnirc:error:";

                    if (json.has("needsCaptcha")) {
                        boolean needsCaptcha = json.getBoolean("needsCaptcha");
                        if (needsCaptcha) {
                            res += "captcha";
                        }
                    }

                    if (!res.equals("respawnirc:error:captcha")) {

                        if (json.has("errors")) {
                            try {
                                // Certaines erreurs retournent un array...
                                JSONArray errors = json.getJSONArray("errors");
                                if(errors.length() > 0)
                                {
                                    res += errors.getString(0);
                                }
                            } catch (JSONException ex) {
                                // Autres erreurs...
                                JSONObject errors = json.getJSONObject("errors");
                                JSONArray errorNames = errors.names();
                                if(errorNames != null && errorNames.length() > 0)
                                {
                                    res += errors.getString(errorNames.getString(0));
                                }
                            }
                        }
                        else
                        {
                            res += "Erreur inconnue.";
                        }
                    }
                }
            } catch (JSONException e) {
                res = "respawnirc:resendneeded";
            }
        }

        return res;
    }

    @TargetApi(25)
    public static void updateShortcuts(Activity parentActivity, ShortcutManager shortcutManager, int sizeOfForumFavArray) {
        ArrayList<ShortcutInfo> listOfShortcuts = new ArrayList<>();
        int sizeOfShortcutArray = Math.min(sizeOfForumFavArray, 4);

        for (int i = 0; i < sizeOfShortcutArray; ++i) {
            String currentShortcutLink = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.FORUM_FAV_LINK, String.valueOf(i));
            String currentShortcutName = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.FORUM_FAV_NAME, String.valueOf(i));
            ShortcutInfo newShortcut = new ShortcutInfo.Builder(parentActivity, String.valueOf(i) + "_" + currentShortcutLink)
                    .setShortLabel(currentShortcutName)
                    .setLongLabel(currentShortcutName)
                    .setIcon(Icon.createWithResource(parentActivity, R.mipmap.ic_shortcut_forum))
                    .setIntent(new Intent(MainActivity.ACTION_OPEN_SHORTCUT, Uri.parse(currentShortcutLink), parentActivity, MainActivity.class)).build();

            listOfShortcuts.add(newShortcut);
        }

        try {
            shortcutManager.setDynamicShortcuts(listOfShortcuts);
        } catch (Exception e) {
            /* À ce qu'il parait ça peut crash "when the user is locked", je sais pas ce que ça
             * veut dire donc dans le doute je mets ça là. */
        }
    }

    public interface StringModifier {
        String changeString(String baseString);
    }
}
