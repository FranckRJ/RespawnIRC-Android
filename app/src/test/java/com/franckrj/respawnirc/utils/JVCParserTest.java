package com.franckrj.respawnirc.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class JVCParserTest {
    @Test
    public void deobfuscateJvcareLink_isCorrect() throws Exception {
        assertEquals(
                "/forums/42-1000021-40573812-128-0-1-0-pc-android-respawnirc.htm#post_1300105844",
                JVCParser.deobfuscateJvcareLink("1F444FC1C34EC21F2B211E2A20202020212A1E2B20232C22252A211E2A21251E201E2A1E201EC0421E4A494BC14F464B1EC143C2C04ACC4946C1421945CB4E12C04FC2CB3F2A2220202A2023252B2B")
        );
    }

    @Test
    public void getTopicLinkFromPermalinkPage_prependsHostToRelativeLink() throws Exception {
        assertEquals(
                "https://www.jeuxvideo.com/forums/42-1000021-40573812-128-0-1-0-pc-android-respawnirc.htm#post_1300105844",
                JVCParser.getTopicLinkFromPermalinkPage("<span class=\"JvCare 1F444FC1C34EC21F2B211E2A20202020212A1E2B20232C22252A211E2A21251E201E2A1E201EC0421E4A494BC14F464B1EC143C2C04ACC4946C1421945CB4E12C04FC2CB3F2A2220202A2023252B2B backTo\">")
        );
    }

    @Test
    public void getTopicLinkFromPermalinkPage_returnsEmptyWhenNoLink() throws Exception {
        assertEquals(
                "",
                JVCParser.getTopicLinkFromPermalinkPage("<span class=\"JvCare 1F444FC1C34EC2 une-autre-classe\">")
        );
    }
}
