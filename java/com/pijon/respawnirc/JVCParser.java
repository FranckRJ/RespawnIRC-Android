package com.pijon.respawnirc;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JVCParser {
    private static final Pattern ajaxTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_liste_messages\" id=\"ajax_timestamp_liste_messages\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_liste_messages\" id=\"ajax_hash_liste_messages\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxModTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_moderation_forum\" id=\"ajax_timestamp_moderation_forum\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxModHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_moderation_forum\" id=\"ajax_hash_moderation_forum\" value=\"([^\"]*)\" />");
    private static final Pattern messageQuotePattern = Pattern.compile("\"txt\":\"(.*)\"", Pattern.DOTALL);
    private static final Pattern entireMessagePattern = Pattern.compile("(<div class=\"bloc-message-forum \".*?)(<span id=\"post_[^\"]*\" class=\"bloc-message-forum-anchor\">|<div class=\"bloc-outils-plus-modo bloc-outils-bottom\">|<div class=\"bloc-pagi-default\">)", Pattern.DOTALL);
    private static final Pattern pseudoInfosPattern = Pattern.compile("<span class=\"JvCare [^ ]* bloc-pseudo-msg text-([^\"]*)\" target=\"_blank\">[^a-zA-Z0-9_\\[\\]-]*([a-zA-Z0-9_\\[\\]-]*)[^<]*</span>");
    private static final Pattern messagePattern = Pattern.compile("<div class=\"bloc-contenu\"><div class=\"txt-msg  text-[^-]*-forum \">((.*?)(?=<div class=\"info-edition-msg\">)|(.*?)(?=<div class=\"signature-msg)|(.*))", Pattern.DOTALL);
    private static final Pattern currentPagePattern = Pattern.compile("<span class=\"page-active\">([^<]*)</span>");
    private static final Pattern pageLinkPattern = Pattern.compile("<span><a href=\"([^\"]*)\" class=\"lien-jv\">([0-9]*)</a></span>");
    private static final Pattern topicFormPattern = Pattern.compile("(<form role=\"form\" class=\"form-post-topic[^\"]*\" method=\"post\" action=\"\".*?>.*?</form>)", Pattern.DOTALL);
    private static final Pattern inputFormPattern = Pattern.compile("<input ([^=]*)=\"([^\"]*)\" ([^=]*)=\"([^\"]*)\" ([^=]*)=\"([^\"]*)\"/>");
    private static final Pattern dateMessagePattern = Pattern.compile("<div class=\"bloc-date-msg\">([^<]*<span class=\"JvCare [^ ]* lien-jv\" target=\"_blank\">)?[^a-zA-Z0-9]*([^ ]* [^ ]* [^ ]* [^ ]* ([0-9:]*))");
    private static final Pattern messageIDPattern = Pattern.compile("<div class=\"bloc-message-forum \" data-id=\"([^\"]*)\">");
    private static final Pattern unicodeInTextPattern = Pattern.compile("\\\\u([a-zA-Z0-9]{4})");

    static class AjaxInfos {
        String list = null;
        String mod = null;
    }

    static class MessageInfos implements Parcelable {
        String pseudo;
        String messageNotParsed;
        String dateTime;
        long id;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(pseudo);
            out.writeString(messageNotParsed);
            out.writeString(dateTime);
            out.writeLong(id);
        }

        public static final Parcelable.Creator<MessageInfos> CREATOR = new Parcelable.Creator<MessageInfos>() {
            @Override
            public MessageInfos createFromParcel(Parcel in) {
                return new MessageInfos(in);
            }

            @Override
            public MessageInfos[] newArray(int size) {
                return new MessageInfos[size];
            }
        };

        MessageInfos() {
            //rien
        }

        private MessageInfos(Parcel in) {
            pseudo = in.readString();
            messageNotParsed = in.readString();
            dateTime = in.readString();
            id = in.readLong();
        }

    }

    private JVCParser() {
        //rien
    }

    static String buildMessageQuotedInfoFromThis(MessageInfos thisMessageInfo) {
        return ">[" + thisMessageInfo.dateTime + "] <" + thisMessageInfo.pseudo + ">";
    }

    private static String parsingAjaxMessages(String ajaxMessage) {
        Matcher unicodeInTextMatcher;

        ajaxMessage = ajaxMessage.replace("\n", "")
                .replace("\\r", "")
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .replace("\\\\", "\\")
                .replace("\\n", "\n");

        unicodeInTextMatcher = unicodeInTextPattern.matcher(ajaxMessage);
        while (unicodeInTextMatcher.find()) {
            ajaxMessage = ajaxMessage.substring(0, unicodeInTextMatcher.start()) + Character.toString((char) Integer.parseInt(unicodeInTextMatcher.group(1).trim(), 16)) + ajaxMessage.substring(unicodeInTextMatcher.end());

            unicodeInTextMatcher = unicodeInTextPattern.matcher(ajaxMessage);
        }

        ajaxMessage = ajaxMessage.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#039;", "\'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");

        return ajaxMessage;
    }

    static String getMessageQuoted(String pageSource) {
        Matcher messageQuoteMatcher = messageQuotePattern.matcher(pageSource);

        if (messageQuoteMatcher.find()) {
            return parsingAjaxMessages(messageQuoteMatcher.group(1)).replace("\n", "\n>");
        }

        return "";
    }

    static AjaxInfos getAllAjaxInfos(String pageSource) {
        AjaxInfos newAjaxInfos = new AjaxInfos();

        Matcher ajaxTimestampMatcher = ajaxTimestampPattern.matcher(pageSource);
        Matcher ajaxHashMatcher = ajaxHashPattern.matcher(pageSource);
        Matcher ajaxModTimestampMatcher = ajaxModTimestampPattern.matcher(pageSource);
        Matcher ajaxModHashMatcher = ajaxModHashPattern.matcher(pageSource);

        if(ajaxTimestampMatcher.find() && ajaxHashMatcher.find()) {
            newAjaxInfos.list = "ajax_timestamp=" + ajaxTimestampMatcher.group(1) + "&ajax_hash=" + ajaxHashMatcher.group(1);
        }

        if(ajaxModTimestampMatcher.find() && ajaxModHashMatcher.find()) {
            newAjaxInfos.mod = "ajax_timestamp=" + ajaxModTimestampMatcher.group(1) + "&ajax_hash=" + ajaxModHashMatcher.group(1);
        }

        return newAjaxInfos;
    }

    static String getListOfInputInAString(String pageSource) {
        StringBuilder allInputInAString = new StringBuilder();
        Matcher topicFormMatcher = topicFormPattern.matcher(pageSource);

        if (topicFormMatcher.find()) {
            Matcher inputFormMatcher;
            pageSource = topicFormMatcher.group(1);

            inputFormMatcher = inputFormPattern.matcher(pageSource);

            while (inputFormMatcher.find()) {
                allInputInAString.append("&");

                if (inputFormMatcher.group(1).equals("type")) {
                    if (inputFormMatcher.group(3).equals("name")) {
                        allInputInAString.append(inputFormMatcher.group(4)).append("=").append(inputFormMatcher.group(6));
                    } else {
                        allInputInAString.append(inputFormMatcher.group(6)).append("=").append(inputFormMatcher.group(4));
                    }
                } else if (inputFormMatcher.group(3).equals("type")) {
                    if (inputFormMatcher.group(1).equals("name")) {
                        allInputInAString.append(inputFormMatcher.group(2)).append("=").append(inputFormMatcher.group(6));
                    } else {
                        allInputInAString.append(inputFormMatcher.group(6)).append("=").append(inputFormMatcher.group(2));
                    }
                } else {
                    if (inputFormMatcher.group(1).equals("name")) {
                        allInputInAString.append(inputFormMatcher.group(2)).append("=").append(inputFormMatcher.group(4));
                    } else {
                        allInputInAString.append(inputFormMatcher.group(4)).append("=").append(inputFormMatcher.group(2));
                    }
                }
            }
        }

        return allInputInAString.toString();
    }

    static String getLastPageOfTopic(String pageSource) {
        int currentPageNumber = 0;
        String lastPage = "";
        Matcher currentPageMatcher = currentPagePattern.matcher(pageSource);
        Matcher pageLinkMatcher = pageLinkPattern.matcher(pageSource);

        if (currentPageMatcher.find()) {
            currentPageNumber = Integer.parseInt(currentPageMatcher.group(1));
        }

        while (pageLinkMatcher.find()) {
            if (Integer.parseInt(pageLinkMatcher.group(2)) > currentPageNumber) {
                currentPageNumber = Integer.parseInt(pageLinkMatcher.group(2));
                lastPage = "http://www.jeuxvideo.com" + pageLinkMatcher.group(1);
            }
        }

        return lastPage;
    }

    static String getNextPageOfTopic(String pageSource) {
        int currentPageNumber = 0;
        Matcher currentPageMatcher = currentPagePattern.matcher(pageSource);
        Matcher pageLinkMatcher = pageLinkPattern.matcher(pageSource);

        if (currentPageMatcher.find()) {
            currentPageNumber = Integer.parseInt(currentPageMatcher.group(1));
        }

        while (pageLinkMatcher.find()) {
            if (Integer.parseInt(pageLinkMatcher.group(2)) == (currentPageNumber + 1)) {
                return "http://www.jeuxvideo.com" + pageLinkMatcher.group(1);
            }
        }

        return "";
    }

    /*TODO: Améliorer ça, le rendre plus propre, et gérer plus de cas (pseudo admin/modo).*/
    static String createMessageFirstLineFromInfos(MessageInfos thisMessageInfo, String pseudoOfUser) {
        if (thisMessageInfo.pseudo.toLowerCase().equals(pseudoOfUser.toLowerCase())) {
            return "[" + thisMessageInfo.dateTime + "] &lt;<font color=\"#3399ff\">" + thisMessageInfo.pseudo + "</font>&gt;";
        }
        else {
            return "[" + thisMessageInfo.dateTime + "] &lt;<font color=\"#80002A\">" + thisMessageInfo.pseudo + "</font>&gt;";
        }
    }

    static String createMessageSecondLineFromInfos(MessageInfos thisMessageInfo) {
        return parseMessageToPrettyMessage(thisMessageInfo.messageNotParsed);
    }

    /*TODO: A refaire en plus propre et plus complet.*/
    private static String parseMessageToPrettyMessage(String thisMessage) {
        thisMessage = thisMessage.replace("\n", "")
                .replace("\r", "")
                .replaceAll("<ul[^>]*>", "")
                .replace("</ul>", "</p>")
                .replaceAll("<ol[^>]*>", "")
                .replace("</ol>", "</p>")
                .replace("<li>", "<br> • ")
                .replace("</li>", "")
                .replaceAll("</p> *<p>", "<br /><br />")
                .replace("<p>", "")
                .replace("</p>", "")
                .replace("</div>", "")
                .replaceAll("<div[^>]*>", "")
                .replaceAll("<img src=\"//image.jeuxvideo.com/smileys_img/([^\"]*)\" alt=\"[^\"]*\" data-def=\"SMILEYS\" data-code=\"([^\"]*)\" title=\"[^\"]*\" />", "$2")
                .replaceAll("<img class=\"img-stickers\" src=\"(http://jv.stkr.fr/p/([^\"]*))\"/>", "<a href=\"$1\">$1</a>")
                .replaceAll("<div class=\"player-contenu\"><div class=\"[^\"]*\"><iframe .*? src=\"http(s)?://www.youtube.com/embed/([^\"]*)\"[^>]*></iframe></div></div>", "<a href=\"http://youtu.be/$2\">http://youtu.be/$2</a>")
                .replaceAll("<a href=\"([^\"]*)\"( title=\"[^\"]*\")?>.*?</a>", "<a href=\"$1\">$1</a>")
                .replaceAll("<span class=\"JvCare [^\"]*\" rel=\"nofollow\" target=\"_blank\">([^<]*)</span>", "<a href=\"$1\">$1</a>")
                .replaceAll("<span class=\"JvCare [^\"]*\"[^i]*itle=\"([^\"]*)\">[^<]*<i></i><span>[^<]*</span>[^<]*</span>", "<a href=\"$1\">$1</a>")
                .replaceAll("<a href=\"([^\"]*)\" data-def=\"NOELSHACK\" target=\"_blank\"><img class=\"img-shack\" .*? src=\"//([^\"]*)\" [^>]*></a>", "<a href=\"$1\">$1</a>");

        while (thisMessage.startsWith("<br>")) {
            thisMessage = thisMessage.substring(4);
        }

        while (thisMessage.endsWith("<br>")) {
            thisMessage = thisMessage.substring(0, thisMessage.length() - 4);
        }

        thisMessage = thisMessage.trim();

        return thisMessage;
    }

    /*TODO: Possiblement passé "Pseudo supprimé" en ressource (string.xml).*/
    private static MessageInfos createMessageInfoFromEntireMessage(String thisEntireMessage) {
        MessageInfos newMessageInfo = new MessageInfos();
        Matcher pseudoInfosMatcher = pseudoInfosPattern.matcher(thisEntireMessage);
        Matcher messageMatcher = messagePattern.matcher(thisEntireMessage);
        Matcher dateMessageMatcher = dateMessagePattern.matcher(thisEntireMessage);
        Matcher messageIDMatcher = messageIDPattern.matcher(thisEntireMessage);

        if (pseudoInfosMatcher.find()) {
            newMessageInfo.pseudo = pseudoInfosMatcher.group(2);
        } else {
            newMessageInfo.pseudo = "Pseudo supprimé";
        }

        if (messageMatcher.find() && messageIDMatcher.find() && dateMessageMatcher.find()) {
            newMessageInfo.messageNotParsed = messageMatcher.group(1);
            newMessageInfo.dateTime = dateMessageMatcher.group(3);
            newMessageInfo.id = Long.parseLong(messageIDMatcher.group(1));
        }

        return newMessageInfo;
    }

    static ArrayList<MessageInfos> getMessagesOfThisPage(String sourcePage) {
        ArrayList<MessageInfos> listOfParsedMessage = new ArrayList<>();
        Matcher entireMessageMatcher = entireMessagePattern.matcher(sourcePage);

        while (entireMessageMatcher.find()) {
            String thisMessage = entireMessageMatcher.group(1);

            listOfParsedMessage.add(createMessageInfoFromEntireMessage(thisMessage));
        }

        return listOfParsedMessage;
    }
}