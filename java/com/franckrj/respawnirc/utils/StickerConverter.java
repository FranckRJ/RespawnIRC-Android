package com.franckrj.respawnirc.utils;

import java.util.ArrayList;

public class StickerConverter {
    public static StickerConvertRule ruleForNoLangageSticker = null;

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
    }

    public static String convertStickerWithThisRule(String messageToConvert, StickerConvertRule ruleToFollow) {
        if (ruleToFollow != null) {
            for (InfoForConvert thisInfo : ruleToFollow.listOfConvert) {
                if (!ruleToFollow.convertToSmiley) {
                    messageToConvert = messageToConvert.replace("<img class=\"img-stickers\" src=\"http://jv.stkr.fr/p/" + thisInfo.base + "\"/>",
                                                                "<img class=\"img-stickers\" src=\"http://jv.stkr.fr/p/" + thisInfo.replacement + "\"/>");
                }
            }
        }

        return messageToConvert;
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
