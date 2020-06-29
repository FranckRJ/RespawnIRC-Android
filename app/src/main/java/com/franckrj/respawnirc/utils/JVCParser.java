package com.franckrj.respawnirc.utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JVCParser {
    public static final Pattern stickerPattern = Pattern.compile("<img class=\"img-stickers\" src=\"([^\"]*)\".*?/>");

    private static final Pattern ajaxListTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_liste_(messages|topics)\" id=\"ajax_timestamp_liste_(messages|topics)\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxListHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_liste_(messages|topics)\" id=\"ajax_hash_liste_(messages|topics)\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxModTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_moderation_forum\" id=\"ajax_timestamp_moderation_forum\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxModHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_moderation_forum\" id=\"ajax_hash_moderation_forum\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxPrefTimestampPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_timestamp_preference_user\" id=\"ajax_timestamp_preference_user\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxPrefHashPattern = Pattern.compile("<input type=\"hidden\" name=\"ajax_hash_preference_user\" id=\"ajax_hash_preference_user\" value=\"([^\"]*)\" />");
    private static final Pattern ajaxSubHashPattern = Pattern.compile("<body *data-abo-session=\"([^\"]*)\">");
    private static final Pattern messageQuotePattern = Pattern.compile("\"txt\":\"(.*)\"", Pattern.DOTALL);
    private static final Pattern entireMessagePattern = Pattern.compile("(<div class=\"bloc-message-forum[^\"]*\".*?)(<span id=\"post_[^\"]*\" class=\"bloc-message-forum-anchor\">|<div class=\"bloc-outils-plus-modo bloc-outils-bottom\">|<div class=\"bloc-pagi-default\">)", Pattern.DOTALL);
    private static final Pattern entireMessageInPermalinkPattern = Pattern.compile("(<div class=\"bloc-message-forum[^\"]*\".*?)<div class=\"bloc-return-topic\">", Pattern.DOTALL);
    private static final Pattern topicLinkInPermalinkPattern = Pattern.compile("<div class=\"bloc-return-topic\">[^<]*<a href=\"([^\"]*)");
    private static final Pattern signaturePattern = Pattern.compile("<div class=\"signature-msg[^\"]*\">(.*)", Pattern.DOTALL);
    private static final Pattern avatarPattern = Pattern.compile("<img src=\"[^\"]*\" data-srcset=\"(https?:)?//([^\"]*)\" class=\"user-avatar-msg\"", Pattern.DOTALL);
    private static final Pattern entireTopicPattern = Pattern.compile("<li (class=\"[^\"]*\" data-id=\"[^\"]*\"|class=\"message[^\"]*\")>.*?<span class=\"topic-subject\">.*?</li>", Pattern.DOTALL);
    private static final Pattern pseudoIsBlacklistedPattern = Pattern.compile("<div class=\"bloc-message-forum msg-pseudo-blacklist[^\"]*\" data-id=\"");
    private static final Pattern messageIsDeletedPattern = Pattern.compile("<div class=\"bloc-message-forum msg-supprime[^\"]*\" data-id=\"");
    private static final Pattern userCanDeleteOrRestoreMessagePattern = Pattern.compile("<span class=\"picto-msg-(croix|restaurer)\" title=\"(Supprimer|Restaurer)\" data-type=\"(delete|restore)\">");
    private static final Pattern userCanEditMessagePattern = Pattern.compile("<span class=\"picto-msg-crayon\" title=\"Editer\">");
    private static final Pattern userCanKickOrDekickAuthorPattern = Pattern.compile("<span class=\"picto-msg-(kick|dekick)\" title=\"(Kicker|Dékicker)\" data-id-alias=\"[^\"]*\">");
    private static final Pattern pseudoInfosPattern = Pattern.compile("<span class=\"JvCare [^ ]* bloc-pseudo-msg text-([^\"]*)\" target=\"_blank\">[^a-zA-Z0-9_\\[\\]-]*([a-zA-Z0-9_\\[\\]-]*)[^<]*</span>");
    private static final Pattern idAliasPattern = Pattern.compile("data-id-alias=\"([0-9]+)\">");
    private static final Pattern messagePattern = Pattern.compile("<div class=\"bloc-contenu\">[^<]*<div class=\"txt-msg +text-[^-]*-forum \">((.*?)(?=<div class=\"info-edition-msg\">)|(.*?)(?=<div class=\"signature-msg)|(.*))", Pattern.DOTALL);
    private static final Pattern currentPagePattern = Pattern.compile("<span class=\"page-active\">([^<]*)</span>");
    private static final Pattern pageLinkPattern = Pattern.compile("<span><a href=\"([^\"]*)\" class=\"lien-jv\">([0-9]*)</a></span>");
    private static final Pattern topicFormPattern = Pattern.compile("(<form role=\"form\" class=\"form-post-topic[^\"]*\" method=\"post\" action=\"[^\"]*\".*?>.*?</form>)", Pattern.DOTALL);
    private static final Pattern modoConnectFormPattern = Pattern.compile("(<form role=\"form\" action=\"\" method=\"post\" id=\"form_connexion\" class=\"form-connect-jv\" autocomplete=\"off\">.*?</form>)", Pattern.DOTALL);
    private static final Pattern inputFormPattern = Pattern.compile("<input (type|name|value)=\"([^\"]*)\" (type|name|value)=\"([^\"]*)\" (type|name|value)=\"([^\"]*)\"/>");
    private static final Pattern dateMessagePattern = Pattern.compile("<div class=\"bloc-date-msg\">([^<]*<span class=\"JvCare [^ ]* lien-jv\" target=\"_blank\">)?[^a-zA-Z0-9]*([^ ]* [^ ]* [^ ]* [^ ]* ([0-9:]*))");
    private static final Pattern messageIdPattern = Pattern.compile("<div class=\"bloc-message-forum[^\"]*\" data-id=\"([^\"]*)\">");
    private static final Pattern unicodeInTextPattern = Pattern.compile("\\\\u([a-zA-Z0-9]{4})");
    private static final Pattern alertPattern = Pattern.compile("<div class=\"alert-row\">([^<]*)</div>");
    private static final Pattern errorBlocPattern = Pattern.compile("<div class=\"bloc-erreur\">([^<]*)</div>");
    private static final Pattern errorInJsonModePattern = Pattern.compile("\"(erreur|error)(s)?\":(\\[)?\"([^\"]*)\"");
    private static final Pattern subIdInJsonPattern = Pattern.compile("\"id-abonnement\":([0-9]*)");
    private static final Pattern codeBlockPattern = Pattern.compile("<pre class=\"pre-jv\"><code class=\"code-jv\">([^<]*)</code></pre>");
    private static final Pattern codeLinePattern = Pattern.compile("<code class=\"code-jv\">(.*?)</code>", Pattern.DOTALL);
    private static final Pattern spoilLinePattern = Pattern.compile("<span class=\"bloc-spoil-jv en-ligne\">.*?<span class=\"contenu-spoil\">(.*?)</span></span>", Pattern.DOTALL);
    private static final Pattern spoilBlockPattern = Pattern.compile("<div class=\"bloc-spoil-jv\">.*?<div class=\"contenu-spoil\">(.*?)</div></div>", Pattern.DOTALL);
    private static final Pattern spoilOverlyPattern = Pattern.compile("(<(span|div) class=\"bloc-spoil-jv[^\"]*\">.*?<(span|div) class=\"contenu-spoil\">|</span></span>|</div></div>)", Pattern.DOTALL);
    private static final Pattern pageTopicLinkNumberPattern = Pattern.compile("^(https?://www\\.jeuxvideo\\.com/forums/(?:1|42)-([0-9]*)-([0-9]*)-)([0-9]*)(-[0-9]*-[0-9]*-[0-9]*-[^./]*\\.htm)[#?]?");
    private static final Pattern pageForumLinkNumberPattern = Pattern.compile("^(https?://www\\.jeuxvideo\\.com/forums/0-([0-9]*)-[0-9]*-[0-9]*-[0-9]*-)([0-9]*)(-[0-9]*-([^./]*)\\.htm)[#?]?");
    private static final Pattern pageSearchTopicLinkNumberPattern = Pattern.compile("^(https?://www\\.jeuxvideo\\.com/recherche/forums/(0-[0-9]*-[0-9]*-[0-9]*-[0-9]*-))([0-9]*)(-[0-9]*-[^./]*\\.htm)([#?]?.*)");
    private static final Pattern messageAnchorInTopicLinkPattern = Pattern.compile("#post_([0-9]*)");
    private static final Pattern jvCarePattern = Pattern.compile("<span class=\"JvCare [^\"]*\">([^<]*)</span>");
    private static final Pattern lastEditMessagePattern = Pattern.compile("<div class=\"info-edition-msg\">[^M]*(Message édité le ([^ ]* [^ ]* [^ ]* [^ ]* [0-9:]*) par.*?)</div>", Pattern.DOTALL);
    private static final Pattern messageEditInfoPattern = Pattern.compile("<textarea((.*?)(?=id=\"text_commentaire\")|(.*?)(?=>))id=\"text_commentaire\"[^>]*>(.*?)</textarea>", Pattern.DOTALL);
    private static final Pattern allArianeStringPattern = Pattern.compile("<div class=\"fil-ariane-crumb\">.*?</h1>", Pattern.DOTALL);
    private static final Pattern forumNameInArianeStringPattern = Pattern.compile("<span><a href=\"(/forums/0-[^\"]*)\">([^<]*)</a></span>");
    private static final Pattern topicNameInArianeStringPattern = Pattern.compile("<span><a href=\"/forums/(42|1)-[^\"]*\">([^<]*)</a></span>");
    private static final Pattern highlightInArianeStringPattern = Pattern.compile("<h1 class=\"highlight\">([^<]*)</h1>");
    private static final Pattern topicNameAndLinkPattern = Pattern.compile("<a class=\"lien-jv topic-title[^\"]*\" href=\"([^\"]*\" title=\"[^\"]*)\"[^>]*>");
    private static final Pattern topicNameAndLinkInMessageSearchPattern = Pattern.compile("<a href=\"([^\"]*)\" class=\"topic-title icon-down-right-arrow\"[^>]*>(.*?)</a>", Pattern.DOTALL);
    private static final Pattern topicNumberMessagesPattern = Pattern.compile("<span class=\"topic-count\">[^0-9]*([0-9]*)");
    private static final Pattern topicNumberMessagesAdmPattern = Pattern.compile("<span class=\"topic-count-adm\">[^0-9]*([0-9]*)");
    private static final Pattern topicAuthorPattern = Pattern.compile("<span class=\".*?text-([^ ]*) topic-author[^>]*>[^A-Za-z0-9\\[\\]_-]*([^<\\n\\r ]*)");
    private static final Pattern topicAuthorInMessageSearchPattern = Pattern.compile("<span class=\".*?text-auteur text-([a-zA-Z]*)[^>]*>[^A-Za-z0-9\\[\\]_-]*([^<\\n\\r ]*)");
    private static final Pattern topicDatePattern = Pattern.compile("<span class=\"topic-date\">[^<]*<span[^>]*>[^0-9/:]*([0-9/:]*)");
    private static final Pattern topicTypePattern = Pattern.compile("<img src=\"/img/forums/topic-(.*?)\\.png\" alt=\"[^\"]*\" title=\"[^\"]*\" class=\"topic-img\"");
    private static final Pattern forumFavsBlocPattern = Pattern.compile("<h2>Mes forums favoris</h2>.*?<ul class=\"display-list-simple\">(.*?)</ul>", Pattern.DOTALL);
    private static final Pattern topicFavsBlocPattern = Pattern.compile("<h2>Mes sujets favoris</h2>.*?<ul class=\"display-list-simple\">(.*?)</ul>", Pattern.DOTALL);
    private static final Pattern favPattern = Pattern.compile("<li><a href=\"([^\"]*)\">([^<]*)</a></li>");
    private static final Pattern forumInSearchPagePattern = Pattern.compile("<a class=\"list-search-forum-name\" href=\"([^\"]*)\"[^>]*>(.*?)</a>");
    private static final Pattern subforumListInForumPagePattern = Pattern.compile("<ul class=\"liste-sous-forums\">(.*?)</ul>", Pattern.DOTALL);
    private static final Pattern noMissTopicsListInForumPagePattern = Pattern.compile("<ul class=\"liste-sujets-nomiss\">(.*?)</ul>", Pattern.DOTALL);
    private static final Pattern subforumInListPattern = Pattern.compile("<li class=\"line-ellipsis\">[^<]*<a href=\"([^\"]*)\" class=\"lien-jv\">([^<]*)</a>[^<]*</li>");
    private static final Pattern noMissTopicInListPattern = Pattern.compile("<a href=\"//www.jeuxvideo.com([^\"]*)\" class=\"lien-jv\">([^<]*)</a>");
    private static final Pattern isInFavPattern = Pattern.compile("<span class=\"picto-favoris([^\"]*)\"");
    private static final Pattern topicIdInTopicPagePattern = Pattern.compile("<div (.*?)data-topic-id=\"([^\"]*)\">");
    private static final Pattern isInSubInTopicPagePattern = Pattern.compile("<span class=\"icon-bell-([^\"]*)\" title=\"[^\"]*\" data-action=\"[^\"]*\"([^>]*)>");
    private static final Pattern subIdInSubButtonPattern = Pattern.compile("data-id-abonnement=\"([^\"]*)\"");
    private static final Pattern lockReasonPattern = Pattern.compile("<div class=\"message-lock-topic\">[^<]*<span>([^<]*)</span>");
    private static final Pattern surveyTitlePattern = Pattern.compile("<div class=\"intitule-sondage\">([^<]*)</div>");
    private static final Pattern surveyResultPattern = Pattern.compile("<div class=\"pied-result\">([^<]*)</div>");
    private static final Pattern surveyReplyPattern = Pattern.compile("<td class=\"result-pourcent\">[^<]*<div class=\"pourcent\">([^<]*)</div>.*?<td class=\"reponse\">([^<]*)</td>", Pattern.DOTALL);
    private static final Pattern surveyReplyWithInfosPattern = Pattern.compile("<a href=\"#\" class=\"btn-sondage-reponse\" data-id-sondage=\"([^\"]*)\" data-id-reponse=\"([^\"]*)\">(.*?)</a>", Pattern.DOTALL);
    /* Parce que c'est faux, l'échappement n'est pas redondant s'il est supprimé la regexp est invalide. */
    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern realSurveyContentPattern = Pattern.compile("\"html\":\"(.*?)\"\\}");
    private static final Pattern numberOfMpJVCPattern = Pattern.compile("<div class=\".*?account-mp.*?\">[^<]*<span[^c]*class=\"jv-account-number-mp[^\"]*\".*?data-val=\"([^\"]*)\"", Pattern.DOTALL);
    private static final Pattern numberOfNotifJVCPattern = Pattern.compile("<div class=\".*?account-notif.*?\">[^<]*<span[^c]*class=\"jv-account-number-notif[^\"]*\".*?data-val=\"([^\"]*)\"", Pattern.DOTALL);
    private static final Pattern numberOfConnectedPattern = Pattern.compile("<span class=\"nb-connect-fofo\">([^<]*)</span>");
    private static final Pattern listOfModeratorsPattern = Pattern.compile("<span class=\"liste-modo-fofo\">(.*?)</span>", Pattern.DOTALL);
    private static final Pattern overlyJVCQuotePattern = Pattern.compile("(<(/)?blockquote>)");
    private static final Pattern overlyBetterQuotePattern = Pattern.compile("<(/)?blockquote>");
    private static final Pattern shortJvcLinkPattern = Pattern.compile("<span +class=\"JvCare [^\"]*\" *>(http(s)?://[^<]*)</span>");
    private static final Pattern longJvcLinkPattern = Pattern.compile("<a +title=\"[^\"]*\" +href=\"([^\"]*)\"[^>]*>[^<]*<i></i><span>[^<]*</span>[^<]*</a>");
    private static final Pattern shortLinkPattern = Pattern.compile("<span +class=\"JvCare [^\"]*\"[^>]*?target=\"_blank\"[^>]*>([^<]*)</span>");
    private static final Pattern longLinkPattern = Pattern.compile("<span +class=\"JvCare [^\"]*\"[^i]*itle=\"([^\"]*)\"[^>]*>[^<]*<i></i><span>[^<]*</span>[^<]*</span>");
    private static final Pattern textLinkPattern = Pattern.compile("http(s)?://[a-zA-Z0-9/%._~!$&'()*+,;=:@?#-]*(?![^<>]*(>|</a>))", Pattern.CASE_INSENSITIVE);
    private static final Pattern smileyPattern = Pattern.compile("<img src=\"http(s)?://image\\.jeuxvideo\\.com/smileys_img/([^\"]*)\" alt=\"[^\"]*\" data-code=\"([^\"]*)\" title=\"[^\"]*\" [^>]*>");
    private static final Pattern embedVideoPattern = Pattern.compile("<div class=\"player-contenu\"><div class=\"[^\"]*\"><iframe.*?src=\"([^\"]*)\"[^>]*></iframe></div></div>");
    private static final Pattern jvcVideoPattern = Pattern.compile("<div class=\"player-contenu\">.*?<div class=\"player-jv\" id=\"player-jv-([^-]*)-.*?</div>[^<]*</div>[^<]*</div>[^<]*</div>", Pattern.DOTALL);
    private static final Pattern surroundedBlockquotePattern = Pattern.compile("(<br /> *)*(<(/)?blockquote>)( *<br />)*");
    private static final Pattern noelshackImagePattern = Pattern.compile("<span class=\"JvCare[^>]*><img class=\"img-shack\".*?src=\"http(s)?://([^\"]*)\" alt=\"([^\"]*)\"[^>]*></span>");
    private static final Pattern emptySearchPattern = Pattern.compile("<span style=\"[^\"]*\">[ \\n\\r]*Aucune réponse pour votre recherche ![ \\n\\r]*</span>");
    private static final Pattern userCanPostAsModoPattern = Pattern.compile("<select class=\"select-user-post\" id=\"form_alias_rang\" name=\"form_alias_rang\">((.*?)(?=<option value=\"2\")|(.*?)(?=</select>))<option value=\"2\"", Pattern.DOTALL);
    private static final Pattern userCanLockOrUnlockTopicPattern = Pattern.compile("<span class=\"btn btn-forum-modo btn-lock-topic\" data-type=\"(un)?lock\">");
    private static final Pattern userCanPinOrUnpinTopicPattern = Pattern.compile("<span class=\"btn btn-forum-modo btn-epingle-topic\" data-type=\"(des)?epingle\">");
    private static final Pattern uglyImagesNamePattern = Pattern.compile("issou|risi|rizi|jesus|picsart|chancla|larry|sermion");
    private static final Pattern adPattern = Pattern.compile("<ins[^>]*></ins>");
    private static final Pattern htmlTagPattern = Pattern.compile("<.+?>");
    private static final Pattern multipleSpacesPattern = Pattern.compile(" +");

    private JVCParser() {
        //rien
    }

    public static String getForumForTopicLink(String topicLink) {
        Matcher pageTopicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(topicLink);

        if (pageTopicLinkNumberMatcher.find()) {
            return "https://www.jeuxvideo.com/forums/0-" + pageTopicLinkNumberMatcher.group(2) + "-0-1-0-1-0-respawn-irc.htm";
        } else {
            return "";
        }
    }

    public static String formatThisUrlToClassicJvcUrl(String urlToChange) {
        if (urlToChange.startsWith("http://")) {
            urlToChange = "https://" + urlToChange.substring(("http://").length());
        } else if (!urlToChange.startsWith("https://")) {
            urlToChange = "https://" + urlToChange;
        }

        if (urlToChange.startsWith("https://m.jeuxvideo.com/")) {
            urlToChange = "https://www.jeuxvideo.com/" + urlToChange.substring(("https://m.jeuxvideo.com/").length());
        } else if (urlToChange.startsWith("https://jeuxvideo.com/")) {
            urlToChange = "https://www.jeuxvideo.com/" + urlToChange.substring(("https://jeuxvideo.com/").length());
        }

        return urlToChange;
    }

    public static String noelshackToDirectLink(String baseLink) {
        if (baseLink.contains("noelshack.com/")) {
            baseLink = baseLink.substring(baseLink.indexOf("noelshack.com/") + 14);
        } else {
            return baseLink;
        }

        if (baseLink.startsWith("fichiers/") || baseLink.startsWith("fichiers-xs/") || baseLink.startsWith("minis/")) {
            baseLink = baseLink.substring(baseLink.indexOf("/") + 1);
        } else {
            baseLink = baseLink.replaceFirst("-", "/").replaceFirst("-", "/");
        }

        //moyen dégueulasse pour checker si le lien utilise le nouveau format (deux nombres entre l'année et le timestamp au lieu d'un)
        //TODO: à changer
        if (baseLink.contains("/")) {
            String checkForNewStringType = baseLink.substring(baseLink.lastIndexOf("/") + 1);

            if (checkForNewStringType.contains("-")) {
                checkForNewStringType = checkForNewStringType.substring(0, checkForNewStringType.indexOf("-"));

                if (checkForNewStringType.matches("[0-9]{1,8}")) {
                    baseLink = baseLink.replaceFirst("-", "/");
                }
            }
        }

        return "http://image.noelshack.com/fichiers/" + baseLink;
    }

    public static boolean checkIfItsNoelshackLink(String linkToCheck) {
        int endOfLink = linkToCheck.indexOf("?");
        if (endOfLink != -1) {
            linkToCheck = linkToCheck.substring(0, endOfLink);
        }

        endOfLink = linkToCheck.indexOf("#");
        if (endOfLink != -1) {
            linkToCheck = linkToCheck.substring(0, endOfLink);
        }

        linkToCheck = linkToCheck.toLowerCase();
        if (!linkToCheck.endsWith(".php") && !linkToCheck.endsWith(".com/") && !linkToCheck.endsWith(".json")) {
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

    public static boolean checkIfForumAreSame(String firstForumLink, String secondForumLink) {
        Matcher firstPageForumLinkNumberMatcher = pageForumLinkNumberPattern.matcher(firstForumLink);
        Matcher secondPageForumLinkNumberMatcher = pageForumLinkNumberPattern.matcher(secondForumLink);

        //noinspection SimplifiableIfStatement
        if (firstPageForumLinkNumberMatcher.find() && secondPageForumLinkNumberMatcher.find()) {
            return firstPageForumLinkNumberMatcher.group(2).equals(secondPageForumLinkNumberMatcher.group(2));
        } else {
            return false;
        }
    }

    public static boolean checkIfItsForumFormatedLink(String linkToCheck) {
        return linkToCheck.startsWith("https://www.jeuxvideo.com/forums/0-");
    }

    public static boolean checkIfItsTopicFormatedLink(String linkToCheck) {
        return linkToCheck.startsWith("https://www.jeuxvideo.com/forums/1-") ||
                linkToCheck.startsWith("https://www.jeuxvideo.com/forums/42-");
    }

    public static boolean checkIfItsSearchFormatedLink(String linkToCheck) {
        return linkToCheck.startsWith("https://www.jeuxvideo.com/recherche/forums/0-");
    }

    public static boolean checkIfItsMessageFormatedLink(String linkToCheck) {
        if (linkToCheck.startsWith("https://www.jeuxvideo.com/")) {
            String partOfLinkToCheck = linkToCheck.substring(("https://www.jeuxvideo.com/").length());

            if (partOfLinkToCheck.contains("/")) {
                partOfLinkToCheck = partOfLinkToCheck.substring(partOfLinkToCheck.indexOf('/'));

                return partOfLinkToCheck.startsWith("/forums/message/");
            }
        }

        return false;
    }

    /* Retourne vrai si c'est un lien ouvrable via RespawnIRC. */
    public static boolean checkIfItsOpennableFormatedLink(String linkToCheck) {
        return checkIfItsForumFormatedLink(linkToCheck) || checkIfItsTopicFormatedLink(linkToCheck) ||
                 checkIfItsSearchFormatedLink(linkToCheck) || checkIfItsMessageFormatedLink(linkToCheck);
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

    public static String getForumNameOfThisForum(String forumLink) {
        Matcher forumLinkNumberMatcher = pageForumLinkNumberPattern.matcher(forumLink);

        if (forumLinkNumberMatcher.find()) {
            return forumLinkNumberMatcher.group(5);
        } else {
            return "";
        }
    }

    public static String getForumIdOfThisForum(String forumLink) {
        Matcher forumLinkNumberMatcher = pageForumLinkNumberPattern.matcher(forumLink);

        if (forumLinkNumberMatcher.find()) {
            return forumLinkNumberMatcher.group(2);
        } else {
            return "";
        }
    }

    public static String getForumIdOfThisTopic(String topicLink) {
        Matcher topicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(topicLink);

        if (topicLinkNumberMatcher.find()) {
            return topicLinkNumberMatcher.group(2);
        } else {
            return "";
        }
    }

    public static String getTopicIdOfThisTopic(String topicLink) {
        Matcher topicLinkNumberMatcher = pageTopicLinkNumberPattern.matcher(topicLink);

        if (topicLinkNumberMatcher.find()) {
            return topicLinkNumberMatcher.group(3);
        } else {
            return "";
        }
    }

    public static String getTopicIdInThisTopicPage(String topicContent) {
        Matcher topicIdInTopicPageMatcher = topicIdInTopicPagePattern.matcher(topicContent);

        if (topicIdInTopicPageMatcher.find()) {
            return topicIdInTopicPageMatcher.group(2);
        } else {
            return "";
        }
    }

    /* Retourne l'ID de l'abonnement si l'user est abonné, retourne un string empty s'il ne l'est pas,
     * retourne null si indéterminé. */
    public static String getSubIdInThisTopicPage(String topicContent) {
        Matcher isInSubInTopicPageMatcher = isInSubInTopicPagePattern.matcher(topicContent);

        if (isInSubInTopicPageMatcher.find()) {
            /* if (isInSub) [ */
            if (isInSubInTopicPageMatcher.group(1).equals("on")) {
                Matcher subIdInSubButtonMatcher = subIdInSubButtonPattern.matcher(isInSubInTopicPageMatcher.group());

                if (subIdInSubButtonMatcher.find()) {
                    return subIdInSubButtonMatcher.group(1);
                }
            } else {
                return "";
            }
        }

        return null;
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

    public static String getPageNumberForThisSearchTopicLink(String searchTopicLink) {
        Matcher pageSearchTopicLinkNumberMatcher = pageSearchTopicLinkNumberPattern.matcher(searchTopicLink);

        if (pageSearchTopicLinkNumberMatcher.find()) {
            return pageSearchTopicLinkNumberMatcher.group(3);
        }
        else {
            return "";
        }
    }

    public static String setPageNumberForThisSearchTopicLink(String searchTopicLink, int newPageNumber) {
        Matcher pageSearchTopicLinkNumberMatcher = pageSearchTopicLinkNumberPattern.matcher(searchTopicLink);

        if (pageSearchTopicLinkNumberMatcher.find()) {
            return pageSearchTopicLinkNumberMatcher.group(1) + String.valueOf(newPageNumber) + pageSearchTopicLinkNumberMatcher.group(4) + pageSearchTopicLinkNumberMatcher.group(5);
        }
        else {
            return "";
        }
    }

    public static String getSuffixForSearchTopicLink(String searchTopicLink) {
        Matcher pageSearchTopicLinkNumberMatcher = pageSearchTopicLinkNumberPattern.matcher(searchTopicLink);

        if (pageSearchTopicLinkNumberMatcher.find()) {
            return pageSearchTopicLinkNumberMatcher.group(2) + pageSearchTopicLinkNumberMatcher.group(3) + pageSearchTopicLinkNumberMatcher.group(4);
        }
        else {
            return "";
        }
    }

    public static String getTextToSearchForSearchTopicLink(String searchTopicLink) {
        Matcher pageSearchTopicLinkNumberMatcher = pageSearchTopicLinkNumberPattern.matcher(searchTopicLink);

        if (pageSearchTopicLinkNumberMatcher.find()) {
            String[] listOfFields = pageSearchTopicLinkNumberMatcher.group(5).substring(1).split("&");

            for (String thisField : listOfFields) {
                if (thisField.startsWith("search_in_forum=")) {
                    return Utils.decodeUrlStringToString(thisField.substring(("search_in_forum=").length()));
                }
            }
        }
        return "";
    }

    public static String getTypeOfSearchForSearchTopicLink(String searchTopicLink) {
        Matcher pageSearchTopicLinkNumberMatcher = pageSearchTopicLinkNumberPattern.matcher(searchTopicLink);

        if (pageSearchTopicLinkNumberMatcher.find()) {
            String[] listOfFields = pageSearchTopicLinkNumberMatcher.group(5).substring(1).split("&");

            for (String thisField : listOfFields) {
                if (thisField.startsWith("type_search_in_forum=")) {
                    return Utils.decodeUrlStringToString(thisField.substring(("type_search_in_forum=").length()));
                }
            }
        }
        return "";
    }

    public static String getMessageAnchorInTopicIfAny(String topicLink) {
        Matcher messageAnchorInTopicLinkMatcher = messageAnchorInTopicLinkPattern.matcher(topicLink);

        if (messageAnchorInTopicLinkMatcher.find()) {
            return messageAnchorInTopicLinkMatcher.group(1);
        } else {
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

    public static ArrayList<SurveyReplyInfos> getListOfSurveyReplyWithInfos(String pageSource) {
        ArrayList<SurveyReplyInfos> listOfReplys = new ArrayList<>();
        Matcher replyWithInfosMatcher = surveyReplyWithInfosPattern.matcher(pageSource);
        int lastOffset = 0;

        while (replyWithInfosMatcher.find(lastOffset)) {
            String tmpInfosForReply = "id_sondage=" + replyWithInfosMatcher.group(1) + "&id_sondage_reponse=" + replyWithInfosMatcher.group(2);
            String tmpTitleOfReply = specialCharToNormalChar(replyWithInfosMatcher.group(3).replace("\n", "").replace("\r", "").trim());

            listOfReplys.add(new SurveyReplyInfos(tmpInfosForReply, tmpTitleOfReply));
            lastOffset = replyWithInfosMatcher.end();
        }

        return listOfReplys;
    }

    public static ArrayList<NameAndLink> getListOfForumsInSearchPage(String pageSource) {
        ArrayList<NameAndLink> listOfForums = new ArrayList<>();
        Matcher forumInSearchPageMatcher = forumInSearchPagePattern.matcher(pageSource);
        int lastOffset = 0;

        while (forumInSearchPageMatcher.find(lastOffset)) {
            NameAndLink newNameAndLink = new NameAndLink();

            newNameAndLink.name = forumInSearchPageMatcher.group(2).replace("<em>", "").replace("</em>", "");
            if (!forumInSearchPageMatcher.group(1).isEmpty()) {
                newNameAndLink.link = "https://www.jeuxvideo.com" + forumInSearchPageMatcher.group(1);
            }

            listOfForums.add(newNameAndLink);
            lastOffset = forumInSearchPageMatcher.end();
        }

        return listOfForums;
    }

    public static ArrayList<NameAndLink> getListOfSubforumsInForumPage(String pageSource) {
        ArrayList<NameAndLink> listOfSubforums = new ArrayList<>();
        Matcher subforumListInForumPageMatcher = subforumListInForumPagePattern.matcher(pageSource);

        if (subforumListInForumPageMatcher.find()) {
            Matcher subforumInListMatcher = subforumInListPattern.matcher(subforumListInForumPageMatcher.group(1));
            int lastOffset = 0;

            while (subforumInListMatcher.find(lastOffset)) {
                NameAndLink newNameAndLink = new NameAndLink();

                newNameAndLink.name = subforumInListMatcher.group(2).trim();
                if (!subforumInListMatcher.group(1).isEmpty()) {
                    newNameAndLink.link = "https://www.jeuxvideo.com" + subforumInListMatcher.group(1);
                }

                listOfSubforums.add(newNameAndLink);
                lastOffset = subforumInListMatcher.end();
            }
        }

        return listOfSubforums;
    }

    public static ArrayList<NameAndLink> getListOfNoMissTopicsInForumPage(String pageSource) {
        ArrayList<NameAndLink> listOfNoMissTopics = new ArrayList<>();
        Matcher noMissTopicsListInForumPageMatcher = noMissTopicsListInForumPagePattern.matcher(pageSource);

        if (noMissTopicsListInForumPageMatcher.find()) {
            Matcher noMissTopicInListMatcher = noMissTopicInListPattern.matcher(noMissTopicsListInForumPageMatcher.group(1));
            int lastOffset = 0;

            while (noMissTopicInListMatcher.find(lastOffset)) {
                NameAndLink newNameAndLink = new NameAndLink();

                newNameAndLink.name = noMissTopicInListMatcher.group(2).trim();
                if (!noMissTopicInListMatcher.group(1).isEmpty()) {
                    newNameAndLink.link = "https://www.jeuxvideo.com" + noMissTopicInListMatcher.group(1);
                }

                listOfNoMissTopics.add(newNameAndLink);
                lastOffset = noMissTopicInListMatcher.end();
            }
        }

        return listOfNoMissTopics;
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
            String tmpLink = favMatcher.group(1);
            newFav.name = specialCharToNormalChar(favMatcher.group(2));

            if (tmpLink.startsWith("/forums/")) {
                newFav.link = "https://www.jeuxvideo.com" + tmpLink;
            } else if (!tmpLink.startsWith("https:")) {
                newFav.link = "https:" + tmpLink;
            } else {
                newFav.link = tmpLink;
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
                forumName = forumNameMatcher.group(2);
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

    public static NameAndLink getMainForumNameAndLinkInForumPage(String pageSource) {
        NameAndLink mainForum = new NameAndLink();
        Matcher allArianeStringMatcher = allArianeStringPattern.matcher(pageSource);

        if (allArianeStringMatcher.find()) {
            String allArianeString = allArianeStringMatcher.group();
            Matcher forumNameMatcher = forumNameInArianeStringPattern.matcher(allArianeString);
            int lastOffset = 0;

            while (forumNameMatcher.find(lastOffset)) {
                mainForum.name = forumNameMatcher.group(2);
                mainForum.link = forumNameMatcher.group(1);
                lastOffset = forumNameMatcher.end();
            }

            if (mainForum.name.startsWith("Forum principal")) {
                mainForum.name =  mainForum.name.substring(("Forum principal").length());
            }
            mainForum.name = specialCharToNormalChar(mainForum.name.trim());

            if (!mainForum.link.isEmpty()) {
                mainForum.link = "https://www.jeuxvideo.com" + mainForum.link;
            }
        }

        if (mainForum.name.isEmpty()) {
            return null;
        } else {
            return mainForum;
        }
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
                currentNames.forum = forumNameMatcher.group(2);
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

    public static String getNumberOfMpFromPage(String pageSource) {
        Matcher numberOfMpJVCMatcher = numberOfMpJVCPattern.matcher(pageSource);

        if (numberOfMpJVCMatcher.find()) {
            return numberOfMpJVCMatcher.group(1);
        } else {
            return null;
        }
    }

    public static String getNumberOfNotifFromPage(String pageSource) {
        Matcher numberOfNotifJVCMatcher = numberOfNotifJVCPattern.matcher(pageSource);

        if (numberOfNotifJVCMatcher.find()) {
            return numberOfNotifJVCMatcher.group(1);
        } else {
            return null;
        }
    }

    public static String getNumberOfConnectFromPage(String pageSource) {
        Matcher numberOfConnectedMatcher = numberOfConnectedPattern.matcher(pageSource);

        if (numberOfConnectedMatcher.find()) {
            return numberOfConnectedMatcher.group(1);
        } else {
            return "";
        }
    }

    public static String getListOfModeratorsFromPage(String pageSource) {
        Matcher listOfModeratorsMatcher = listOfModeratorsPattern.matcher(pageSource);

        if (listOfModeratorsMatcher.find()) {
            return listOfModeratorsMatcher.group(1).replace("<!--", "").replace("-->", "").replace(" ", "").replace("\n", "").replace(",", ", ");
        } else {
            return "";
        }
    }

    public static boolean getSearchIsEmptyInPage(String pageSource) {
        return emptySearchPattern.matcher(pageSource).find();
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

    public static String getSurveyHtmlTitleFromPage(String pageSource) {
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
        Matcher errorMatcher = alertPattern.matcher(pageSource);

        if (errorMatcher.find()) {
            return "Erreur : " + specialCharToNormalChar(errorMatcher.group(1)).trim();
        } else {
            return "Erreur : le message n'a pas été envoyé.";
        }
    }

    public static String getErrorMessageWhenModoConnect(String pageSource) {
        Matcher errorMatcher = errorBlocPattern.matcher(pageSource);

        if (errorMatcher.find()) {
            return "Erreur : " + specialCharToNormalChar(errorMatcher.group(1)).trim();
        } else {
            Matcher alertMatcher = alertPattern.matcher(pageSource);

            if (alertMatcher.find()) {
                if (!alertMatcher.group(1).trim().isEmpty()) {
                    return "";
                }
            }
        }

        return "Erreur : la connexion à échouée.";
    }

    public static String getErrorMessageInJsonMode(String pageSource) {
        Matcher errorMatcher = errorInJsonModePattern.matcher(pageSource);

        if (errorMatcher.find()) {
            String errorMessage = errorMatcher.group(4);

            if (!errorMessage.isEmpty()) {
                return "Erreur : " + specialCharToNormalChar(parsingAjaxMessages(errorMessage)).trim();
            }
        }

        return null;
    }

    public static String getSubIdFromJson(String pageSource) {
        Matcher subIdInJsonMatcher = subIdInJsonPattern.matcher(pageSource);

        if (subIdInJsonMatcher.find()) {
            return subIdInJsonMatcher.group(1);
        } else {
            return "";
        }
    }

    public static String getMessageEdit(String pageSource) {
        Matcher messageEditInfoMatcher = messageEditInfoPattern.matcher(pageSource);

        if (messageEditInfoMatcher.find()) {
            return specialCharToNormalChar(messageEditInfoMatcher.group(4));
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
                .replaceAll("(?<!\\\\)\\\\t", "\t")
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
        Matcher ajaxSubHashMatcher = ajaxSubHashPattern.matcher(pageSource);

        if (ajaxListTimestampMatcher.find() && ajaxListHashMatcher.find()) {
            newAjaxInfos.list = "ajax_timestamp=" + ajaxListTimestampMatcher.group(3) + "&ajax_hash=" + ajaxListHashMatcher.group(3);
        }

        if (ajaxModTimestampMatcher.find() && ajaxModHashMatcher.find()) {
            newAjaxInfos.mod = "ajax_timestamp=" + ajaxModTimestampMatcher.group(1) + "&ajax_hash=" + ajaxModHashMatcher.group(1);
        }

        if (ajaxPrefTimestampMatcher.find() && ajaxPrefHashMatcher.find()) {
            newAjaxInfos.pref = "ajax_timestamp=" + ajaxPrefTimestampMatcher.group(1) + "&ajax_hash=" + ajaxPrefHashMatcher.group(1);
        }

        if (ajaxSubHashMatcher.find()) {
            newAjaxInfos.sub = "ajax_hash=" + ajaxSubHashMatcher.group(1);
        }

        return newAjaxInfos;
    }

    public static boolean getUserCanPostAsModo(String pageSource) {
        return userCanPostAsModoPattern.matcher(pageSource).find();
    }

    public static boolean getUserCanLockOrUnlockTopic(String pageSource) {
        return userCanLockOrUnlockTopicPattern.matcher(pageSource).find();
    }

    public static boolean getUserCanPinOrUnpinTopic(String pageSource) {
        return userCanPinOrUnpinTopicPattern.matcher(pageSource).find();
    }

    public static boolean getTopicIsPinned(String pageSource) {
        Matcher topicIsPinnedMatcher = userCanPinOrUnpinTopicPattern.matcher(pageSource);

        //noinspection SimplifiableIfStatement
        if (topicIsPinnedMatcher.find()) {
            return !Utils.stringIsEmptyOrNull(topicIsPinnedMatcher.group(1));
        } else {
            return false;
        }
    }

    public static String getListOfInputInAStringInTopicFormForThisPage(String pageSource) {
        Matcher topicFormMatcher = topicFormPattern.matcher(pageSource);

        if (topicFormMatcher.find()) {
            return getListOfInputInAStringInThisForm(topicFormMatcher.group(1));
        }

        return "";
    }

    public static String getListOfInputInAStringInModoConnectFormForThisPage(String pageSource) {
        Matcher modoConnectFormMatcher = modoConnectFormPattern.matcher(pageSource);

        if (modoConnectFormMatcher.find()) {
            return getListOfInputInAStringInThisForm(modoConnectFormMatcher.group(1));
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
                lastPage = "https://www.jeuxvideo.com" + pageLinkMatcher.group(1);
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
                return "https://www.jeuxvideo.com" + pageLinkMatcher.group(1);
            }
        }

        return "";
    }

    public static String createMessageInfoLineFromInfos(MessageInfos thisMessageInfo, Settings settings) {
        StringBuilder newFirstLine = new StringBuilder(settings.firstLineFormat);

        ToolForParsing.replaceStringByAnother(newFirstLine, "<%DATE_TIME%>", thisMessageInfo.dateTime);
        ToolForParsing.replaceStringByAnother(newFirstLine, "<%DATE_FULL%>", thisMessageInfo.wholeDate);
        ToolForParsing.replaceStringByAnother(newFirstLine, "<%PSEUDO_PSEUDO%>", (thisMessageInfo.pseudoIsBlacklisted ? "Auteur blacklisté" : thisMessageInfo.pseudo));

        if (thisMessageInfo.isAnEdit) {
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%DATE_COLOR_START%>", "<font color=\"#008000\">");
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%DATE_COLOR_END%>", "</font>");
        } else {
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%DATE_COLOR_START%>", "");
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%DATE_COLOR_END%>", "");
        }

        if ((settings.typeOfPseudoToColorInInfoLine.type == PrefsManager.PseudoColorType.CURRENT_ONLY && thisMessageInfo.pseudo.toLowerCase().equals(settings.pseudoOfUser.toLowerCase()) ||
                (settings.typeOfPseudoToColorInInfoLine.type == PrefsManager.PseudoColorType.ALL_ACCOUNTS && AccountManager.getIndexOfThisAccount(thisMessageInfo.pseudo) >= 0))) {
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%PSEUDO_COLOR_START%>", "<font color=\"" + settings.colorPseudoUser + "\">");
        } else if (thisMessageInfo.pseudoType.equals("modo")){
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%PSEUDO_COLOR_START%>", "<font color=\"" + settings.colorPseudoModo + "\">");
        } else if (thisMessageInfo.pseudoType.equals("admin") || thisMessageInfo.pseudoType.equals("staff")){
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%PSEUDO_COLOR_START%>", "<font color=\"" + settings.colorPseudoAdmin + "\">");
        } else {
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%PSEUDO_COLOR_START%>", "<font color=\"" + settings.colorPseudoOther + "\">");
        }
        ToolForParsing.replaceStringByAnother(newFirstLine, "<%PSEUDO_COLOR_END%>", "</font>");

        if (settings.applyMarkToPseudoAuthor && thisMessageInfo.pseudo.toLowerCase().equals(settings.pseudoOfAuthor.toLowerCase())) {
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%MARK_FOR_PSEUDO%>", " \ud83d\udd8a\ufe0f");
        } else {
            ToolForParsing.replaceStringByAnother(newFirstLine, "<%MARK_FOR_PSEUDO%>", "");
        }

        return newFirstLine.toString();
    }

    public static String createMessageMessageLineFromInfos(MessageInfos thisMessageInfo, Settings settings) {
        String finalMessage = settings.secondLineFormat;
        SpoilTagsInfos infosOfSpoilTags = new SpoilTagsInfos();
        infosOfSpoilTags.containSpoil = thisMessageInfo.messageContentContainSpoil;
        infosOfSpoilTags.listOfSpoilIdToShow = thisMessageInfo.listOfSpoilIdToShow;
        infosOfSpoilTags.lastIdOfSpoil = -1;

        finalMessage = finalMessage.replace("<%MESSAGE_MESSAGE%>", parseMessageToPrettyMessage(thisMessageInfo.messageNotParsed, settings, infosOfSpoilTags, thisMessageInfo.showOverlyQuote, thisMessageInfo.showUglyImages));
        thisMessageInfo.lastIdOfSpoilInMessage = infosOfSpoilTags.lastIdOfSpoil;
        if (!thisMessageInfo.lastTimeEdit.isEmpty()) {
            finalMessage = finalMessage.replace("<%EDIT_ALL%>", settings.addBeforeEdit + thisMessageInfo.lastTimeEdit.trim() + settings.addAfterEdit);
        } else {
            finalMessage = finalMessage.replace("<%EDIT_ALL%>", "");
        }
        finalMessage = surroundedBlockquotePattern.matcher(finalMessage).replaceAll("$2");

        return "<span>" + finalMessage + "</span>"; //pour corriger un bug de BackgroundSpan qui se ferme jamais si ouvert tout au début
    }

    public static String createSignatureFromInfos(MessageInfos thisMessageInfo, Settings settings) {
        SpoilTagsInfos infosOfSpoilTags = new SpoilTagsInfos();
        infosOfSpoilTags.containSpoil = thisMessageInfo.signatureContainSpoil;
        infosOfSpoilTags.listOfSpoilIdToShow = thisMessageInfo.listOfSpoilIdToShow;
        infosOfSpoilTags.lastIdOfSpoil = thisMessageInfo.lastIdOfSpoilInMessage;
        return "<small>" + parseMessageToPrettyMessage(thisMessageInfo.signatureNotParsed, settings, infosOfSpoilTags, true, true) + "</small>";
    }

    public static String parseMessageToPrettyMessage(String messageInString, Settings settings, SpoilTagsInfos infosOfSpoilTag, boolean showOverlyQuote, boolean showUglyImages) {
        StringBuilder messageInBuilder = new StringBuilder(messageInString);
        MakeShortenedLinkIfPossible makeLinkDependingOnSettingsAndForceMake = new MakeShortenedLinkIfPossible((settings.shortenLongLink ? 50 : 0), true);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, codeBlockPattern, 1, "<p><font face=\"monospace\">", "</font></p>", new MakeCodeTagGreatAgain(true), null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, codeLinePattern, 1, " <font face=\"monospace\">", "</font> ", new MakeCodeTagGreatAgain(false), null);
        ToolForParsing.replaceStringByAnother(messageInBuilder, "\n", "");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "<span&nbsp;&nbsp;", "<span  "); // Fix sale pour que les liens longs fonctionnent dans les balises.

        StickerConverter.convertStickerWithThisRule(messageInBuilder, StickerConverter.ruleForNoLangageSticker);
        if (settings.transformStickerToSmiley) {
            StickerConverter.convertStickerWithThisRule(messageInBuilder, StickerConverter.ruleForStickerToSmiley);
        }
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, stickerPattern, 1, "<img src=\"sticker_", ".png\"/>", new ConvertUrlToStickerId(), new ConvertStringToString("-", "_"));
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, smileyPattern, 2, "<img src=\"smiley_", "\"/>", null, null);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, embedVideoPattern, 1, "<p>", "</p>", makeLinkDependingOnSettingsAndForceMake, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, jvcVideoPattern, 1, "<p>", "</p>", new AddPrefixString("https://www.jeuxvideo.com/videos/iframe/"), makeLinkDependingOnSettingsAndForceMake);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, shortJvcLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, longJvcLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, shortLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, longLinkPattern, 1, "", "", makeLinkDependingOnSettingsAndForceMake, null);

        if (settings.hideUglyImages && !showUglyImages) {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, noelshackImagePattern, 0, "", "", new SuppressIfContainUglyNames(), null);
        }

        if (settings.showNoelshackImages) {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, noelshackImagePattern, 3, "", "", new MakeNoelshackImageLink(settings.enableAlphaInNoelshackMini), null);
        } else {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, noelshackImagePattern, 3, "", "", makeLinkDependingOnSettingsAndForceMake, null);
        }

        if (infosOfSpoilTag.containSpoil) {
            BuildSpoilTag spoilTagBuilder = new BuildSpoilTag(infosOfSpoilTag.listOfSpoilIdToShow, infosOfSpoilTag.lastIdOfSpoil);
            spoilTagBuilder.setItsForSpoilBlock(false);
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, spoilLinePattern, 1, "", "", new RemoveFirstsAndLastsPAndBr(), spoilTagBuilder);
            spoilTagBuilder.setItsForSpoilBlock(true);
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, spoilBlockPattern, 1, "<p>", "</p>",  new RemoveFirstsAndLastsPAndBr(), spoilTagBuilder);
            infosOfSpoilTag.lastIdOfSpoil = spoilTagBuilder.getLastIdUsedForSpoil();
        }

        ToolForParsing.removeDivAndAdaptParagraphInMessage(messageInBuilder);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, surroundedBlockquotePattern, 2, "", "", null, null);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, jvCarePattern, 1, "", "", new MakeShortenedLinkIfPossible((settings.shortenLongLink ? 50 : 0), false), null);

        ToolForParsing.removeFirstAndLastBrInMessage(messageInBuilder);

        if (!showOverlyQuote) {
            ToolForParsing.removeOverlyQuoteInPrettyMessage(messageInBuilder, settings.maxNumberOfOverlyQuotes);
        }

        if (settings.typeOfPseudoToColorInMessage.type == PrefsManager.PseudoColorType.CURRENT_ONLY && !settings.pseudoOfUser.isEmpty()) {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, Pattern.compile("(?i)(?<!\\w)" + settings.pseudoOfUser.replace("[", "\\[").replace("]", "\\]") + "(?!\\w)(?![^<>]*(>|</a>))"), 0, "<font color=\"" + settings.colorPseudoUser + "\">", "</font>", null, null);
        } else if (settings.typeOfPseudoToColorInMessage.type == PrefsManager.PseudoColorType.ALL_ACCOUNTS && !AccountManager.getAllAccountsPseudoRegex().isEmpty()) {
            ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, Pattern.compile("(?i)(?<!\\w)" + AccountManager.getAllAccountsPseudoRegex() + "(?!\\w)(?![^<>]*(>|</a>))"), 0, "<font color=\"" + settings.colorPseudoUser + "\">", "</font>", null, null);
        }

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, textLinkPattern, 0, "", "", new MakeShortenedLinkIfPossible(0, false), null);

        return messageInBuilder.toString();
    }

    public static String parseMessageToSimpleMessage(String messageInString) {
        StringBuilder messageInBuilder = new StringBuilder(messageInString);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, codeBlockPattern, 1, "<p>&lt;code&gt;", "&lt;/code&gt;</p>", new ConvertStringToString("\n", "<br />"), new ConvertStringToString("  ", "&nbsp;&nbsp;"));
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, codeLinePattern, 1, "&lt;code&gt;", "&lt;/code&gt;", new ConvertStringToString("  ", "&nbsp;&nbsp;"), null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, stickerPattern, 1, "[[sticker:p/", "]]", new ConvertUrlToStickerId(), null);
        ToolForParsing.replaceStringByAnother(messageInBuilder, "\n", "");
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, smileyPattern, 3, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, embedVideoPattern, 1, "<p>", "</p>", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, jvcVideoPattern, 1, "<p>https://www.jeuxvideo.com/videos/iframe/", "</p>", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, shortJvcLinkPattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, longJvcLinkPattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, shortLinkPattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, longLinkPattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, noelshackImagePattern, 3, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, spoilLinePattern, 1, "&lt;spoil&gt;", "&lt;/spoil&gt;", new RemoveFirstsAndLastsPAndBr(), null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, spoilBlockPattern, 1, "<p>&lt;spoil&gt;", "&lt;/spoil&gt;</p>",  new RemoveFirstsAndLastsPAndBr(), null);
        ToolForParsing.removeDivAndAdaptParagraphInMessage(messageInBuilder);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, surroundedBlockquotePattern, -1, "<br /><br />", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, jvCarePattern, 1, "", "", null, null);
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, multipleSpacesPattern, -1, " ", "", null, null);
        ToolForParsing.removeFirstAndLastBrInMessage(messageInBuilder);
        ToolForParsing.replaceStringByAnother(messageInBuilder, "<strong>", "&#039;&#039;&#039;");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "</strong>", "&#039;&#039;&#039;");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "<em>", "&#039;&#039;");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "</em>", "&#039;&#039;");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "<u>", "&lt;u&gt;");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "</u>", "&lt;/u&gt;");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "<s>", "&lt;s&gt;");
        ToolForParsing.replaceStringByAnother(messageInBuilder, "</s>", "&lt;/s&gt;");

        ToolForParsing.replaceStringByAnother(messageInBuilder, "<br />", "\n");
        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, htmlTagPattern, -1, "", "", null, null);
        ToolForParsing.replaceStringByAnother(messageInBuilder, "\n", "<br />");
        return messageInBuilder.toString();
    }

    public static String makeBasicMessageParse(String messageToParse, boolean containSpoil) {
        StringBuilder messageInBuilder = new StringBuilder(messageToParse);

        ToolForParsing.parseThisMessageWithThisPattern(messageInBuilder, adPattern, -1, "", "", null, null);
        ToolForParsing.replaceStringByAnother(messageInBuilder, "\r", "");
        ToolForParsing.parseListInMessageIfNeeded(messageInBuilder);
        ToolForParsing.replaceStringByAnother(messageInBuilder, "<blockquote class=\"blockquote-jv\">", "<blockquote>");

        if (containSpoil) {
            ToolForParsing.removeOverlySpoils(messageInBuilder);
        }

        return messageInBuilder.toString();
    }

    public static MessageInfos createMessageInfoFromEntireMessage(String thisEntireMessage) {
        MessageInfos newMessageInfo = new MessageInfos();
        Matcher pseudoIsBlacklistedMatcher = pseudoIsBlacklistedPattern.matcher(thisEntireMessage);
        Matcher messageIsDeletedMatcher = messageIsDeletedPattern.matcher(thisEntireMessage);
        Matcher userCanDeleteOrRestoreMessageMatcher = userCanDeleteOrRestoreMessagePattern.matcher(thisEntireMessage);
        Matcher userCanEditMessageMatcher = userCanEditMessagePattern.matcher(thisEntireMessage);
        Matcher userCanKickOrDekickAuthorMatcher = userCanKickOrDekickAuthorPattern.matcher(thisEntireMessage);
        Matcher pseudoInfosMatcher = pseudoInfosPattern.matcher(thisEntireMessage);
        Matcher idAliasMatcher = idAliasPattern.matcher(thisEntireMessage);
        Matcher messageMatcher = messagePattern.matcher(thisEntireMessage);
        Matcher signatureMatcher = signaturePattern.matcher(thisEntireMessage);
        Matcher avatarMatcher = avatarPattern.matcher(thisEntireMessage);
        Matcher dateMessageMatcher = dateMessagePattern.matcher(thisEntireMessage);
        Matcher lastEditMessageMatcher = lastEditMessagePattern.matcher(thisEntireMessage);
        Matcher messageIdMatcher = messageIdPattern.matcher(thisEntireMessage);

        newMessageInfo.pseudoIsBlacklisted = pseudoIsBlacklistedMatcher.find();
        newMessageInfo.messageIsDeleted = messageIsDeletedMatcher.find();
        newMessageInfo.userCanDeleteOrRestoreMessage = userCanDeleteOrRestoreMessageMatcher.find();
        newMessageInfo.userCanEditMessage = userCanEditMessageMatcher.find();
        newMessageInfo.userCanKickOrDekickAuthor = userCanKickOrDekickAuthorMatcher.find();

        if (newMessageInfo.userCanKickOrDekickAuthor) {
            newMessageInfo.authorIsKicked = userCanKickOrDekickAuthorMatcher.group(1).equals("dekick");
        }

        if (pseudoInfosMatcher.find()) {
            newMessageInfo.pseudo = pseudoInfosMatcher.group(2);
            newMessageInfo.pseudoType = pseudoInfosMatcher.group(1);
        }

        if (idAliasMatcher.find()) {
            newMessageInfo.idAlias = idAliasMatcher.group(1);
        }

        if (lastEditMessageMatcher.find()) {
            newMessageInfo.lastTimeEdit = lastEditMessageMatcher.group(1).replaceAll(htmlTagPattern.pattern(), "");
        }

        if (signatureMatcher.find()) {
            newMessageInfo.signatureNotParsed = signatureMatcher.group(1);
        }

        if (avatarMatcher.find()) {
            newMessageInfo.avatarLink = "https://" + avatarMatcher.group(2);
        }

        if (messageIdMatcher.find()) {
            newMessageInfo.id = Long.parseLong(messageIdMatcher.group(1));
        }

        if (dateMessageMatcher.find()) {
            newMessageInfo.dateTime = dateMessageMatcher.group(3);
            newMessageInfo.wholeDate = dateMessageMatcher.group(2);
        }

        if (messageMatcher.find()) {
            newMessageInfo.messageNotParsed = messageMatcher.group(1);
            newMessageInfo.containUglyImages = ToolForParsing.hasUglyImagesInNotPrettyMessage(newMessageInfo.messageNotParsed);

            newMessageInfo.messageContentContainSpoil = newMessageInfo.messageNotParsed.contains(" class=\"contenu-spoil\">");
            newMessageInfo.signatureContainSpoil = newMessageInfo.signatureNotParsed.contains(" class=\"contenu-spoil\">");

            newMessageInfo.messageNotParsed = makeBasicMessageParse(newMessageInfo.messageNotParsed, newMessageInfo.messageContentContainSpoil);
            newMessageInfo.signatureNotParsed = makeBasicMessageParse(newMessageInfo.signatureNotParsed, newMessageInfo.signatureContainSpoil);

            newMessageInfo.numberOfOverlyQuote = ToolForParsing.countNumberOfOverlyQuoteInPreParsedMessage(newMessageInfo.messageNotParsed);
        }

        return newMessageInfo;
    }

    public static TopicInfos createTopicInfoFromEntireTopic(String thisEntireTopic) {
        TopicInfos newTopicInfo = new TopicInfos();
        Matcher topicNameAndLinkMatcher = topicNameAndLinkPattern.matcher(thisEntireTopic);
        Matcher topicNumberMessagesMatcher = topicNumberMessagesPattern.matcher(thisEntireTopic);
        Matcher topicNumberMessagesAdmMatcher = topicNumberMessagesAdmPattern.matcher(thisEntireTopic);
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

        if (topicNumberMessagesAdmMatcher.find()) {
            newTopicInfo.nbOfMessages = topicNumberMessagesAdmMatcher.group(1);
        } else if (topicNumberMessagesMatcher.find()) {
            newTopicInfo.nbOfMessages = topicNumberMessagesMatcher.group(1);
        }

        if (topicNameAndLinkMatcher.find()) {
            String topicNameAndLinkString = topicNameAndLinkMatcher.group(1);
            newTopicInfo.link = "https://www.jeuxvideo.com" + topicNameAndLinkString.substring(0, topicNameAndLinkString.indexOf("\""));
            newTopicInfo.htmlName = topicNameAndLinkString.substring(topicNameAndLinkString.indexOf("title=\"") + 7);
        }

        if (topicDateMatcher.find()) {
            newTopicInfo.wholeDate = topicDateMatcher.group(1);
        }

        if (topicTypeMatcher.find()) {
            newTopicInfo.type = topicTypeMatcher.group(1);
        }

        return newTopicInfo;
    }

    public static TopicInfos createTopicInfoFromEntireTopicMessageSearch(String thisEntireTopic) {
        TopicInfos newTopicInfo = new TopicInfos();
        Matcher topicNameAndLinkMatcher = topicNameAndLinkInMessageSearchPattern.matcher(thisEntireTopic);
        Matcher topicAuthorMatcher = topicAuthorInMessageSearchPattern.matcher(thisEntireTopic);
        Matcher topicDateMatcher = topicDatePattern.matcher(thisEntireTopic);

        if (topicAuthorMatcher.find()) {
            newTopicInfo.author = topicAuthorMatcher.group(2).trim();
            newTopicInfo.authorType = topicAuthorMatcher.group(1).trim();
        } else {
            newTopicInfo.author = "Pseudo supprimé";
            newTopicInfo.authorType = "user";
        }

        if (topicNameAndLinkMatcher.find()) {
            newTopicInfo.link = "https://www.jeuxvideo.com" + topicNameAndLinkMatcher.group(1);
            newTopicInfo.htmlName = topicNameAndLinkMatcher.group(2).replace("\r", "").replace("\n", "").replace("em>", "u>").trim();
        }

        if (topicDateMatcher.find()) {
            newTopicInfo.wholeDate = topicDateMatcher.group(1);
        }

        newTopicInfo.type = "message";

        return newTopicInfo;
    }

    public static ArrayList<MessageInfos> getMessagesOfThisPage(String sourcePage) {
        ArrayList<MessageInfos> listOfParsedMessage = new ArrayList<>();
        Matcher entireMessageMatcher = entireMessagePattern.matcher(sourcePage);

        while (entireMessageMatcher.find()) {
            listOfParsedMessage.add(createMessageInfoFromEntireMessage(entireMessageMatcher.group(1)));
        }

        Collections.sort(listOfParsedMessage);

        return listOfParsedMessage;
    }

    public static MessageInfos getMessageFromPermalinkPage(String sourcePage) {
        Matcher entireMessageMatcher = entireMessageInPermalinkPattern.matcher(sourcePage);

        if (entireMessageMatcher.find()) {
            MessageInfos newMessage = createMessageInfoFromEntireMessage(entireMessageMatcher.group(1));
            newMessage.userCanQuoteMessage = false;
            return newMessage;
        }

        return null;
    }

    public static String getTopicLinkFromPermalinkPage(String sourcePage) {
        Matcher topicLinkMatcher = topicLinkInPermalinkPattern.matcher(sourcePage);

        if (topicLinkMatcher.find()) {
            return "https://www.jeuxvideo.com" + topicLinkMatcher.group(1);
        }

        return "";
    }

    public static ArrayList<TopicInfos> getTopicsOfThisPage(String sourcePage) {
        ArrayList<TopicInfos> listOfParsedTopic = new ArrayList<>();
        Matcher entireTopicMatcher = entireTopicPattern.matcher(sourcePage);

        while (entireTopicMatcher.find()) {
            if (entireTopicMatcher.group(1).startsWith("class=\"message")) {
                listOfParsedTopic.add(createTopicInfoFromEntireTopicMessageSearch(entireTopicMatcher.group(0)));
            } else {
                listOfParsedTopic.add(createTopicInfoFromEntireTopic(entireTopicMatcher.group(0)));
            }
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
                boolean itsEndingTag = spoilOverlyMatcher.group().startsWith("</");

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

        public static int countNumberOfOverlyQuoteInPreParsedMessage(String preParsedMessage) {
            Matcher htmlTagMatcher = overlyJVCQuotePattern.matcher(preParsedMessage);
            int maxNumberOfOverlyQuoteInMessage = 0;
            int currentNumberOfOverlyQuoteInMessage = 0;
            int lastOffsetOfTag = 0;

            while (htmlTagMatcher.find(lastOffsetOfTag)) {
                if (htmlTagMatcher.group().equals("<blockquote>")) {
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
                Matcher uglyImagesNameMatcher = uglyImagesNamePattern.matcher(currentMatch);

                if (uglyImagesNameMatcher.find()) {
                    return true;
                }

                lastOffsetOfTag = noelshackImageMatcher.end();
            }

            return false;
        }

        public static void parseThisMessageWithThisPattern(StringBuilder messageToParse, Pattern patternToUse, int groupToUse, String stringBefore,
                                                           String stringAfter, Utils.StringModifier firstModifier, Utils.StringModifier secondModifier) {
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

    public static class AjaxInfos implements Parcelable {
        public String list = null;
        public String mod = null;
        public String pref = null;
        public String sub = null;

        public static final Parcelable.Creator<AjaxInfos> CREATOR = new Parcelable.Creator<AjaxInfos>() {
            @Override
            public AjaxInfos createFromParcel(Parcel in) {
                return new AjaxInfos(in);
            }

            @Override
            public AjaxInfos[] newArray(int size) {
                return new AjaxInfos[size];
            }
        };

        public AjaxInfos() {
            //rien
        }

        public AjaxInfos(AjaxInfos baseForCopy) {
            list = baseForCopy.list;
            mod = baseForCopy.mod;
            pref = baseForCopy.pref;
            sub = baseForCopy.sub;
        }

        private AjaxInfos(Parcel in) {
            list = in.readString();
            mod = in.readString();
            pref = in.readString();
            sub = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(list);
            out.writeString(mod);
            out.writeString(pref);
            out.writeString(sub);
        }
    }

    public static class MessageInfos implements Parcelable, Comparable<MessageInfos> {
        public String pseudo = "Pseudo supprimé";
        public String pseudoType = "user";
        public String idAlias = "0";
        public String messageNotParsed = "";
        public String signatureNotParsed = "";
        public String avatarLink = "";
        public String dateTime = "";
        public String wholeDate = "";
        public String lastTimeEdit = "";
        public boolean pseudoIsBlacklisted = false;
        public boolean messageIsDeleted = false;
        public boolean userCanDeleteOrRestoreMessage = false;
        public boolean userCanEditMessage = false;
        public boolean userCanQuoteMessage = true;
        public boolean authorIsKicked = false;
        public boolean userCanKickOrDekickAuthor = false;
        public boolean messageContentContainSpoil = false;
        public boolean signatureContainSpoil = false;
        public int numberOfOverlyQuote = 0;
        public boolean showOverlyQuote = false;
        public boolean isAnEdit = false;
        public boolean containUglyImages = false;
        public boolean showUglyImages = false;
        public long id = 0;
        public int lastIdOfSpoilInMessage = -1;
        public ArraySet<Integer> listOfSpoilIdToShow = new ArraySet<>();

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
            idAlias = in.readString();
            messageNotParsed = in.readString();
            signatureNotParsed = in.readString();
            avatarLink = in.readString();
            dateTime = in.readString();
            wholeDate = in.readString();
            lastTimeEdit = in.readString();
            pseudoIsBlacklisted = (in.readByte() == 1);
            messageIsDeleted = (in.readByte() == 1);
            userCanDeleteOrRestoreMessage = (in.readByte() == 1);
            userCanEditMessage = (in.readByte() == 1);
            userCanQuoteMessage = (in.readByte() == 1);
            authorIsKicked = (in.readByte() == 1);
            userCanKickOrDekickAuthor = (in.readByte() == 1);
            messageContentContainSpoil = (in.readByte() == 1);
            signatureContainSpoil = (in.readByte() == 1);
            numberOfOverlyQuote = in.readInt();
            showOverlyQuote = (in.readByte() == 1);
            isAnEdit = (in.readByte() == 1);
            containUglyImages = (in.readByte() == 1);
            showUglyImages = (in.readByte() == 1);
            id = in.readLong();
            lastIdOfSpoilInMessage = in.readInt();

            final int sizeOfListOfSpoidIdToShow = in.readInt();
            for (int i = 0; i < sizeOfListOfSpoidIdToShow; ++i) {
                listOfSpoilIdToShow.add(in.readInt());
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(pseudo);
            out.writeString(pseudoType);
            out.writeString(idAlias);
            out.writeString(messageNotParsed);
            out.writeString(signatureNotParsed);
            out.writeString(avatarLink);
            out.writeString(dateTime);
            out.writeString(wholeDate);
            out.writeString(lastTimeEdit);
            out.writeByte((byte)(pseudoIsBlacklisted ? 1 : 0));
            out.writeByte((byte)(messageIsDeleted ? 1 : 0));
            out.writeByte((byte)(userCanDeleteOrRestoreMessage ? 1 : 0));
            out.writeByte((byte)(userCanEditMessage ? 1 : 0));
            out.writeByte((byte)(userCanQuoteMessage ? 1 : 0));
            out.writeByte((byte)(authorIsKicked ? 1 : 0));
            out.writeByte((byte)(userCanKickOrDekickAuthor ? 1 : 0));
            out.writeByte((byte)(messageContentContainSpoil ? 1 : 0));
            out.writeByte((byte)(signatureContainSpoil ? 1 : 0));
            out.writeInt(numberOfOverlyQuote);
            out.writeByte((byte)(showOverlyQuote ? 1 : 0));
            out.writeByte((byte)(isAnEdit ? 1 : 0));
            out.writeByte((byte)(containUglyImages ? 1 : 0));
            out.writeByte((byte)(showUglyImages ? 1 : 0));
            out.writeLong(id);
            out.writeInt(lastIdOfSpoilInMessage);

            out.writeInt(listOfSpoilIdToShow.size());
            for (Integer currentSpoilId : listOfSpoilIdToShow) {
                if (currentSpoilId != null) {
                    out.writeInt(currentSpoilId);
                } else {
                    out.writeInt(-8); //valeur impossible à avoir en temps normal, le minimum est censé être -1.
                }
            }
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
        public String nbOfMessages = "";

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
            nbOfMessages = in.readString();
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
            out.writeString(nbOfMessages);
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

    public static class SurveyReplyInfos implements Parcelable {
        public final String infosForReply;
        public final String titleOfReply;

        public static final Parcelable.Creator<SurveyReplyInfos> CREATOR = new Parcelable.Creator<SurveyReplyInfos>() {
            @Override
            public SurveyReplyInfos createFromParcel(Parcel in) {
                return new SurveyReplyInfos(in);
            }

            @Override
            public SurveyReplyInfos[] newArray(int size) {
                return new SurveyReplyInfos[size];
            }
        };

        private SurveyReplyInfos(String newInfos, String newTitle) {
            infosForReply = newInfos;
            titleOfReply = newTitle;
        }

        private SurveyReplyInfos(Parcel in) {
            infosForReply = in.readString();
            titleOfReply = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(infosForReply);
            out.writeString(titleOfReply);
        }
    }

    private static class ConvertStringToString implements Utils.StringModifier {
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

    private static class AddPrefixString implements Utils.StringModifier {
        private final String stringPrefix;

        AddPrefixString(String newStringPrefix) {
            stringPrefix = newStringPrefix;
        }

        @Override
        public String changeString(String baseString) {
            return stringPrefix + baseString;
        }
    }

    private static class RemoveFirstsAndLastsPAndBr implements Utils.StringModifier {
        @Override
        public String changeString(String baseString) {
            while (baseString.startsWith("<p>") || baseString.startsWith("<br />")) {
                baseString = baseString.substring(baseString.indexOf(">") + 1);
            }
            while (baseString.endsWith("</p>") || baseString.endsWith("<br />")) {
                baseString = baseString.substring(0, baseString.lastIndexOf("<"));
            }
            return baseString;
        }
    }

    private static class ConvertUrlToStickerId implements Utils.StringModifier {
        @Override
        public String changeString(String baseString) {
            if (baseString.endsWith("/")) {
                baseString = baseString.substring(0, baseString.length() - 1);
            }
            if (baseString.contains("/")) {
                return baseString.substring(baseString.lastIndexOf("/") + 1);
            }
            return baseString;
        }
    }

    private static class SuppressIfContainUglyNames implements Utils.StringModifier {
        @Override
        public String changeString(String baseString) {
            Matcher uglyImagesNameMatcher = uglyImagesNamePattern.matcher(baseString);
            if (uglyImagesNameMatcher.find()) {
                return "";
            }
            return baseString;
        }
    }

    private static class MakeCodeTagGreatAgain implements Utils.StringModifier {
        private final boolean isCodeBlock;

        MakeCodeTagGreatAgain(boolean newIsCodeBlock) {
            isCodeBlock = newIsCodeBlock;
        }

        @Override
        public String changeString(String baseString) {
            if (isCodeBlock) {
                while (baseString.startsWith("\n")) {
                    baseString = baseString.substring(1);
                }
                while (baseString.endsWith("\n")) {
                    baseString = baseString.substring(0, baseString.length() - 1);
                }
                baseString = baseString.replace("\n", "<br />");
            } else {
                if (baseString.startsWith(" ")) {
                    baseString = "&nbsp;" + baseString.substring(1);
                }
                if (baseString.endsWith(" ")) {
                    baseString = baseString.substring(0, baseString.length() - 1) + "&nbsp;";
                }
            }

            return baseString.replace("  ", "&nbsp;&nbsp;");
        }
    }

    private static class MakeShortenedLinkIfPossible implements Utils.StringModifier {
        private final int maxStringSize;
        private final boolean forceLinkCreation;

        MakeShortenedLinkIfPossible(int newMaxStringSize, boolean newForceLinkCreation) {
            maxStringSize = newMaxStringSize;
            forceLinkCreation = newForceLinkCreation;
        }

        @Override
        public String changeString(String baseString) {
            if (baseString != null && (forceLinkCreation || ((baseString.startsWith("http://") || baseString.startsWith("https://")) && !baseString.contains(" ")))) {
                String linkShowed = baseString;
                if (maxStringSize > 0 && linkShowed.length() > maxStringSize + 3) {
                    linkShowed = linkShowed.substring(0, maxStringSize / 2) + "[…]" + linkShowed.substring(linkShowed.length() - (maxStringSize / 2));
                }
                baseString = "<a href=\"" + baseString + "\">" + linkShowed + "</a>";
            }
            return baseString;
        }
    }

    private static class MakeNoelshackImageLink implements Utils.StringModifier {
        private final boolean useFichierXsForPng;

        MakeNoelshackImageLink(boolean newUseFichierXsForPng) {
            useFichierXsForPng = newUseFichierXsForPng;
        }

        @Override
        public String changeString(String baseString) {
            String imageLink = noelshackToDirectLink(baseString);

            if (useFichierXsForPng && imageLink.endsWith(".png")) {
                imageLink = "http://image.noelshack.com/fichiers-xs/" + imageLink.substring(("http://image.noelshack.com/fichiers/").length());
            } else {
                imageLink = "http://image.noelshack.com/minis/" + imageLink.substring(("http://image.noelshack.com/fichiers/").length());
                imageLink = imageLink.substring(0, imageLink.lastIndexOf(".")) + ".png";
            }

            return "<a href=\"" + baseString + "\"><img src=\"" + imageLink + "\"/></a>";
        }
    }

    private static class BuildSpoilTag implements Utils.StringModifier {
        private final String spoilButtonCode = "<bg_spoil_button><font color=\"#" + (ThemeManager.currentThemeUseDarkColors() ? "000000" : "FFFFFF") +
                                               "\">&nbsp;SPOIL&nbsp;</font></bg_spoil_button>";

        private final ArraySet<Integer> listOfSpoilIdToShow;
        private final boolean showAllSpoils;
        private int lastIdUsed;
        private boolean itsForSpoilBlock = false;

        public BuildSpoilTag(ArraySet<Integer> newListOfSpoilIdToShow, int newLastIdUsed) {
            listOfSpoilIdToShow = newListOfSpoilIdToShow;
            showAllSpoils = listOfSpoilIdToShow.contains(-1);
            lastIdUsed = newLastIdUsed;
        }

        public int getLastIdUsedForSpoil() {
            return lastIdUsed;
        }

        public void setItsForSpoilBlock(boolean newVal) {
            itsForSpoilBlock = newVal;
        }

        @Override
        public String changeString(String baseString) {
            //lastIdUsed + 1 est l'ID utilisé pour la balise actuelle
            String id = String.valueOf(++lastIdUsed);
            boolean showThisSpoil = showAllSpoils || listOfSpoilIdToShow.contains(lastIdUsed);

            if (showThisSpoil) {
                return "<holdstring_c" + id + ">" + spoilButtonCode + "</holdstring_c" + id + ">" + "<bg_spoil_content><font color=\"#" +
                        (ThemeManager.currentThemeUseDarkColors() ? "FFFFFF" : "000000") + "\">" + (itsForSpoilBlock ? "<br />" : " ") +
                        baseString + "</font></bg_spoil_content>";
            } else {
                return "<holdstring_o" + id + ">" + spoilButtonCode + "</holdstring_o" + id + ">";
            }
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

        public ForumAndTopicName() {
            //rien
        }

        public ForumAndTopicName(ForumAndTopicName baseForCopy) {
            forum = baseForCopy.forum;
            topic = baseForCopy.topic;
        }
    }

    public static class Settings {
        public String pseudoOfUser = "";
        public String pseudoOfAuthor = "";
        public String firstLineFormat;
        public String secondLineFormat;
        public String addBeforeEdit;
        public String addAfterEdit;
        public String colorPseudoUser;
        public String colorPseudoOther;
        public String colorPseudoModo;
        public String colorPseudoAdmin;
        public int maxNumberOfOverlyQuotes = 0;
        public PrefsManager.PseudoColorType typeOfPseudoToColorInInfoLine = new PrefsManager.PseudoColorType(PrefsManager.PseudoColorType.ALL_ACCOUNTS);
        public PrefsManager.PseudoColorType typeOfPseudoToColorInMessage = new PrefsManager.PseudoColorType(PrefsManager.PseudoColorType.ALL_ACCOUNTS);
        public boolean applyMarkToPseudoAuthor = false;
        public boolean showNoelshackImages = false;
        public boolean transformStickerToSmiley = false;
        public boolean shortenLongLink = false;
        public boolean hideUglyImages = false;
        public boolean enableAlphaInNoelshackMini = false;
    }

    private static class SpoilTagsInfos {
        public ArraySet<Integer> listOfSpoilIdToShow = null;
        public boolean containSpoil = false;
        public int lastIdOfSpoil = -1;
    }
}
