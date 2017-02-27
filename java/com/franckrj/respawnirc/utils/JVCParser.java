package com.franckrj.respawnirc.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JVCParser {
    private static final Pattern ajaxListTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_liste_(messages|topics)\" id=\"ajax_timestamp_liste_(messages|topics)\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxListHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_liste_(messages|topics)\" id=\"ajax_hash_liste_(messages|topics)\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxModTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_moderation_forum\" id=\"ajax_timestamp_moderation_forum\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxModHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_moderation_forum\" id=\"ajax_hash_moderation_forum\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxPrefTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_preference_user\" id=\"ajax_timestamp_preference_user\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxPrefHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_preference_user\" id=\"ajax_hash_preference_user\" value=\"([^\"]*)\" />");
    private static final Pattern messageQuotePattern = Pattern.compile("\"txt\":\"(.*)\"", Pattern.DOTALL);
    private static final Pattern entireMessagePattern = Pattern.compile("(<div class=\"bloc-message-forum \".*?)(<span id=\"post_[^\"]*\" class=\"bloc-message-forum-anchor\">|<div class=\"bloc-outils-plus-modo bloc-outils-bottom\">|<div class=\"bloc-pagi-default\">)", Pattern.DOTALL);
    private static final Pattern signaturePattern = Pattern.compile("<div class=\"signature-msg[^\"]*\">(.*?)</div>", Pattern.DOTALL);
    private static final Pattern avatarPattern = Pattern.compile("<img src=\"[^\"]*\" data-srcset=\"(http:)?//([^\"]*)\" class=\"user-avatar-msg\"", Pattern.DOTALL);
    private static final Pattern entireTopicPattern = Pattern.compile("<li class=\"\" data-id=\"[^\"]*\">[^<]*<span class=\"topic-subject\">.*?</li>", Pattern.DOTALL);
    private static final Pattern pseudoInfosPattern = Pattern.compile("<span class=\"JvCare [^ ]* bloc-pseudo-msg text-([^\"]*)\" target=\"_blank\">[^a-zA-Z0-9_\\[\\]-]*([a-zA-Z0-9_\\[\\]-]*)[^<]*</span>");
    private static final Pattern messagePattern = Pattern.compile("<div class=\"bloc-contenu\"><div class=\"txt-msg  text-[^-]*-forum \">((.*?)(?=<div class=\"info-edition-msg\">)|(.*?)(?=<div class=\"signature-msg)|(.*))", Pattern.DOTALL);
    private static final Pattern currentPagePattern = Pattern.compile("<span class=\"page-active\">([^<]*)</span>");
    private static final Pattern pageLinkPattern = Pattern.compile("<span><a href=\"([^\"]*)\" class=\"lien-jv\">([0-9]*)</a></span>");
    private static final Pattern topicFormPattern = Pattern.compile("(<form role=\"form\" class=\"form-post-topic[^\"]*\" method=\"post\" action=\"\".*?>.*?</form>)", Pattern.DOTALL);
    private static final Pattern inputFormPattern = Pattern.compile("<input (type|name|value)=\"([^\"]*)\" (type|name|value)=\"([^\"]*)\" (type|name|value)=\"([^\"]*)\"/>");
    private static final Pattern dateMessagePattern = Pattern.compile("<div class=\"bloc-date-msg\">([^<]*<span class=\"JvCare [^ ]* lien-jv\" target=\"_blank\">)?[^a-zA-Z0-9]*([^ ]* [^ ]* [^ ]* [^ ]* ([0-9:]*))");
    private static final Pattern messageIDPattern = Pattern.compile("<div class=\"bloc-message-forum \" data-id=\"([^\"]*)\">");
    private static final Pattern unicodeInTextPattern = Pattern.compile("\\\\u([a-zA-Z0-9]{4})");
    private static final Pattern errorPattern = Pattern.compile("<div class=\"alert-row\">([^<]*)</div>");
    private static final Pattern errorInEditModePattern = Pattern.compile("\"erreur\":\\[\"([^\"]*)\"");
    private static final Pattern codeBlockPattern = Pattern.compile("<pre class=\"pre-jv\"><code class=\"code-jv\">([^<]*)</code></pre>");
    private static final Pattern codeLinePattern = Pattern.compile("<code class=\"code-jv\">(.*?)</code>", Pattern.DOTALL);
    private static final Pattern spoilLinePattern = Pattern.compile("<span class=\"bloc-spoil-jv en-ligne\">.*?<span class=\"contenu-spoil\">(.*?)</span></span>", Pattern.DOTALL);
    private static final Pattern spoilBlockPattern = Pattern.compile("<span class=\"bloc-spoil-jv\">.*?<span class=\"contenu-spoil\">(.*?)</span></span>", Pattern.DOTALL);
    private static final Pattern spoilOverlyPattern = Pattern.compile("(<span class=\"bloc-spoil-jv[^\"]*\">.*?<span class=\"contenu-spoil\">|</span></span>)", Pattern.DOTALL);
    private static final Pattern stickerPattern = Pattern.compile("<img class=\"img-stickers\" src=\"(http://jv.stkr.fr/p[^/]*/([^\"]*))\"/>");
    private static final Pattern pageTopicLinkNumberPattern = Pattern.compile("^(http://www.jeuxvideo.com/forums/[0-9]*-([0-9]*)-([0-9]*)-)([0-9]*)(-[0-9]*-[0-9]*-[0-9]*-[^.]*.htm)");
    private static final Pattern pageForumLinkNumberPattern = Pattern.compile("^(http://www.jeuxvideo.com/forums/[0-9]*-([0-9]*)-[0-9]*-[0-9]*-[0-9]*-)([0-9]*)(-[0-9]*-[^.]*.htm)");
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
    private static final Pattern forumInSearchPagePattern = Pattern.compile("<a class=\"list-search-forum-name\" href=\"([^\"]*)\"[^>]*>(.*?)</a>");
    private static final Pattern isInFavPattern = Pattern.compile("<span class=\"picto-favoris([^\"]*)\"");
    private static final Pattern topicIDInTopicPagePattern = Pattern.compile("<div (.*?)data-topic-id=\"([^\"]*)\">");
    private static final Pattern lockReasonPattern = Pattern.compile("<div class=\"message-lock-topic\">[^<]*<span>([^<]*)</span>");
    private static final Pattern surveyTitlePattern = Pattern.compile("<div class=\"intitule-sondage\">([^<]*)</div>");
    private static final Pattern surveyResultPattern = Pattern.compile("<div class=\"pied-result\">([^<]*)</div>");
    private static final Pattern surveyReplyPattern = Pattern.compile("<td class=\"result-pourcent\">[^<]*<div class=\"pourcent\">([^<]*)</div>.*?<td class=\"reponse\">([^<]*)</td>", Pattern.DOTALL);
    private static final Pattern realSurveyContentPattern = Pattern.compile("\"html\":\"(.*?)\"\\}");
    private static final Pattern numberOfMpJVCPattern = Pattern.compile("<span[^c]*class=\"account-number-mp[^\"]*\".*?data-val=\"([^\"]*)\"", Pattern.DOTALL);
    private static final Pattern overlyJVCQuotePattern = Pattern.compile("(<blockquote class=\"blockquote-jv\">|</blockquote>)");
    private static final Pattern overlyBetterQuotePattern = Pattern.compile("<(/)?blockquote>");
    private static final Pattern jvcLinkPattern = Pattern.compile("<a href=\"([^\"]*)\"( title=\"[^\"]*\")?>.*?</a>");
    private static final Pattern shortLinkPattern = Pattern.compile("<span class=\"JvCare [^\"]*\" rel=\"nofollow[^\"]*\" target=\"_blank\">([^<]*)</span>");
    private static final Pattern longLinkPattern = Pattern.compile("<span class=\"JvCare [^\"]*\"[^i]*itle=\"([^\"]*)\">[^<]*<i></i><span>[^<]*</span>[^<]*</span>");
    private static final Pattern smileyPattern = Pattern.compile("<img src=\"//image.jeuxvideo.com/smileys_img/([^\"]*)\" alt=\"[^\"]*\" data-def=\"SMILEYS\" data-code=\"([^\"]*)\" title=\"[^\"]*\" />");
    private static final Pattern youtubeVideoPattern = Pattern.compile("<div class=\"player-contenu\"><div class=\"[^\"]*\"><iframe .*? src=\"http(s)?://www.youtube.com/embed/([^\"]*)\"[^>]*></iframe></div></div>");
    private static final Pattern surroundedBlockquotePattern = Pattern.compile("(<br /> *)*(<(/)?blockquote>)( *<br />)*");
    private static final Pattern noelshackImagePattern = Pattern.compile("<a href=\"([^\"]*)\" data-def=\"NOELSHACK\" target=\"_blank\"><img class=\"img-shack\" .*? src=\"//([^\"]*)\" [^>]*></a>");
    private static final Pattern htmlTagPattern = Pattern.compile("<.+?>");
    private static final Pattern multipleSpacesPattern = Pattern.compile(" +");

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
            urlToChange = "http://" + urlToChange.substring(("https://").length());
        } else if (!urlToChange.startsWith("http://")) {
            urlToChange = "http://" + urlToChange;
        }

        if (urlToChange.startsWith("http://m.jeuxvideo.com/")) {
            urlToChange = "http://www.jeuxvideo.com/" + urlToChange.substring(("http://m.jeuxvideo.com/").length());
        } else if (urlToChange.startsWith("http://jeuxvideo.com/")) {
            urlToChange = "http://www.jeuxvideo.com/" + urlToChange.substring(("http://jeuxvideo.com/").length());
        }

        return urlToChange;
    }

    public static String noelshackToDirectLink(String baseLink) {
        if (baseLink.contains("noelshack.com/")) {
            baseLink = baseLink.substring(baseLink.indexOf("noelshack.com/") + 14);
        } else {
            return baseLink;
        }

        if (baseLink.startsWith("fichiers/") || baseLink.startsWith("minis/")) {
            baseLink = baseLink.substring(baseLink.indexOf("/") + 1);
        } else {
            baseLink = baseLink.replaceFirst("-", "/").replaceFirst("-", "/");
        }

        return "http://image.noelshack.com/fichiers/" + baseLink;
    }

    public static boolean checkIfItsNoelshackLink(String linkToCheck) {
        if (!linkToCheck.contains(".php")) {
            if (linkToCheck.startsWith("http://") || linkToCheck.startsWith("https://")) {
                linkToCheck = linkToCheck.substring(linkToCheck.indexOf("://") + 3);
            }

            return linkToCheck.startsWith("image.noelshack.com/") || linkToCheck.startsWith("www.noelshack.com/") ||
                    linkToCheck.startsWith("noelshack.com/");
        } else {
            return false;
        }
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
        return linkToCheck.startsWith("http://www.jeuxvideo.com/forums/0-");
    }

    public static boolean checkIfItsJVCLink(String linkToCheck) {
        return linkToCheck.startsWith("http://www.jeuxvideo.com/forums/0-") ||
                linkToCheck.startsWith("http://www.jeuxvideo.com/forums/1-") ||
                linkToCheck.startsWith("http://www.jeuxvideo.com/forums/42-");
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

    public static String getForumIDOfThisForum(String forumLink) {
        Matcher forumLinkNumberMatcher = pageForumLinkNumberPattern.matcher(forumLink);

        if (forumLinkNumberMatcher.find()) {
            return forumLinkNumberMatcher.group(2);
        } else {
            return "";
        }
    }

    public static String getForumIDOfThisTopic(String topicLink) {
        Matcher topicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(topicLink);

        if (topicLinkNumberMatcher.find()) {
            return topicLinkNumberMatcher.group(2);
        } else {
            return "";
        }
    }

    public static String getTopicIDInThisTopicPage(String topicContent) {
        Matcher topicIDInTopicPageMatcher = topicIDInTopicPagePattern.matcher(topicContent);

        if (topicIDInTopicPageMatcher.find()) {
            return topicIDInTopicPageMatcher.group(2);
        } else {
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

    public static SurveyInfos getSurveyInfosFromSurveyBlock(String surveyBlock) {
        SurveyInfos currentInfos = new SurveyInfos();
        Matcher surveyTitleMatcher = surveyTitlePattern.matcher(surveyBlock);
        Matcher surveyResultMatcher = surveyResultPattern.matcher(surveyBlock);
        Matcher surveyReplyMatcher = surveyReplyPattern.matcher(surveyBlock);
        int lastOffset = 0;

        if (surveyTitleMatcher.find()) {
            currentInfos.htmlTitle = surveyTitleMatcher.group(1);
        }
        if (surveyResultMatcher.find()) {
            currentInfos.numberOfVotes = surveyResultMatcher.group(1).replace("\n", "").replaceAll(" +", " ").trim();
        }
        currentInfos.isOpen = !surveyBlock.contains("<span>Sondage fermé</span>");

        while (surveyReplyMatcher.find(lastOffset)) {
            SurveyInfos.SurveyReply replyForSurvey = new SurveyInfos.SurveyReply();

            replyForSurvey.percentageOfVotes = surveyReplyMatcher.group(1);
            replyForSurvey.htmlTitle = surveyReplyMatcher.group(2);

            currentInfos.listOfReplys.add(replyForSurvey);
            lastOffset = surveyReplyMatcher.end();
        }

        return currentInfos;
    }

    public static ArrayList<NameAndLink> getListOfForumsInSearchPage(String pageSource) {
        ArrayList<NameAndLink> listOfForums = new ArrayList<>();
        Matcher forumInSearchPageMatcher = forumInSearchPagePattern.matcher(pageSource);
        int lastOffset = 0;

        while (forumInSearchPageMatcher.find(lastOffset)) {
            NameAndLink newNameAndLink = new NameAndLink();

            newNameAndLink.name = forumInSearchPageMatcher.group(2).replace("<em>", "").replace("</em>", "");
            if (!forumInSearchPageMatcher.group(1).isEmpty()) {
                newNameAndLink.link = "http://www.jeuxvideo.com" + forumInSearchPageMatcher.group(1);
            }

            listOfForums.add(newNameAndLink);
            lastOffset = forumInSearchPageMatcher.end();
        }

        return listOfForums;
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
            newFav.name = specialCharToNormalChar(favMatcher.group(2));
            newFav.link = favMatcher.group(1);
            if (newFav.link.startsWith("/forums/")) {
                newFav.link = "http://www.jeuxvideo.com" + newFav.link;
            } else if (!newFav.link.startsWith("http:")) {
                newFav.link = "http:" + newFav.link;
            }
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
            forumName = specialCharToNormalChar(forumName.trim());
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

            currentNames.forum = specialCharToNormalChar(currentNames.forum.trim());
            currentNames.topic = specialCharToNormalChar(currentNames.topic.trim());
        }

        return currentNames;
    }

    public static String getNumberOfMPFromPage(String pageSource) {
        Matcher numberOfMpJVCMatcher = numberOfMpJVCPattern.matcher(pageSource);

        if (numberOfMpJVCMatcher.find()) {
            return numberOfMpJVCMatcher.group(1);
        } else {
            return null;
        }
    }

    public static Boolean getIsInFavsFromPage(String pageSource) {
        Matcher isInFavMatcher = isInFavPattern.matcher(pageSource);

        if (isInFavMatcher.find()) {
            return !isInFavMatcher.group(1).isEmpty();
        } else {
            return null;
        }
    }

    public static String getLockReasonFromPage(String pageSource) {
        Matcher lockReasonMatcher = lockReasonPattern.matcher(pageSource);

        if (lockReasonMatcher.find()) {
            return specialCharToNormalChar(lockReasonMatcher.group(1).replace("\n", " "));
        } else {
            return null;
        }
    }

    public static String getSurveyHTMLTitleFromPage(String pageSource) {
        Matcher surveyTitleMatcher = surveyTitlePattern.matcher(pageSource);

        if (surveyTitleMatcher.find()) {
            return surveyTitleMatcher.group(1);
        } else {
            return "";
        }
    }

    public static String getRealSurveyContent(String pageSource) {
        Matcher realSurveyContentMatcher = realSurveyContentPattern.matcher(pageSource);

        if (realSurveyContentMatcher.find()) {
            return parsingAjaxMessages(realSurveyContentMatcher.group(1));
        } else {
            return "";
        }
    }

    public static String getErrorMessage(String pageSource) {
        Matcher errorMatcher = errorPattern.matcher(pageSource);

        if (errorMatcher.find()) {
            return "Erreur : " + errorMatcher.group(1);
        } else {
            return "Erreur : le message n'a pas été envoyé.";
        }
    }

    public static String getErrorMessageInJSONMode(String pageSource) {
        Matcher errorMatcher = errorInEditModePattern.matcher(pageSource);

        if (errorMatcher.find()) {
            return "Erreur : " + specialCharToNormalChar(parsingAjaxMessages(errorMatcher.group(1)));
        } else {
            return null;
        }
    }

    public static String getMessageEdit(String pageSource) {
        Matcher messageEditInfoMatcher = messageEditInfoPattern.matcher(pageSource);

        if (messageEditInfoMatcher.find()) {
            return specialCharToNormalChar(messageEditInfoMatcher.group(1));
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

        return ajaxMessage;
    }

    public static String getMessageQuoted(String pageSource) {
        Matcher messageQuoteMatcher = messageQuotePattern.matcher(pageSource);

        if (messageQuoteMatcher.find()) {
            return specialCharToNormalChar(parsingAjaxMessages(messageQuoteMatcher.group(1)).replace("\n", "\n>"));
        }

        return "";
    }

    public static AjaxInfos getAllAjaxInfos(String pageSource) {
        AjaxInfos newAjaxInfos = new AjaxInfos();

        Matcher ajaxListTimestampMatcher = ajaxListTimestampPattern.matcher(pageSource);
        Matcher ajaxListHashMatcher = ajaxListHashPattern.matcher(pageSource);
        Matcher ajaxModTimestampMatcher = ajaxModTimestampPattern.matcher(pageSource);
        Matcher ajaxModHashMatcher = ajaxModHashPattern.matcher(pageSource);
        Matcher ajaxPrefTimestampMatcher = ajaxPrefTimestampPattern.matcher(pageSource);
        Matcher ajaxPrefHashMatcher = ajaxPrefHashPattern.matcher(pageSource);

        if (ajaxListTimestampMatcher.find() && ajaxListHashMatcher.find()) {
            newAjaxInfos.list = "ajax_timestamp=" + ajaxListTimestampMatcher.group(3) + "&ajax_hash=" + ajaxListHashMatcher.group(3);
        }

        if (ajaxModTimestampMatcher.find() && ajaxModHashMatcher.find()) {
            newAjaxInfos.mod = "ajax_timestamp=" + ajaxModTimestampMatcher.group(1) + "&ajax_hash=" + ajaxModHashMatcher.group(1);
        }

        if (ajaxPrefTimestampMatcher.find() && ajaxPrefHashMatcher.find()) {
            newAjaxInfos.pref = "ajax_timestamp=" + ajaxPrefTimestampMatcher.group(1) + "&ajax_hash=" + ajaxPrefHashMatcher.group(1);
        }

        return newAjaxInfos;
    }

    public static String getListOfInputInAStringInTopicFormForThisPage(String pageSource) {
        Matcher topicFormMatcher = topicFormPattern.matcher(pageSource);

        if (topicFormMatcher.find()) {
            return getListOfInputInAStringInThisForm(topicFormMatcher.group(1));
        }

        return "";
    }

    public static String getListOfInputInAStringInThisForm(String thisForm) {
        Matcher inputFormMatcher = inputFormPattern.matcher(thisForm);
        StringBuilder allInputInAString = new StringBuilder();

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

        finalMessage = finalMessage.replace("<%MESSAGE_MESSAGE%>", parseMessageToPrettyMessage(thisMessageInfo.messageNotParsed, settings, thisMessageInfo.containSpoil, thisMessageInfo.showSpoil, thisMessageInfo.showOverlyQuote, thisMessageInfo.showUglyImages));
        if (!thisMessageInfo.lastTimeEdit.isEmpty()) {
            finalMessage = finalMessage.replace("<%EDIT_ALL%>", settings.addBeforeEdit + thisMessageInfo.lastTimeEdit.trim() + settings.addAfterEdit);
        } else {
            finalMessage = finalMessage.replace("<%EDIT_ALL%>", "");
        }
        finalMessage = surroundedBlockquotePattern.matcher(finalMessage).replaceAll("$2");

        return finalMessage;
    }

    public static String createSignatureFromInfos(MessageInfos thisMessageInfo, Settings settings) {
        return "<small>" + parseMessageToPrettyMessage(thisMessageInfo.signatureNotParsed, settings, true, false, true, true) + "</small>";
    }

    public static String parseMessageToPrettyMessage(String messageInString, Settings settings, boolean containSpoil, boolean showSpoil, boolean showOverlyQuote, boolean showUglyImages) {
        StringBuilder messageInBuilder = new StringBuilder(messageInString);
        MakeShortenedLinkIfPossible makeLinkDependingOnSettingsAndForceMake = new MakeShortenedLinkIfPossible((settings.shortenLongLink ? 50 : 0), true);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, codeBlockPattern, 1, "<p><font face=\"monospace\">", "</font></p>", new ConvertStringToString("\n", "<br />"), new ConvertStringToString("  ", "  ")); //remplace les doubles espaces par des doubles alt+255
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, codeLinePattern, 1, "<font face=\"monospace\">", "</font>", new ConvertStringToString("  ", "  "), null); //remplace les doubles espaces par des doubles alt+255
        StickerConverter.convertStickerWithThisRule(messageInBuilder, StickerConverter.ruleForNoLangageSticker);
        if (settings.transformStickerToSmiley) {
            StickerConverter.convertStickerWithThisRule(messageInBuilder, StickerConverter.ruleForStickerToSmiley);
        }
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, stickerPattern, 2, "<img src=\"sticker_", ".png\"/>", new ConvertStringToString("-", "_"), null);

        ToolForParsing.replaceStringByAnother(messageInBuilder, "\n", "");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "\r", "");
        ToolForParsing.parseListInMessageIfNeeded(messageInBuilder);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, smileyPattern, 1, "<img src=\"smiley_", "\"/>", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, youtubeVideoPattern, 2, "<a href=\"http://youtu.be/", "\">http://youtu.be/", 2, "</a>");

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, jvcLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, shortLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, longLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake, null);

        if (settings.hideUglyImages && !showUglyImages) {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, noelshackImagePattern, 0, "", "", new SuppressIfContainUglyNames(), null);
        }

        if (settings.showNoelshackImages) {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, noelshackImagePattern, 1, "<a href=\"", "\"><img src=\"http://", 2, "\"/></a>");
        } else {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, noelshackImagePattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake, null);
        }

        if (containSpoil) {
            ToolForParsing.removeOverlySpoils(messageInBuilder);
            if (!showSpoil) {
                ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, spoilLinePattern, 1, "", "", new ConvertRegexpToString("<.+?>", " "), new ConvertRegexpToString("(?s).", "█"));
                ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, spoilBlockPattern, 1, "<p>", "</p>", new ConvertRegexpToString("<.+?>", " "), new ConvertRegexpToString("(?s).", "█"));
            } else {
                ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, spoilLinePattern, 1, "<font color=\"#" + (ThemeManager.getThemeUsedIsDark() ? "FFFFFF" : "000000") + "\">", "</font>", new RemoveFirstsAndLastsP(), null);
                ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, spoilBlockPattern, 1, "<p><font color=\"#" + (ThemeManager.getThemeUsedIsDark() ? "FFFFFF" : "000000") + "\">", "</font></p>",  new RemoveFirstsAndLastsP(), null);
            }
        }

        ToolForParsing.replaceStringByAnother(messageInBuilder, "<blockquote class=\"blockquote-jv\">", "<blockquote>");
        ToolForParsing.removeDivAndAdaptParagraphInMessage(messageInBuilder);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, surroundedBlockquotePattern, 2, "", "", null, null);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, jvCarePattern, 1, "", "", new MakeShortenedLinkIfPossible((settings.shortenLongLink ? 50 : 0), false), null);

        ToolForParsing.removeFirstAndLastBrInMessage(messageInBuilder);

        if (!showOverlyQuote) {
            ToolForParsing.removeOverlyQuoteInPrettyMessage(messageInBuilder, settings.maxNumberOfOverlyQuotes);
        }

        if (!settings.pseudoOfUser.isEmpty()) {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, Pattern.compile("(?i)" + settings.pseudoOfUser.replace("[", "\\[").replace("]", "\\]") + "(?![^<>]*(>|</a>))"), 0, "<font color=\"" + settings.colorPseudoUser + "\">", "</font>", null, null);
        }

        return messageInBuilder.toString();
    }

    public static String parseMessageToSimpleMessage(String messageInString) {
        StringBuilder messageInBuilder = new StringBuilder(messageInString);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, stickerPattern, 2, "[[sticker:p/", "]]", null, null);
        ToolForParsing.replaceStringByAnother(messageInBuilder, "\n", "");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "\r", "");
        ToolForParsing.parseListInMessageIfNeeded(messageInBuilder);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, smileyPattern, 2, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, youtubeVideoPattern, 2, "http://youtu.be/", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, jvcLinkPattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, shortLinkPattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, longLinkPattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, noelshackImagePattern, 1, "", "", null, null);
        ToolForParsing.replaceStringByAnother(messageInBuilder, "<blockquote class=\"blockquote-jv\">", "<blockquote>");
        ToolForParsing.removeDivAndAdaptParagraphInMessage(messageInBuilder);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, surroundedBlockquotePattern, -1, "<br /><br />", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, jvCarePattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, multipleSpacesPattern, -1, " ", "", null, null);
        ToolForParsing.removeFirstAndLastBrInMessage(messageInBuilder);

        ToolForParsing.replaceStringByAnother(messageInBuilder, "<br />", "\n");
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, htmlTagPattern, -1, "", "", null, null);
        return specialCharToNormalChar(messageInBuilder.toString());
    }

    public static MessageInfos createMessageInfoFromEntireMessage(String thisEntireMessage) {
        MessageInfos newMessageInfo = new MessageInfos();
        Matcher pseudoInfosMatcher = pseudoInfosPattern.matcher(thisEntireMessage);
        Matcher messageMatcher = messagePattern.matcher(thisEntireMessage);
        Matcher signatureMatcher = signaturePattern.matcher(thisEntireMessage);
        Matcher avatarMatcher = avatarPattern.matcher(thisEntireMessage);
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

        if (signatureMatcher.find()) {
            newMessageInfo.signatureNotParsed = signatureMatcher.group(1);
        } else {
            newMessageInfo.signatureNotParsed = "";
        }

        if (avatarMatcher.find()) {
            newMessageInfo.avatarLink = "http://" + avatarMatcher.group(2);
        } else {
            newMessageInfo.avatarLink = "";
        }

        if (messageMatcher.find() && messageIDMatcher.find() && dateMessageMatcher.find()) {
            newMessageInfo.messageNotParsed = messageMatcher.group(1);
            newMessageInfo.dateTime = dateMessageMatcher.group(3);
            newMessageInfo.wholeDate = dateMessageMatcher.group(2);
            newMessageInfo.containSpoil = newMessageInfo.messageNotParsed.contains("<span class=\"contenu-spoil\">");
            newMessageInfo.numberOfOverlyQuote = ToolForParsing.countNumberOfOverlyQuoteInNotPrettyMessage(newMessageInfo.messageNotParsed);
            newMessageInfo.containUglyImages = ToolForParsing.hasUglyImagesInNotPrettyMessage(newMessageInfo.messageNotParsed);
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
            newTopicInfo.htmlName = topicNameAndLinkString.substring(topicNameAndLinkString.indexOf("title=\"") + 7);
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

    public static String specialCharToNormalChar(String baseMessage) {
        return baseMessage.replace("&amp;", "&").replace("&quot;", "\"").replace("&#039;", "\'").replace("&lt;", "<").replace("&gt;", ">");
    }

    public static class ToolForParsing {
        private static final Pattern uolistOpenTagPattern = Pattern.compile("<(ul|ol)[^>]*>");
        private static final Pattern divOpenTagPattern = Pattern.compile("<div[^>]*>");
        private static final Pattern largeParagraphePattern = Pattern.compile("(<br /> *){0,2}</p> *<p>( *<br />){0,2}");
        private static final Pattern surroundedParagraphePattern = Pattern.compile("<br /> *<(/)?p> *<br />");
        private static final Pattern leftParagraphePattern = Pattern.compile("(<br /> *){1,2}<(/)?p>");
        private static final Pattern rightParagraphePattern = Pattern.compile("<(/)?p>(<br /> *){1,2}");
        private static final Pattern smallParagraphePattern = Pattern.compile("<(/)?p>");

        public static void parseListInMessageIfNeeded(StringBuilder message) {
            if (message.indexOf("<li>") != -1) {
                ToolForParsing.parseThisMessageWithThisPattern(message, uolistOpenTagPattern, -1, "<p>", "", null, null);
                ToolForParsing.replaceStringByAnother(message, "</ul>", "</p>");
                ToolForParsing.replaceStringByAnother(message, "</ol>", "</p>");
                ToolForParsing.replaceStringByAnother(message, "<li><p><li>", "<li><li>");
                ToolForParsing.replaceStringByAnother(message, "<li><p><li>", "<li><li>");
                ToolForParsing.replaceStringByAnother(message, "<li>", " • ");
                ToolForParsing.replaceStringByAnother(message, "</li></p></li>", "</li>");
                ToolForParsing.replaceStringByAnother(message, "</li></p></li>", "</li>");
                ToolForParsing.replaceStringByAnother(message, "</li>", "<br />");
            }
        }

        public static void removeDivAndAdaptParagraphInMessage(StringBuilder message) {
            ToolForParsing.parseThisMessageWithThisPattern(message, divOpenTagPattern, -1, "", "", null, null);
            ToolForParsing.replaceStringByAnother(message, "</div>", "");
            ToolForParsing.parseThisMessageWithThisPattern(message, largeParagraphePattern, -1, "<br /><br />", "", null, null);
            ToolForParsing.parseThisMessageWithThisPattern(message, surroundedParagraphePattern, -1, "<br /><br />", "", null, null);
            ToolForParsing.parseThisMessageWithThisPattern(message, leftParagraphePattern, -1, "<br /><br />", "", null, null);
            ToolForParsing.parseThisMessageWithThisPattern(message, rightParagraphePattern, -1, "<br /><br />", "", null, null);
            ToolForParsing.parseThisMessageWithThisPattern(message, smallParagraphePattern, -1, "<br /><br />", "", null, null);
        }

        public static void removeFirstAndLastBrInMessage(StringBuilder message) {
            trimStringBuilder(message);

            while (startsWithStringBuilder(message, "<br />")) {
                message.delete(0, 6);
                trimStringBuilder(message);
            }

            while (endsWithStringBuilder(message, "<br />")) {
                message.delete(message.length() - 6, message.length());
                trimStringBuilder(message);
            }
        }

        public static void removeOverlySpoils(StringBuilder baseMessage) {
            Matcher spoilOverlyMatcher = spoilOverlyPattern.matcher(baseMessage);
            int currentSpoilTagDeepness = 0;
            int lastOffsetOfTag = 0;

            while (spoilOverlyMatcher.find(lastOffsetOfTag)) {
                boolean itsEndingTag = spoilOverlyMatcher.group().equals("</span></span>");

                if (!itsEndingTag) {
                    ++currentSpoilTagDeepness;
                }

                if (currentSpoilTagDeepness > 1) {
                    lastOffsetOfTag = spoilOverlyMatcher.start();
                    baseMessage.delete(spoilOverlyMatcher.start(), spoilOverlyMatcher.end());
                    spoilOverlyMatcher = spoilOverlyPattern.matcher(baseMessage);
                } else {
                    lastOffsetOfTag = spoilOverlyMatcher.end();
                }

                if (itsEndingTag) {
                    --currentSpoilTagDeepness;

                    if (currentSpoilTagDeepness < 0) {
                        currentSpoilTagDeepness = 0;
                    }
                }
            }
        }

        public static void removeOverlyQuoteInPrettyMessage(StringBuilder prettyMessage, int maxNumberOfOverlyQuotes) {
            Matcher htmlTagMatcher = overlyBetterQuotePattern.matcher(prettyMessage);
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
                    Matcher secHtmlTagMatcher = overlyBetterQuotePattern.matcher(prettyMessage);
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
                        prettyMessage.replace(htmlTagMatcher.end(), secHtmlTagMatcher.start(), "[...]");
                        htmlTagMatcher = overlyBetterQuotePattern.matcher(prettyMessage);
                    }
                }
            }
        }

        public static int countNumberOfOverlyQuoteInNotPrettyMessage(String notPrettyMessage) {
            Matcher htmlTagMatcher = overlyJVCQuotePattern.matcher(notPrettyMessage);
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

        public static boolean hasUglyImagesInNotPrettyMessage(String notPrettyMessage) {
            Matcher noelshackImageMatcher = noelshackImagePattern.matcher(notPrettyMessage);
            int lastOffsetOfTag = 0;

            while (noelshackImageMatcher.find(lastOffsetOfTag)) {
                String currentMatch = noelshackImageMatcher.group();
                if (currentMatch.contains("risitas") || currentMatch.contains("jesus") || currentMatch.contains("issou")) {
                    return true;
                }

                lastOffsetOfTag = noelshackImageMatcher.end();
            }

            return false;
        }

        public static void parseThisMessageWithThisPattern(StringBuilder messageToParse, Pattern patternToUse, int groupToUse, String stringBefore, String stringAfter, StringModifier firstModifier, StringModifier secondModifier) {
            Matcher matcherToUse = patternToUse.matcher(messageToParse);
            int lastOffset = 0;

            while (matcherToUse.find(lastOffset)) {
                StringBuilder newMessage = new StringBuilder(stringBefore);
                String messageToUse = (groupToUse == -1 ? "" : matcherToUse.group(groupToUse));

                if (firstModifier != null) {
                    messageToUse = firstModifier.changeString(messageToUse);
                }
                if (secondModifier != null) {
                    messageToUse = secondModifier.changeString(messageToUse);
                }

                newMessage.append(messageToUse).append(stringAfter);

                messageToParse.replace(matcherToUse.start(), matcherToUse.end(), newMessage.toString());
                lastOffset = matcherToUse.start() + newMessage.length();
                matcherToUse = patternToUse.matcher(messageToParse);
            }
        }

        public static void parseThisMessageWithThisPattern(StringBuilder messageToParse, Pattern patternToUse, int groupToUse, String stringBefore, String stringAfter, int secondGroupToUse, String stringAfterAfter) {
            Matcher matcherToUse = patternToUse.matcher(messageToParse);
            int lastOffset = 0;

            while (matcherToUse.find(lastOffset)) {
                StringBuilder newMessage = new StringBuilder(stringBefore);

                if (groupToUse != -1) {
                    newMessage.append(matcherToUse.group(groupToUse));
                }

                newMessage.append(stringAfter);

                if (secondGroupToUse != -1) {
                    newMessage.append(matcherToUse.group(secondGroupToUse));
                }

                newMessage.append(stringAfterAfter);

                messageToParse.replace(matcherToUse.start(), matcherToUse.end(), newMessage.toString());
                lastOffset = matcherToUse.start() + newMessage.length();
                matcherToUse = patternToUse.matcher(messageToParse);
            }
        }

        public static void replaceStringByAnother(StringBuilder builder, String from, String to)
        {
            int index = builder.indexOf(from);
            while (index != -1) {
                builder.replace(index, index + from.length(), to);
                index += to.length();
                index = builder.indexOf(from, index);
            }
        }

        public static void trimStringBuilder(StringBuilder builder) {
            while (builder.length() > 0 && builder.charAt(0) == ' ') {
                builder.delete(0, 1);
            }

            while (builder.length() > 0 && builder.charAt(builder.length() - 1) == ' ') {
                builder.delete(builder.length() - 1, builder.length());
            }
        }

        public static boolean startsWithStringBuilder(StringBuilder builder, String with) {
            if (builder.length() >= with.length()) {
                for (int indexStart = 0; indexStart < with.length(); ++indexStart) {
                    if (builder.charAt(indexStart) != with.charAt(indexStart)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public static boolean endsWithStringBuilder(StringBuilder builder, String with) {
            if (builder.length() >= with.length()) {
                for (int reverseIndexEnd = 0; reverseIndexEnd < with.length(); ++reverseIndexEnd) {
                    if (builder.charAt(builder.length() - 1 - reverseIndexEnd) != with.charAt(with.length() - 1 - reverseIndexEnd)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }

    public static class MessageInfos implements Parcelable, Comparable<MessageInfos> {
        public String pseudo = "";
        public String pseudoType = "";
        public String messageNotParsed = "";
        public String signatureNotParsed = "";
        public String avatarLink = "";
        public String dateTime = "";
        public String wholeDate = "";
        public String lastTimeEdit = "";
        public boolean containSpoil = false;
        public boolean showSpoil = false;
        public int numberOfOverlyQuote = 0;
        public boolean showOverlyQuote = false;
        public boolean isAnEdit = false;
        public boolean containUglyImages = false;
        public boolean showUglyImages = false;
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
            signatureNotParsed = in.readString();
            avatarLink = in.readString();
            dateTime = in.readString();
            wholeDate = in.readString();
            lastTimeEdit = in.readString();
            containSpoil = (in.readInt() == 1);
            showSpoil = (in.readInt() == 1);
            numberOfOverlyQuote = in.readInt();
            showOverlyQuote = (in.readInt() == 1);
            isAnEdit = (in.readInt() == 1);
            containUglyImages = (in.readInt() == 1);
            showUglyImages = (in.readInt() == 1);
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
            out.writeString(signatureNotParsed);
            out.writeString(avatarLink);
            out.writeString(dateTime);
            out.writeString(wholeDate);
            out.writeString(lastTimeEdit);
            out.writeInt(containSpoil ? 1 : 0);
            out.writeInt(showSpoil ? 1 : 0);
            out.writeInt(numberOfOverlyQuote);
            out.writeInt(showOverlyQuote ? 1 : 0);
            out.writeInt(isAnEdit ? 1 : 0);
            out.writeInt(containUglyImages ? 1 : 0);
            out.writeInt(showUglyImages ? 1 : 0);
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
        public String htmlName = "";
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
            htmlName = in.readString();
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
            out.writeString(htmlName);
            out.writeString(link);
            out.writeString(wholeDate);
            out.writeString(messages);
        }
    }

    public static class NameAndLink implements Parcelable {
        public String name = "";
        public String link = "";

        public static final Parcelable.Creator<NameAndLink> CREATOR = new Parcelable.Creator<NameAndLink>() {
            @Override
            public NameAndLink createFromParcel(Parcel in) {
                return new NameAndLink(in);
            }

            @Override
            public NameAndLink[] newArray(int size) {
                return new NameAndLink[size];
            }
        };

        public NameAndLink() {
            //rien
        }

        public NameAndLink(String newName, String newLink) {
            name = newName;
            link = newLink;
        }

        private NameAndLink(Parcel in) {
            name = in.readString();
            link = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(name);
            out.writeString(link);
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

    private static class RemoveFirstsAndLastsP implements StringModifier {
        @Override
        public String changeString(String baseString) {
            while (baseString.startsWith("<p>")) {
                baseString = baseString.substring(3);
            }
            while (baseString.endsWith("</p>")) {
                baseString = baseString.substring(0, baseString.length() - 4);
            }
            return baseString;
        }
    }

    private static class SuppressIfContainUglyNames implements StringModifier {
        @Override
        public String changeString(String baseString) {
            if (baseString.contains("jesus") || baseString.contains("risitas") || baseString.contains("issou")) {
                return "";
            }
            return baseString;
        }
    }

    private static class MakeShortenedLinkIfPossible implements StringModifier {
        final int maxStringSize;
        final boolean forceLinkCreation;

        MakeShortenedLinkIfPossible(int newMaxStringSize, boolean newForceLinkCreation) {
            maxStringSize = newMaxStringSize;
            forceLinkCreation = newForceLinkCreation;
        }

        @Override
        public String changeString(String baseString) {
            if (forceLinkCreation || ((baseString.startsWith("http://") || baseString.startsWith("https://")) && !baseString.contains(" "))) {
                String linkShowed = baseString;
                if (maxStringSize > 0 && linkShowed.length() > maxStringSize + 3) {
                    linkShowed = linkShowed.substring(0, maxStringSize / 2) + "[…]" + linkShowed.substring(linkShowed.length() - (maxStringSize / 2));
                }
                baseString = "<a href=\"" + baseString + "\">" + linkShowed + "</a>";
            }
            return baseString;
        }
    }

    public static class SurveyInfos {
        public boolean isOpen = true;
        public String htmlTitle = "";
        public String numberOfVotes = "";
        public ArrayList<SurveyReply> listOfReplys = new ArrayList<>();

        public static class SurveyReply {
            public String htmlTitle = "";
            public String percentageOfVotes = "";
        }
    }

    public static class ForumAndTopicName {
        public String forum = "";
        public String topic = "";
    }

    public static class AjaxInfos {
        public String list = null;
        public String mod = null;
        public String pref = null;
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
        public boolean transformStickerToSmiley = false;
        public boolean shortenLongLink = false;
        public boolean hideUglyImages = false;
    }

    private interface StringModifier {
        String changeString(String baseString);
    }
}
