package uk.coles.ed.eric.utils;

import uk.coles.ed.eric.data.access.ChatBotDAO;

/**
 * Natural Language Processing Utilities
 */
public class NlpUtilities {
    private ChatBotDAO chatBotDAO;

    public boolean isStopWord(String word) {
        return chatBotDAO.getStopWords().contains(word);
    }
}
