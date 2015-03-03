//	---------------------------------------------------------------------------
//	jWebSocket AMQ PlugIn Tools (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.amq;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class Tools {

    /**
     * Performs a wildcard matching for the text and pattern provided.
     *
     * @param aText the text to be tested for matches.
     *
     * @param aPattern the pattern to be matched for. This can contain the
     * wildcard character '*' (asterisk).
     *
     * @see http://www.adarshr.com/papers/wildcard
     * @return <tt>true</tt> if a match is found, <tt>false</tt>
     * otherwise.
     */
    public static boolean wildCardMatch(String aText, String aPattern) {
        if ("*".equals(aPattern) || aText.equals(aPattern)) {
            return true;
        }

        // Create the cards by splitting using a RegEx. If more speed 
        // is desired, a simpler character based splitting can be done.
        String[] lCards = aPattern.split("\\*");

        // Iterate over the cards.
        for (String lCard : lCards) {
            int lIdx = aText.indexOf(lCard);

            // Card not detected in the text.
            if (lIdx == -1) {
                return false;
            }

            // Move ahead, towards the right of the text.
            aText = aText.substring(lIdx + lCard.length());
        }

        return true;
    }

    /**
     * Performs a wildcard matching for the text and patterns list provided.
     *
     * @param aPatterns the texts array to be tested for matches.
     *
     * @param aText the pattern to be matched for. This can contain the wildcard
     * character '*' (asterisk).
     *
     * @return <tt>true</tt> if a match is found, <tt>false</tt>
     * otherwise.
     */
    public static boolean wildCardMatch(String[] aPatterns, String aText) {
        for (String aPattern : aPatterns) {
            if (wildCardMatch(aText, aPattern)) {
                return true;
            }
        }
        return false;
    }
}
