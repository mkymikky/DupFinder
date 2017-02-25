package de.b0n.dir.processor;

import java.util.Date;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public interface SuchProcessorCallback {


    /**
     * Wird aufgerufen wenn der Processor mit der Arbeit beginnt
     *
     * @param date
     */
    void processorStartAt(final ProcessorID id, final Date date);


    /**
     * Wird aufgerufen wenn der Processor seine Arbeit beendet
     *
     * @param date
     */
    void processorEndsAt(final ProcessorID id, final Date date);
}
