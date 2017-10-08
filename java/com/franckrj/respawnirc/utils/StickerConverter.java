package com.franckrj.respawnirc.utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class StickerConverter {
    public static ArrayList<InfoForConvert> ruleForNoLangageSticker = null;
    public static ArrayList<InfoForConvert> ruleForStickerToSmiley = null;

    public static void initializeBasesRules() {
        ruleForNoLangageSticker = new ArrayList<>();
        ruleForNoLangageSticker.add(new InfoForConvert("1jc3", "1jc3-fr", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1lej", "1lej-en", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1leq", "1leq-en", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1n1q", "1n1q-fr", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1n1t", "1n1t-fr", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1n1r", "1n1r-fr", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1n1o", "1n1o-fr", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1n1n", "1n1n-fr", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1n1m", "1n1m-fr", false));
        ruleForNoLangageSticker.add(new InfoForConvert("1n1p", "1n1p-fr", false));
        ruleForNoLangageSticker.add(new InfoForConvert("zuc", "zuc-fr", false));

        ruleForStickerToSmiley = new ArrayList<>();
        ruleForStickerToSmiley.add(new InfoForConvert("1jnh", "45.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kki", "1.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jng", "15.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jni", "56.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jnj", "61.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kku", "hapoelparty.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1klb", "54.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl8", "54.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lgg", "45.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jnf", "45.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jne", "57.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kkl", "play.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kkh", "60.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lmk", "14.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jnd", "67.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lmh", "nyu.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kkn", "36.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jnc", "41.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kkr", "11.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kkq", "11.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lgd", "70.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kkp", "11.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1ljp", "36.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lmn", "62.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl6", "66.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("zu2", "41.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lml", "27.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lge", "36.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lm9", "36.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("zuc-fr", "36.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jc5", "54.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl4", "52.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jc3-fr", "42.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1jch", "41.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lmc", "14.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lmb", "39.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lma", "70.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lgc", "2.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1mqx", "62.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("zua", "27.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("zu9", "15.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("zu8", "57.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lgf", "70.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lgb", "15.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lgh", "pf.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lga", "46.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1nu6", "56.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kgu", "61.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl3", "68.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl5", "27.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl2", "46.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1li4", "26.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kh1", "70.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl1", "2.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lt7", "32.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1lt8", "30.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1ltb", "51.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1ptd", "36.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kkt", "31.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl0", "19.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kl9", "play.gif", true));
        ruleForStickerToSmiley.add(new InfoForConvert("1kks", "21.gif", true));
    }

    public static void convertStickerWithThisRule(StringBuilder messageToConvert, ArrayList<InfoForConvert> ruleToFollow) {
        if (ruleToFollow != null) {
            for (InfoForConvert thisInfo : ruleToFollow) {
                JVCParser.ToolForParsing.parseThisMessageWithThisPattern(messageToConvert, thisInfo.base, -1, thisInfo.replacement, "", null, null);
            }
        }
    }

    private static class InfoForConvert {
        private Pattern base;
        private String replacement;

        private InfoForConvert(String baseString, String replacementString, boolean itsAConvertToSmiley) {
            base = Pattern.compile("<img class=\"img-stickers\" src=\"http://jv\\.stkr\\.fr/p[^/]*/" + baseString + "\".*?/>");

            if (itsAConvertToSmiley) {
                replacement = "<img src=\"http://image.jeuxvideo.com/smileys_img/" + replacementString + "\" alt=\"\" data-code=\"\" title=\"\" />";
            } else {
                replacement = "<img class=\"img-stickers\" src=\"http://jv.stkr.fr/p/" + replacementString + "\"/>";
            }
        }
    }
}
