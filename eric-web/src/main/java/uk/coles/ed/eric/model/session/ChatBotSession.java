package uk.coles.ed.eric.model.session;

import java.util.Map;

public class ChatBotSession {
    private final int DEFAULT_APOLOGIES = 0;

    private int apologies;
    private Map<String, AnnActivation> anState;

    public int getApologiesCount() {
        return apologies;
    }

    public void resetApologiesCount() {
        apologies = DEFAULT_APOLOGIES;
    }

    public Map<String, AnnActivation> getAnState() {
        return anState;
    }
}
