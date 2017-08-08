package com.franckrj.respawnirc.utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class StickerConverter {
    public static StickerConvertRule ruleForNoLangageSticker = null;
    public static StickerConvertRule ruleForStickerToSmiley = null;

    public static void initializeBasesRules() {
        ruleForNoLangageSticker = new StickerConvertRule();
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1jc3", "1jc3-fr"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1lej", "1lej-en"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1leq", "1leq-en"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1n1q", "1n1q-fr"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1n1t", "1n1t-fr"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1n1r", "1n1r-fr"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1n1o", "1n1o-fr"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1n1n", "1n1n-fr"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1n1m", "1n1m-fr"));
        ruleForNoLangageSticker.listOfConvert.add(new InfoForConvert("1n1p", "1n1p-fr"));
        ruleForNoLangageSticker.convertToSmiley = false;

        ruleForStickerToSmiley = new StickerConvertRule();
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jnh", "45.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kki", "1.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jng", "15.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jni", "56.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jnj", "61.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kku", "hapoelparty.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1klb", "54.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl8", "54.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lgg", "45.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jnf", "45.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jne", "57.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kkl", "play.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kkh", "60.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lmk", "14.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jnd", "67.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lmh", "nyu.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kkn", "36.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jnc", "41.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kkr", "11.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kkq", "11.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lgd", "70.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kkp", "11.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1ljp", "36.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lmn", "62.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl6", "66.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("zu2", "41.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lml", "27.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lge", "36.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lm9", "36.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("zuc", "36.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jc5", "54.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl4", "52.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jc3-fr", "42.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1jch", "41.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lmc", "14.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lmb", "39.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lma", "70.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lgc", "2.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1mqx", "62.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("zua", "27.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("zu9", "15.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("zu8", "57.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lgf", "70.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lgb", "15.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lgh", "pf.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lga", "46.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1nu6", "56.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kgu", "61.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl3", "68.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl5", "27.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl2", "46.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1li4", "26.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kh1", "70.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl1", "2.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lt7", "32.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1lt8", "30.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1ltb", "51.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1ptd", "36.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kkt", "31.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl0", "19.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kl9", "play.gif"));
        ruleForStickerToSmiley.listOfConvert.add(new InfoForConvert("1kks", "21.gif"));
        ruleForStickerToSmiley.convertToSmiley = true;
    }

    public static void convertStickerWithThisRule(StringBuilder messageToConvert, StickerConvertRule ruleToFollow) {
        if (ruleToFollow != null) {
            for (InfoForConvert thisInfo : ruleToFollow.listOfConvert) {
                if (!ruleToFollow.convertToSmiley) {
                    JVCParser.ToolForParsing.parseThisMessageWithThisPattern(messageToConvert, Pattern.compile("<img class=\"img-stickers\" src=\"http://jv\\.stkr\\.fr/p[^/]*/" + thisInfo.base + "\".*?/>"), -1,
                                                                "<img class=\"img-stickers\" src=\"http://jv.stkr.fr/p/" + thisInfo.replacement + "\"/>", "", null, null);
                } else {
                    JVCParser.ToolForParsing.parseThisMessageWithThisPattern(messageToConvert, Pattern.compile("<img class=\"img-stickers\" src=\"http://jv\\.stkr\\.fr/p[^/]*/" + thisInfo.base + "\".*?/>"), -1,
                            "<img src=\"http://image.jeuxvideo.com/smileys_img/" + thisInfo.replacement + "\" alt=\"\" data-code=\"\" title=\"\" />", "", null, null);
                }
            }
        }
    }

    public static class StickerConvertRule {
        ArrayList<InfoForConvert> listOfConvert = new ArrayList<>();
        boolean convertToSmiley = false;
    }

    public static class InfoForConvert {
        public String base = "";
        public String replacement = "";

        InfoForConvert(String newBase, String newReplacement) {
            base = newBase;
            replacement = newReplacement;
        }
    }
}
