package com.my.televip.obfuscate.resolve;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps the client checks honest: if reading the call sites broke, every client would pass them
 * by checking nothing.
 */
public class CallSitesTest {

    @Test
    public void readsEveryKindOfCallSite() throws Exception {
        List<CallSites.Site> sites = CallSites.read();
        assertTrue("only " + sites.size() + " call sites found", sites.size() > 200);

        Set<String> keys = new HashSet<>();
        for (CallSites.Site s : sites) keys.add(s.key());
        String[] expected = {
                "ConnectionsManager#sendRequestInternal",                       // resolve(owner, name, Method)
                "ChatActivity.pinnedMessageView",                               // resolve(owner, name, Field)
                "MessagesController#getInputChannelO2",                         // resolveOverload
                "MessagesController#storiesEnabled",                            // hookMethod(cls, owner, names[])
                "org.telegram.tgnet.TLRPC$TL_messages_getSponsoredMessages",    // ClassNames constant
                "PhotoViewer#openPhotoOOOOAAAIOOJJJZOI",
        };
        for (String key : expected) assertTrue("call site not found: " + key, keys.contains(key));
    }

    @Test
    public void fallbacksAndRoutesNameRealCallSites() throws Exception {
        List<CallSites.Site> sites = CallSites.read();
        CallSites.Verdict v = CallSites.verdict(sites, java.util.Collections.<CallSites.Site>emptyList());
        for (Map.Entry<String, List<String>> e : v.broken.entrySet()) {
            fail(e.getKey() + ": " + e.getValue());
        }
    }
}
