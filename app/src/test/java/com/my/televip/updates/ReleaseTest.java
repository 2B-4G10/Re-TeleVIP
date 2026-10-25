package com.my.televip.updates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReleaseTest {

    @Test
    public void newerVersions() {
        assertTrue(Release.isNewer("v1.4", "1.3"));
        assertTrue(Release.isNewer("v1.3.1", "1.3"));
        assertTrue(Release.isNewer("v1.10", "1.9"));
        assertTrue(Release.isNewer("v2.0", "1.12.5"));
    }

    @Test
    public void sameOrOlderVersions() {
        assertFalse(Release.isNewer("v1.3", "1.3"));
        assertFalse(Release.isNewer("v1.3.0", "1.3"));
        assertFalse(Release.isNewer("v1.2.2", "1.3"));
        assertFalse(Release.isNewer("v1.9", "1.10"));
    }

    @Test
    public void oddTagsNeverPrompt() {
        assertFalse(Release.isNewer("nightly", "1.3"));
        assertFalse(Release.isNewer("", "1.3"));
        assertFalse(Release.isNewer(null, "1.3"));
        assertFalse(Release.isNewer("v99999999999", "1.3"));
    }

    @Test
    public void notesKeepWhatsNewOnly() {
        String body = "\n- **You decide when the sender sees it.** With Prevent media on,\n"
                + "  opening it tells the sender nothing.\n"
                + "- **Graph Messenger is supported.**\n\n\n"
                + "### Install\n\n1. Download **TeleVip-…-release.apk** below.\n";
        assertEquals("• You decide when the sender sees it. With Prevent media on, opening it tells the sender nothing.\n\n"
                + "• Graph Messenger is supported.", Release.notes(body));
    }

    @Test
    public void notesSkipALeadingHeading() {
        assertEquals("• Fixed.", Release.notes("### What's new\n\n- Fixed.\n\n### Install\nsteps"));
        assertEquals("", Release.notes(null));
    }
}
