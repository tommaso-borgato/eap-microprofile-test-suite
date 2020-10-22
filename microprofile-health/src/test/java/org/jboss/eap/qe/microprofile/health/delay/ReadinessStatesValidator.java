package org.jboss.eap.qe.microprofile.health.delay;

import static org.jboss.eap.qe.microprofile.health.delay.ReadinessState.END;
import static org.jboss.eap.qe.microprofile.health.delay.ReadinessState.START;

import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;

/**
 * Validates sequences of states returned by the readiness health check probe
 */
public class ReadinessStatesValidator {

    public static ReadinessStatesValidator of(ReadinessChecker stateGetter) {
        return new ReadinessStatesValidator(stateGetter.getStates());
    }

    private final List<ReadinessState> states;

    private final static Logger LOGGER = Logger.getLogger(ReadinessStatesValidator.class.getName());

    private ReadinessStatesValidator(List<ReadinessState> states) {
        this.states = states;
    }

    public ReadinessStatesValidator containSequence(ReadinessState... sequence) {
        LOGGER.info("[MARKER] Sequence of states requested: " + statesToString(false, sequence) + " real Sequence: "
                + statesToString(true));
        if (states.size() < sequence.length) {
            throw new AssertionError(
                    "[1] Sequence of states " + statesToString(false, sequence) + " is not in " + statesToString(true)
                            + " as expected");
        }
        for (int actualStatesIndex = 0; actualStatesIndex < states.size(); actualStatesIndex++) {
            if (states.get(actualStatesIndex).equals(sequence[0]) && states.size() - actualStatesIndex >= sequence.length) {
                boolean ok = true;
                for (int lookupSequenceIndex = 1; lookupSequenceIndex < sequence.length; lookupSequenceIndex++) {
                    if (!sequence[lookupSequenceIndex].equals(states.get(actualStatesIndex + lookupSequenceIndex))) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    return this;
                }
            }
        }
        throw new AssertionError(
                "[2] Sequence of states " + statesToString(false, sequence) + " is not in " + statesToString(true)
                        + " as expected");
    }

    private String statesToString(boolean withTimestamp) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < states.size(); i++) {
            sb.append("<").append(states.get(i).toString(withTimestamp));
            sb.append(">");
            if (i < states.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String statesToString(boolean withTimestamp, ReadinessState... sequence) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < sequence.length; i++) {
            sb.append("<").append(sequence[i].toString(withTimestamp));
            sb.append(">");
            if (i < sequence.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public ReadinessStatesValidator finishedProperly() {
        Assert.assertEquals("The first state must be start", START(), states.get(0));
        Assert.assertEquals("The last state must be end", END(), states.get(states.size() - 1));
        return this;
    }
}
