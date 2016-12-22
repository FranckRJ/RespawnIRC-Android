package com.franckrj.respawnirc.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JVCParser {
    private static final Pattern ajaxTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_liste_messages\" id=\"ajax_timestamp_liste_messages\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_liste_messages\" id=\"ajax_hash_liste_messages\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxModTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_moderation_forum\" id=\"ajax_timestamp_moderation_forum\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxModHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_moderation_forum\" id=\"ajax_hash_moderation_forum\" value=\"([^\"]*)\" />");
    private static final Pattern messageQuotePattern = Pattern.compile("\"txt\":\"(.*)\"", Pattern.DOTALL);
    private static final Pattern entireMessagePattern = Pattern.compile("(<div class=\"bloc-message-forum \".*?)(<span id=\"post_[^\"]*\" class=\"bloc-message-forum-anchor\">|<div class=\"bloc-outils-plus-modo bloc-outils-bottom\">|<div class=\"bloc-pagi-default\">)", Pattern.DOTALL);
    private static final Pattern entireTopicPattern = Pattern.compile("<li class=\"\" data-id=\"[^\"]*\">[^<]*<span class=\"topic-subject\">.*?</li>", Pattern.DOTALL);
    private static final Pattern pseudoInfosPattern = Pattern.compile("<span class=\"JvCare [^ ]* bloc-pseudo-msg text-([^\"]*)\" target=\"_blank\">[^a-zA-Z0-9_\\[\\]-]*([a-zA-Z0-9_\\[\\]-]*)[^<]*</span>");
    private static final Pattern messagePattern = Pattern.compile("<div class=\"bloc-contenu\"><div class=\"txt-msg  text-[^-]*-forum \">((.*?)(?=<div class=\"info-edition-msg\">)|(.*?)(?=<div class=\"signature-msg)|(.*))", Pattern.DOTALL);
    private static final Pattern currentPagePattern = Pattern.compile("<span class=\"page-active\">([^<]*)</span>");
    private static final Pattern pageLinkPattern = Pattern.compile("<span><a href=\"([^\"]*)\" class=\"lien-jv\">([0-9]*)</a></span>");
    private static final Pattern topicFormPattern = Pattern.compile("(<form role=\"form\" class=\"form-post-topic[^\"]*\" method=\"post\" action=\"\".*?>.*?</form>)", Pattern.DOTALL);
    private static final Pattern inputFormPattern = Pattern.compile("<input ([^=]*)=\"([^\"]*)\" ([^=]*)=\"([^\"]*)\" ([^=]*)=\"([^\"]*)\"/>");
    private static final Pattern dateMessagePattern = Pattern.compile("<div class=\"bloc-date-msg\">([^<]*<span class=\"JvCare [^ ]* lien-jv\" target=\"_blank\">)?[^a-zA-Z0-9]*([^ ]* [^ ]* [^ ]* [^ ]* ([0-9:]*))");
    private static final Pattern messageIDPattern = Pattern.compile("<div class=\"bloc-message-forum \" data-id=\"([^\"]*)\">");
    private static final Pattern unicodeInTextPattern = Pattern.compile("\\\\u([a-zA-Z0-9]{4})");
    private static final Pattern errorPattern = Pattern.compile("<div class=\"alert-row\">([^<]*)</div>");
    private static final Pattern errorInEditModePattern = Pattern.compile("\"erreur\":\\[\"([^\"]*)\"");
    private static final Pattern codeBlockPattern = Pattern.compile("<pre class=\"pre-jv\"><code class=\"code-jv\">([^<]*)</code></pre>");
    private static final Pattern codeLinePattern = Pattern.compile("<code class=\"code-jv\">(.*?)</code>", Pattern.DOTALL);
    private static final Pattern spoilLinePattern = Pattern.compile("<span class=\"bloc-spoil-jv en-ligne\">.*?<span class=\"contenu-spoil\">(.*?)</span></span>", Pattern.DOTALL);
    private static final Pattern spoilBlockPattern = Pattern.compile("<span class=\"bloc-spoil-jv\">.*?<span class=\"contenu-spoil\">(.*?)</span></span>", Pattern.DOTALL);
    private static final Pattern stickerPattern = Pattern.compile("<img class=\"img-stickers\" src=\"(http://jv.stkr.fr/p/([^\"]*))\"/>");
    private static final Pattern pageTopicLinkNumberPattern = Pattern.compile("(http://www.jeuxvideo.com/forums/[0-9]*-([0-9]*)-([0-9]*)-)([0-9]*)(-[0-9]*-[0-9]*-[0-9]*-[^.]*.htm)");
    private static final Pattern pageForumLinkNumberPattern = Pattern.compile("(http://www.jeuxvideo.com/forums/[0-9]*-([0-9]*)-[0-9]*-[0-9]*-[0-9]*-)([0-9]*)(-[0-9]*-[^.]*.htm)");
    private static final Pattern jvCarePattern = Pattern.compile("<span class=\"JvCare [^\"]*\">([^<]*)</span>");
    private static final Pattern lastEditMessagePattern = Pattern.compile("<div class=\"info-edition-msg\">(Message édité le ([^ ]* [^ ]* [^ ]* [^ ]* [0-9:]*) par.*?)</div>");
    private static final Pattern messageEditInfoPattern = Pattern.compile("<textarea tabindex=\"3\" class=\"area-editor\" name=\"text_commentaire\" id=\"text_commentaire\" placeholder=\"[^\"]*\">(.*?)</textarea>", Pattern.DOTALL);
    private static final Pattern allArianeStringPattern = Pattern.compile("<div class=\"fil-ariane-crumb\">.*?</h1>", Pattern.DOTALL);
    private static final Pattern forumNameInArianeStringPattern = Pattern.compile("<span><a href=\"/forums/0-[^\"]*\">([^<]*)</a></span>");
    private static final Pattern topicNameInArianeStringPattern = Pattern.compile("<span><a href=\"/forums/(42|1)-[^\"]*\">([^<]*)</a></span>");
    private static final Pattern highlightInArianeStringPattern = Pattern.compile("<h1 class=\"highlight\">([^<]*)</h1>");
    private static final Pattern topicNameAndLinkPattern = Pattern.compile("<a class=\"lien-jv topic-title[^\"]*\" href=\"([^\"]*\" title=\"[^\"]*)\"[^>]*>");
    private static final Pattern topicNumberMessagesPattern = Pattern.compile("<span class=\"topic-count\">[^0-9]*([0-9]*)");
    private static final Pattern topicAuthorPattern = Pattern.compile("<span class=\".*?text-([^ ]*) topic-author[^>]*>[^A-Za-z0-9\\[\\]_-]*([^<\n]*)");
    private static final Pattern topicDatePattern = Pattern.compile("<span class=\"topic-date\">[^<]*<span[^>]*>[^0-9/:]*([0-9/:]*)");
    private static final Pattern topicTypePattern = Pattern.compile("<img src=\"/img/forums/topic-(.*?).png\"");
    private static final Pattern forumFavsBlocPattern = Pattern.compile("<h2>Mes forums favoris</h2>.*?<ul class=\"display-list-simple\">(.*?)</ul>", Pattern.DOTALL);
    private static final Pattern topicFavsBlocPattern = Pattern.compile("<h2>Mes sujets favoris</h2>.*?<ul class=\"display-list-simple\">(.*?)</ul>", Pattern.DOTALL);
    private static final Pattern favPattern = Pattern.compile("<li><a href=\"([^\"]*)\">([^<]*)</a></li>");
    private static final Pattern htmlTagPattern = Pattern.compile("<.+?>");

    private JVCParser() {
        //rien
    }

    public static String getForumForTopicLink(String topicLink) {
        Matcher pageTopicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(topicLink);

        if (pageTopicLinkNumberMatcher.find()) {
            return "http://www.jeuxvideo.com/forums/0-" + pageTopicLinkNumberMatcher.group(2) + "-0-1-0-1-0-respawn-irc.htm";
        } else {
            return "";
        }
    }

    public static String formatThisUrl(String urlToChange) {
        if (urlToChange.startsWith("https://")) {
            urlToChange = urlToChange.replaceFirst("https://", "http://");
        }

        if (!urlToChange.startsWith("http://")) {
            urlToChange = "http://" + urlToChange;
        }

        if (urlToChange.startsWith("http://m.jeuxvideo.com/")) {
            urlToChange = urlToChange.replaceFirst("http://m.jeuxvideo.com/", "http://www.jeuxvideo.com/");
        } else if (urlToChange.startsWith("http://jeuxvideo.com/")) {
            urlToChange = urlToChange.replaceFirst("http://jeuxvideo.com/", "http://www.jeuxvideo.com/");
        }

        return urlToChange;
    }

    public static boolean checkIfTopicAreSame(String firstTopicLink, String secondTopicLink) {
        Matcher firstPageTopicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(firstTopicLink);
        Matcher secondPageTopicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(secondTopicLink);

        if (firstPageTopicLinkNumberMatcher.find() && secondPageTopicLinkNumberMatcher.find()) {
            boolean forumAreEquals = firstPageTopicLinkNumberMatcher.group(2).equals(secondPageTopicLinkNumberMatcher.group(2));
            boolean topicsAreEquals = firstPageTopicLinkNumberMatcher.group(3).equals(secondPageTopicLinkNumberMatcher.group(3));
            return forumAreEquals && topicsAreEquals;
        } else {
            return false;
        }
    }

    public static boolean checkIfItsForumLink(String linkToCheck) {
        return linkToCheck.contains("jeuxvideo.com/forums/0-");
    }

    public static String getFirstPageForThisTopicLink(String topicLink) {
        Matcher pageTopicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(topicLink);

        if (pageTopicLinkNumberMatcher.find()) {
            return pageTopicLinkNumberMatcher.group(1) + "1" + pageTopicLinkNumberMatcher.group(5);
        }
        else {
            return "";
        }
    }

    public static String getPageNumberForThisTopicLink(String topicLink) {
        Matcher pageTopicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(topicLink);

        if (pageTopicLinkNumberMatcher.find()) {
            return pageTopicLinkNumberMatcher.group(4);
        }
        else {
            return "";
        }
    }

    public static String setPageNumberForThisTopicLink(String topicLink, int newPageNumber) {
        Matcher pageTopicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(topicLink);

        if (pageTopicLinkNumberMatcher.find()) {
            return pageTopicLinkNumberMatcher.group(1) + String.valueOf(newPageNumber) + pageTopicLinkNumberMatcher.group(5);
        }
        else {
            return "";
        }
    }

    public static String getPageNumberForThisForumLink(String forumLink) {
        Matcher pageForumLinkNumberMatcher = pageForumLinkNumberPattern.matcher(forumLink);

        if (pageForumLinkNumberMatcher.find()) {
            return pageForumLinkNumberMatcher.group(3);
        }
        else {
            return "";
        }
    }

    public static String setPageNumberForThisForumLink(String forumLink, int newPageNumber) {
        Matcher pageForumLinkNumberMatcher = pageForumLinkNumberPattern.matcher(forumLink);

        if (pageForumLinkNumberMatcher.find()) {
            return pageForumLinkNumberMatcher.group(1) + String.valueOf(newPageNumber) + pageForumLinkNumberMatcher.group(4);
        }
        else {
            return "";
        }
    }

    public static ArrayList<NameAndLink> getListOfForumsFavs(String pageSource) {
        Matcher forumFavsBlocMatcher = forumFavsBlocPattern.matcher(pageSource);

        if (forumFavsBlocMatcher.find()) {
            return getListOfFavInBloc(forumFavsBlocMatcher.group());
        } else {
            return new ArrayList<>();
        }
    }

    public static ArrayList<NameAndLink> getListOfTopicsFavs(String pageSource) {
        Matcher topicFavsBlocMatcher = topicFavsBlocPattern.matcher(pageSource);

        if (topicFavsBlocMatcher.find()) {
            return getListOfFavInBloc(topicFavsBlocMatcher.group());
        } else {
            return new ArrayList<>();
        }
    }

    public static ArrayList<NameAndLink> getListOfFavInBloc(String pageSource) {
        Matcher favMatcher = favPattern.matcher(pageSource);
        ArrayList<NameAndLink> listOfFav = new ArrayList<>();
        int lastOffset = 0;

        while (favMatcher.find(lastOffset)) {
            NameAndLink newFav = new NameAndLink();
            newFav.name = favMatcher.group(2);
            newFav.link = "http:" + favMatcher.group(1);
            listOfFav.add(newFav);
            lastOffset = favMatcher.end();
        }

        return listOfFav;
    }

    public static String getForumNameInForumPage(String pageSource) {
        String forumName = "";
        Matcher allArianeStringMatcher = allArianeStringPattern.matcher(pageSource);

        if (allArianeStringMatcher.find()) {
            String allArianeString = allArianeStringMatcher.group();
            Matcher forumNameMatcher = forumNameInArianeStringPattern.matcher(allArianeString);
            Matcher highlightMatcher = highlightInArianeStringPattern.matcher(allArianeString);
            int lastOffset = 0;

            while (forumNameMatcher.find(lastOffset)) {
                forumName = forumNameMatcher.group(1);
                lastOffset = forumNameMatcher.end();
            }
            if (highlightMatcher.find()) {
                if (forumName.isEmpty() || !forumName.equals(highlightMatcher.group(1).substring(0, Math.min(forumName.length(), highlightMatcher.group(1).length())))) {
                    forumName = highlightMatcher.group(1);
                }
            }

            if (forumName.startsWith("Forum")) {
                forumName =  forumName.substring(("Forum").length());
            }
            forumName = forumName.trim().replace("&amp;", "&").replace("&quot;", "\"").replace("&#039;", "\'").replace("&lt;", "<").replace("&gt;", ">");
        }

        return forumName;
    }

    public static ForumAndTopicName getForumAndTopicNameInTopicPage(String pageSource) {
        ForumAndTopicName currentNames = new ForumAndTopicName();
        Matcher allArianeStringMatcher = allArianeStringPattern.matcher(pageSource);

        if (allArianeStringMatcher.find()) {
            String allArianeString = allArianeStringMatcher.group();
            Matcher forumNameMatcher = forumNameInArianeStringPattern.matcher(allArianeString);
            Matcher topicNameMatcher = topicNameInArianeStringPattern.matcher(allArianeString);
            Matcher highlightMatcher = highlightInArianeStringPattern.matcher(allArianeString);
            int lastOffset = 0;

            while (forumNameMatcher.find(lastOffset)) {
                currentNames.forum = forumNameMatcher.group(1);
                lastOffset = forumNameMatcher.end();
            }
            if (topicNameMatcher.find()) {
                currentNames.topic = topicNameMatcher.group(2);
            } else if (highlightMatcher.find()) {
                currentNames.topic = highlightMatcher.group(1);
            }

            if (currentNames.forum.startsWith("Forum")) {
                currentNames.forum =  currentNames.forum.substring(("Forum").length());
            }
            if (currentNames.topic.startsWith("Topic")) {
                currentNames.topic =  currentNames.topic.substring(("Topic").length());
            }

            currentNames.forum = currentNames.forum.trim().replace("&amp;", "&").replace("&quot;", "\"").replace("&#039;", "\'").replace("&lt;", "<").replace("&gt;", ">");
            currentNames.topic = currentNames.topic.trim().replace("&amp;", "&").replace("&quot;", "\"").replace("&#039;", "\'").replace("&lt;", "<").replace("&gt;", ">");
        }

        return currentNames;
    }

    public static String getErrorMessage(String pageSource) {
        Matcher errorMatcher = errorPattern.matcher(pageSource);

        if (errorMatcher.find()) {
            return "Erreur : " + errorMatcher.group(1);
        } else {
            return "Erreur : le message n'a pas été envoyé.";
        }
    }

    public static String getErrorMessageInEditMode(String pageSource) {
        Matcher errorMatcher = errorInEditModePattern.matcher(pageSource);

        if (errorMatcher.find()) {
            return "Erreur : " + parsingAjaxMessages(errorMatcher.group(1));
        } else {
            return null;
        }
    }

    public static String getMessageEdit(String pageSource) {
        Matcher messageEditInfoMatcher = messageEditInfoPattern.matcher(pageSource);

        if (messageEditInfoMatcher.find()) {
            return messageEditInfoMatcher.group(1);
        } else {
            return "";
        }
    }

    public static String buildMessageQuotedInfoFromThis(MessageInfos thisMessageInfo) {
        return ">[" + thisMessageInfo.dateTime + "] <" + thisMessageInfo.pseudo + ">";
    }

    public static String parsingAjaxMessages(String ajaxMessage) {
        Matcher unicodeInTextMatcher;

        ajaxMessage = ajaxMessage.replace("\n", "")
                .replace("\r", "")
                .replaceAll("(?<!\\\\)\\\\r", "")
                .replaceAll("(?<!\\\\)\\\\\"", "\"")
                .replaceAll("(?<!\\\\)\\\\/", "/")
                .replaceAll("(?<!\\\\)\\\\n", "\n")
                .replace("\\\\", "\\");

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

    public static String getMessageQuoted(String pageSource) {
        Matcher messageQuoteMatcher = messageQuotePattern.matcher(pageSource);

        if (messageQuoteMatcher.find()) {
            return parsingAjaxMessages(messageQuoteMatcher.group(1)).replace("\n", "\n>");
        }

        return "";
    }

    public static AjaxInfos getAllAjaxInfos(String pageSource) {
        AjaxInfos newAjaxInfos = new AjaxInfos();

        Matcher ajaxTimestampMatcher = ajaxTimestampPattern.matcher(pageSource);
        Matcher ajaxHashMatcher = ajaxHashPattern.matcher(pageSource);
        Matcher ajaxModTimestampMatcher = ajaxModTimestampPattern.matcher(pageSource);
        Matcher ajaxModHashMatcher = ajaxModHashPattern.matcher(pageSource);

        if (ajaxTimestampMatcher.find() && ajaxHashMatcher.find()) {
            newAjaxInfos.list = "ajax_timestamp=" + ajaxTimestampMatcher.group(1) + "&ajax_hash=" + ajaxHashMatcher.group(1);
        }

        if (ajaxModTimestampMatcher.find() && ajaxModHashMatcher.find()) {
            newAjaxInfos.mod = "ajax_timestamp=" + ajaxModTimestampMatcher.group(1) + "&ajax_hash=" + ajaxModHashMatcher.group(1);
        }

        return newAjaxInfos;
    }

    public static String getListOfInputInAString(String pageSource) {
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

    public static String getLastPageOfTopic(String pageSource) {
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

    public static String getNextPageOfTopic(String pageSource) {
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

    public static String createMessageFirstLineFromInfos(MessageInfos thisMessageInfo, Settings settings) {
        String newFirstLine = settings.firstLineFormat;

        newFirstLine = newFirstLine.replaceAll("<%DATE_TIME%>", thisMessageInfo.dateTime);
        newFirstLine = newFirstLine.replaceAll("<%DATE_FULL%>", thisMessageInfo.wholeDate);
        newFirstLine = newFirstLine.replaceAll("<%PSEUDO_PSEUDO%>", thisMessageInfo.pseudo);

        if (thisMessageInfo.isAnEdit) {
            newFirstLine = newFirstLine.replaceAll("<%DATE_COLOR_START%>", "<font color=\"#008000\">");
            newFirstLine = newFirstLine.replaceAll("<%DATE_COLOR_END%>", "</font>");
        } else {
            newFirstLine = newFirstLine.replaceAll("<%DATE_COLOR_START%>", "");
            newFirstLine = newFirstLine.replaceAll("<%DATE_COLOR_END%>", "");
        }

        if (thisMessageInfo.pseudo.toLowerCase().equals(settings.pseudoOfUser.toLowerCase())) {
            newFirstLine = newFirstLine.replaceAll("<%PSEUDO_COLOR_START%>", "<font color=\"" + settings.colorPseudoUser + "\">");
        } else if (thisMessageInfo.pseudoType.equals("modo")){
            newFirstLine = newFirstLine.replaceAll("<%PSEUDO_COLOR_START%>", "<font color=\"" + settings.colorPseudoModo + "\">");
        } else if (thisMessageInfo.pseudoType.equals("admin") || thisMessageInfo.pseudoType.equals("staff")){
            newFirstLine = newFirstLine.replaceAll("<%PSEUDO_COLOR_START%>", "<font color=\"" + settings.colorPseudoAdmin + "\">");
        } else {
            newFirstLine = newFirstLine.replaceAll("<%PSEUDO_COLOR_START%>", "<font color=\"" + settings.colorPseudoOther + "\">");
        }
        newFirstLine = newFirstLine.replaceAll("<%PSEUDO_COLOR_END%>", "</font>");

        return newFirstLine;
    }

    public static String createMessageSecondLineFromInfos(MessageInfos thisMessageInfo, Settings settings) {
        String finalMessage = settings.secondLineFormat;
        String tmpMessage = thisMessageInfo.messageNotParsed;

        tmpMessage = parseThisMessageWithThisPattern(tmpMessage, codeBlockPattern, 1, "<p><font face=\"monospace\">", "</font></p>", new ConvertStringToString("\n", "<br />"), new ConvertStringToString(" ", " ")); //remplace les espaces par des alt+255
        tmpMessage = parseThisMessageWithThisPattern(tmpMessage, codeLinePattern, 1, "<font face=\"monospace\">", "</font>", new ConvertStringToString(" ", " "), null); //remplace les espaces par des alt+255
        tmpMessage = StickerConverter.convertStickerWithThisRule(tmpMessage, StickerConverter.ruleForNoLangageSticker);
        tmpMessage = parseThisMessageWithThisPattern(tmpMessage, stickerPattern, 2, "<img src=\"sticker_", ".png\"/>", new ConvertStringToString("-", "_"), null);

        tmpMessage = tmpMessage.replace("\n", "").replace("\r", "");

        if (tmpMessage.contains("<li>")) {
            tmpMessage = tmpMessage.replaceAll("<ul[^>]*>", "<p>")
                    .replace("</ul>", "</p>")
                    .replaceAll("<ol[^>]*>", "<p>")
                    .replace("</ol>", "</p>")
                    .replace("<li><p><li>", "<li><li>")
                    .replace("<li><p><li>", "<li><li>")
                    .replace("<li>", " • ")
                    .replaceAll("</li></p></li>", "</li>")
                    .replaceAll("</li></p></li>", "</li>")
                    .replace("</li>", "<br />");
        }

        tmpMessage = tmpMessage.replaceAll("<img src=\"//image.jeuxvideo.com/smileys_img/([^\"]*)\" alt=\"[^\"]*\" data-def=\"SMILEYS\" data-code=\"([^\"]*)\" title=\"[^\"]*\" />", "<img src=\"smiley_$1\"/>")
                .replaceAll("<div class=\"player-contenu\"><div class=\"[^\"]*\"><iframe .*? src=\"http(s)?://www.youtube.com/embed/([^\"]*)\"[^>]*></iframe></div></div>", "<a href=\"http://youtu.be/$2\">http://youtu.be/$2</a>")
                .replaceAll("<a href=\"([^\"]*)\"( title=\"[^\"]*\")?>.*?</a>", "<a href=\"$1\">$1</a>")
                .replaceAll("<span class=\"JvCare [^\"]*\" rel=\"nofollow[^\"]*\" target=\"_blank\">([^<]*)</span>", "<a href=\"$1\">$1</a>")
                .replaceAll("<span class=\"JvCare [^\"]*\"[^i]*itle=\"([^\"]*)\">[^<]*<i></i><span>[^<]*</span>[^<]*</span>", "<a href=\"$1\">$1</a>");

        if (settings.showNoelshackImages) {
            tmpMessage = tmpMessage.replaceAll("<a href=\"([^\"]*)\" data-def=\"NOELSHACK\" target=\"_blank\"><img class=\"img-shack\" .*? src=\"//([^\"]*)\" [^>]*></a>", "<a href=\"$1\"><img src=\"http://$2\"/></a>");
        } else {
            tmpMessage = tmpMessage.replaceAll("<a href=\"([^\"]*)\" data-def=\"NOELSHACK\" target=\"_blank\"><img class=\"img-shack\" .*? src=\"//([^\"]*)\" [^>]*></a>", "<a href=\"$1\">$1</a>");
        }

        if (thisMessageInfo.containSpoil) {
            if (!thisMessageInfo.showSpoil) {
                tmpMessage = parseThisMessageWithThisPattern(tmpMessage, spoilLinePattern, 1, "", "", new ConvertRegexpToString("<.+?>", " "), new ConvertRegexpToString("(?s).", "█"));
                tmpMessage = parseThisMessageWithThisPattern(tmpMessage, spoilBlockPattern, 1, "<p>", "</p>", new ConvertRegexpToString("<.+?>", " "), new ConvertRegexpToString("(?s).", "█"));
            } else {
                tmpMessage = parseThisMessageWithThisPattern(tmpMessage, spoilLinePattern, 1, "<font color=\"#000000\">", "</font>", new RemoveFirstsAndLastsP(), null);
                tmpMessage = parseThisMessageWithThisPattern(tmpMessage, spoilBlockPattern, 1, "<p><font color=\"#000000\">", "</font></p>",  new RemoveFirstsAndLastsP(), null);
            }
        }

        tmpMessage = tmpMessage.replace("<blockquote class=\"blockquote-jv\">", "<blockquote>")
                .replaceAll("<div[^>]*>", "")
                .replace("</div>", "")
                .replaceAll("(<br /> *){0,2}</p> *<p>( *<br />){0,2}", "<br /><br />")
                .replaceAll("<br /> *<(/)?p> *<br />", "<br /><br />")
                .replaceAll("(<br /> *){1,2}<(/)?p>", "<br /><br />")
                .replaceAll("<(/)?p>(<br /> *){1,2}", "<br /><br />")
                .replaceAll("<(/)?p>", "<br /><br />")
                .replaceAll("(<br /> *)*(<(/)?blockquote>)( *<br />)*", "$2");

        tmpMessage = parseThisMessageWithThisPattern(tmpMessage, jvCarePattern, 1, "", "", new MakeLinkIfPossible(), null);

        tmpMessage = tmpMessage.trim();

        while (tmpMessage.startsWith("<br />")) {
            tmpMessage = tmpMessage.substring(6).trim();
        }

        while (tmpMessage.endsWith("<br />")) {
            tmpMessage = tmpMessage.substring(0, tmpMessage.length() - 6).trim();
        }

        if (!thisMessageInfo.showOverlyQuote) {
            tmpMessage = removeOverlyQuoteInPrettyMessage(tmpMessage, settings.maxNumberOfOverlyQuotes);
        }

        if (!settings.pseudoOfUser.isEmpty()) {
            tmpMessage = tmpMessage.replaceAll("(?i)" + settings.pseudoOfUser.replace("[", "\\[").replace("]", "\\]") + "(?![^<>]*(>|</a>))", "<font color=\"" + settings.colorPseudoUser + "\">$0</font>");
        }

        finalMessage = finalMessage.replace("<%MESSAGE_MESSAGE%>", tmpMessage);
        if (!thisMessageInfo.lastTimeEdit.isEmpty()) {
            finalMessage = finalMessage.replace("<%EDIT_ALL%>", settings.addBeforeEdit + thisMessageInfo.lastTimeEdit.trim() + settings.addAfterEdit);
        } else {
            finalMessage = finalMessage.replace("<%EDIT_ALL%>", "");
        }
        finalMessage = finalMessage.replaceAll("(<br /> *)*(<(/)?blockquote>)( *<br />)*", "$2");

        return finalMessage;
    }

    public static String removeOverlyQuoteInPrettyMessage(String prettyMessage, int maxNumberOfOverlyQuotes) {
        Matcher htmlTagMatcher = htmlTagPattern.matcher(prettyMessage);
        int lastOffsetOfTag = 0;

        maxNumberOfOverlyQuotes += 1;

        while (htmlTagMatcher.find(lastOffsetOfTag)) {
            if (htmlTagMatcher.group().equals("<blockquote>")) {
                --maxNumberOfOverlyQuotes;
            }
            else if (htmlTagMatcher.group().equals("</blockquote>")) {
                ++maxNumberOfOverlyQuotes;
            }

            lastOffsetOfTag = htmlTagMatcher.end();

            if (maxNumberOfOverlyQuotes <= 0) {
                Matcher secHtmlTagMatcher = htmlTagPattern.matcher(prettyMessage);
                int tmpNumberQuote = 0;
                boolean hasMatched = false;
                int secLastOffsetTag = htmlTagMatcher.end();

                while (secHtmlTagMatcher.find(secLastOffsetTag)) {
                    hasMatched = true;

                    if (secHtmlTagMatcher.group().equals("<blockquote>")) {
                        ++tmpNumberQuote;
                    }
                    else if (secHtmlTagMatcher.group().equals("</blockquote>")) {
                        --tmpNumberQuote;
                    }

                    secLastOffsetTag = secHtmlTagMatcher.end();

                    if (tmpNumberQuote < 0) {
                        break;
                    }
                }

                if (hasMatched) {
                    prettyMessage = prettyMessage.substring(0, htmlTagMatcher.end()) + "[...]" + prettyMessage.substring(secHtmlTagMatcher.start());
                    htmlTagMatcher = htmlTagPattern.matcher(prettyMessage);
                }
            }
        }

        return prettyMessage;
    }

    public static int countNumberOfOverlyQuoteInNotPrettyMessage(String notPrettyMessage) {
        Matcher htmlTagMatcher = htmlTagPattern.matcher(notPrettyMessage);
        int maxNumberOfOverlyQuoteInMessage = 0;
        int currentNumberOfOverlyQuoteInMessage = 0;
        int lastOffsetOfTag = 0;

        while (htmlTagMatcher.find(lastOffsetOfTag)) {
            if (htmlTagMatcher.group().equals("<blockquote class=\"blockquote-jv\">")) {
                ++currentNumberOfOverlyQuoteInMessage;
            }
            else if (htmlTagMatcher.group().equals("</blockquote>")) {
                --currentNumberOfOverlyQuoteInMessage;
            }

            if (currentNumberOfOverlyQuoteInMessage > maxNumberOfOverlyQuoteInMessage) {
                maxNumberOfOverlyQuoteInMessage = currentNumberOfOverlyQuoteInMessage;
            }

            lastOffsetOfTag = htmlTagMatcher.end();
        }

        return maxNumberOfOverlyQuoteInMessage;
    }

    public static String parseThisMessageWithThisPattern(String messageToParse, Pattern patternToUse, int groupToUse, String stringBefore, String stringAfter, StringModifier firstModifier, StringModifier secondModifier) {
        Matcher matcherToUse = patternToUse.matcher(messageToParse);

        while (matcherToUse.find()) {
            String newMessage = messageToParse.substring(0, matcherToUse.start()) + stringBefore;
            String messageContent = matcherToUse.group(groupToUse);

            if (firstModifier != null) {
                messageContent = firstModifier.changeString(messageContent);
            }
            if (secondModifier != null ) {
                messageContent = secondModifier.changeString(messageContent);
            }

            newMessage += messageContent + stringAfter + messageToParse.substring(matcherToUse.end());

            messageToParse = newMessage;
            matcherToUse = patternToUse.matcher(messageToParse);
        }

        return messageToParse;
    }

    /*TODO: Possiblement passer "Pseudo supprimé" en ressource (string.xml).*/
    public static MessageInfos createMessageInfoFromEntireMessage(String thisEntireMessage) {
        MessageInfos newMessageInfo = new MessageInfos();
        Matcher pseudoInfosMatcher = pseudoInfosPattern.matcher(thisEntireMessage);
        Matcher messageMatcher = messagePattern.matcher(thisEntireMessage);
        Matcher dateMessageMatcher = dateMessagePattern.matcher(thisEntireMessage);
        Matcher lastEditMessageMatcher = lastEditMessagePattern.matcher(thisEntireMessage);
        Matcher messageIDMatcher = messageIDPattern.matcher(thisEntireMessage);

        if (pseudoInfosMatcher.find()) {
            newMessageInfo.pseudo = pseudoInfosMatcher.group(2);
            newMessageInfo.pseudoType = pseudoInfosMatcher.group(1);
        } else {
            newMessageInfo.pseudo = "Pseudo supprimé";
            newMessageInfo.pseudoType = "user";
        }

        if (lastEditMessageMatcher.find()) {
            newMessageInfo.lastTimeEdit = lastEditMessageMatcher.group(1).replaceAll(htmlTagPattern.pattern(), "");
        } else {
            newMessageInfo.lastTimeEdit = "";
        }

        if (messageMatcher.find() && messageIDMatcher.find() && dateMessageMatcher.find()) {
            newMessageInfo.messageNotParsed = messageMatcher.group(1);
            newMessageInfo.dateTime = dateMessageMatcher.group(3);
            newMessageInfo.wholeDate = dateMessageMatcher.group(2);
            newMessageInfo.containSpoil = newMessageInfo.messageNotParsed.contains("<span class=\"contenu-spoil\">");
            newMessageInfo.numberOfOverlyQuote = countNumberOfOverlyQuoteInNotPrettyMessage(newMessageInfo.messageNotParsed);
            newMessageInfo.id = Long.parseLong(messageIDMatcher.group(1));
        }

        return newMessageInfo;
    }

    public static TopicInfos createTopicInfoFromEntireTopic(String thisEntireTopic) {
        TopicInfos newTopicInfo = new TopicInfos();
        Matcher topicNameAndLinkMatcher = topicNameAndLinkPattern.matcher(thisEntireTopic);
        Matcher topicNumberMessagesMatcher = topicNumberMessagesPattern.matcher(thisEntireTopic);
        Matcher topicAuthorMatcher = topicAuthorPattern.matcher(thisEntireTopic);
        Matcher topicDateMatcher = topicDatePattern.matcher(thisEntireTopic);
        Matcher topicTypeMatcher = topicTypePattern.matcher(thisEntireTopic);

        if (topicAuthorMatcher.find()) {
            newTopicInfo.author = topicAuthorMatcher.group(2).trim();
            newTopicInfo.authorType = topicAuthorMatcher.group(1).trim();
        } else {
            newTopicInfo.author = "Pseudo supprimé";
            newTopicInfo.authorType = "user";
        }

        if (topicNameAndLinkMatcher.find() && topicNumberMessagesMatcher.find() && topicDateMatcher.find() && topicTypeMatcher.find()) {
            String topicNameAndLinkString = topicNameAndLinkMatcher.group(1);
            newTopicInfo.link = "http://www.jeuxvideo.com" + topicNameAndLinkString.substring(0, topicNameAndLinkString.indexOf("\""));
            newTopicInfo.name = topicNameAndLinkString.substring(topicNameAndLinkString.indexOf("title=\"") + 7);
            newTopicInfo.name = newTopicInfo.name.replace("&amp;", "&").replace("&quot;", "\"").replace("&#039;", "\'").replace("&lt;", "<").replace("&gt;", ">");
            newTopicInfo.messages = topicNumberMessagesMatcher.group(1);
            newTopicInfo.wholeDate = topicDateMatcher.group(1);
            newTopicInfo.type = topicTypeMatcher.group(1);
        }

        return newTopicInfo;
    }

    public static ArrayList<MessageInfos> getMessagesOfThisPage(String sourcePage) {
        ArrayList<MessageInfos> listOfParsedMessage = new ArrayList<>();
        Matcher entireMessageMatcher = entireMessagePattern.matcher(sourcePage);

        while (entireMessageMatcher.find()) {
            String thisMessage = entireMessageMatcher.group(1);

            listOfParsedMessage.add(createMessageInfoFromEntireMessage(thisMessage));
        }

        Collections.sort(listOfParsedMessage);

        return listOfParsedMessage;
    }

    public static ArrayList<TopicInfos> getTopicsOfThisPage(String sourcePage) {
        ArrayList<TopicInfos> listOfParsedTopic = new ArrayList<>();
        Matcher entireTopicMatcher = entireTopicPattern.matcher(sourcePage);

        while (entireTopicMatcher.find()) {
            String thisTopic = entireTopicMatcher.group(0);

            listOfParsedTopic.add(createTopicInfoFromEntireTopic(thisTopic));
        }

        return listOfParsedTopic;
    }

    public static class MessageInfos implements Parcelable, Comparable<MessageInfos> {
        public String pseudo = "";
        public String pseudoType = "";
        public String messageNotParsed = "";
        public String dateTime = "";
        public String wholeDate = "";
        public String lastTimeEdit = "";
        public boolean containSpoil = false;
        public boolean showSpoil = false;
        public int numberOfOverlyQuote = 0;
        public boolean showOverlyQuote = false;
        public boolean isAnEdit = false;
        public long id = 0;

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

        public MessageInfos() {
            //rien
        }

        private MessageInfos(Parcel in) {
            pseudo = in.readString();
            pseudoType = in.readString();
            messageNotParsed = in.readString();
            dateTime = in.readString();
            wholeDate = in.readString();
            lastTimeEdit = in.readString();
            containSpoil = (in.readInt() == 1);
            showSpoil = (in.readInt() == 1);
            numberOfOverlyQuote = in.readInt();
            showOverlyQuote = (in.readInt() == 1);
            isAnEdit = (in.readInt() == 1);
            id = in.readLong();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(pseudo);
            out.writeString(pseudoType);
            out.writeString(messageNotParsed);
            out.writeString(dateTime);
            out.writeString(wholeDate);
            out.writeString(lastTimeEdit);
            out.writeInt(containSpoil ? 1 : 0);
            out.writeInt(showSpoil ? 1 : 0);
            out.writeInt(numberOfOverlyQuote);
            out.writeInt(showOverlyQuote ? 1 : 0);
            out.writeInt(isAnEdit ? 1 : 0);
            out.writeLong(id);
        }

        @Override
        public int compareTo(@NonNull MessageInfos messageInfos) {
            if (id - messageInfos.id < 0) {
                return -1;
            } else if (id - messageInfos.id > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static class TopicInfos implements Parcelable {
        public String author = "";
        public String authorType = "";
        public String type = "";
        public String name = "";
        public String link = "";
        public String wholeDate = "";
        public String messages = "";

        public static final Parcelable.Creator<TopicInfos> CREATOR = new Parcelable.Creator<TopicInfos>() {
            @Override
            public TopicInfos createFromParcel(Parcel in) {
                return new TopicInfos(in);
            }

            @Override
            public TopicInfos[] newArray(int size) {
                return new TopicInfos[size];
            }
        };

        public TopicInfos() {
            //rien
        }

        private TopicInfos(Parcel in) {
            author = in.readString();
            authorType = in.readString();
            type = in.readString();
            name = in.readString();
            link = in.readString();
            wholeDate = in.readString();
            messages = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(author);
            out.writeString(authorType);
            out.writeString(type);
            out.writeString(name);
            out.writeString(link);
            out.writeString(wholeDate);
            out.writeString(messages);
        }
    }

    private static class ConvertStringToString implements StringModifier {
        private final String stringToRemplace;
        private final String stringNew;

        ConvertStringToString(String newStringToRemplace, String newStringNew) {
            stringToRemplace = newStringToRemplace;
            stringNew = newStringNew;
        }

        @Override
        public String changeString(String baseString) {
            return baseString.replace(stringToRemplace, stringNew);
        }
    }

    private static class ConvertRegexpToString implements StringModifier {
        private final String regexpToRemplace;
        private final String stringNew;

        ConvertRegexpToString(String newRegexpToRemplace, String newStringNew) {
            regexpToRemplace = newRegexpToRemplace;
            stringNew = newStringNew;
        }

        @Override
        public String changeString(String baseString) {
            return baseString.replaceAll(regexpToRemplace, stringNew);
        }
    }

    private static class MakeLinkIfPossible implements StringModifier {
        @Override
        public String changeString(String baseString) {
            if ((baseString.startsWith("http://") || baseString.startsWith("https://")) && !baseString.contains(" ")) {
                baseString = "<a href=\"" + baseString + "\">" + baseString + "</a>";
            }
            return baseString;
        }
    }

    private static class RemoveFirstsAndLastsP implements StringModifier {
        @Override
        public String changeString(String baseString) {
            baseString = baseString.trim();
            while (baseString.startsWith("<p>")) {
                baseString = baseString.substring(3).trim();
            }
            while (baseString.endsWith("</p>")) {
                baseString = baseString.substring(0, baseString.length() - 4).trim();
            }
            return baseString;
        }
    }

    public static class ForumAndTopicName {
        public String forum = "";
        public String topic = "";
    }

    public static class NameAndLink {
        public String name = "";
        public String link = "";
    }

    public static class AjaxInfos {
        public String list = null;
        public String mod = null;
    }

    public static class Settings {
        public String pseudoOfUser = "";
        public String firstLineFormat;
        public String secondLineFormat;
        public String addBeforeEdit;
        public String addAfterEdit;
        public String colorPseudoUser;
        public String colorPseudoOther;
        public String colorPseudoModo;
        public String colorPseudoAdmin;
        public int maxNumberOfOverlyQuotes = 0;
        public boolean showNoelshackImages = false;
    }

    private interface StringModifier {
        String changeString(String baseString);
    }
}
