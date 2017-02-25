package de.b0n.dir.processor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public interface SearchProcessorCallback {

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss:sss");

    /**
     * Wird aufgerufen wenn der Processor mit der Arbeit beginnt
     *
     * @param date
     */
    default void processorStartAt(final ProcessorID id, final Date date){
        System.out.println("Processor "+id+" gestartet um "+ DATE_FORMATTER.format(date)+" .");
    }


    /**
     * Wird aufgerufen wenn der Processor seine Arbeit beendet
     *
     * @param date
     */
    default void processorEndsAt(final ProcessorID id, final Date date){
        System.out.println("Processor "+id+" beendet um "+DATE_FORMATTER.format(date)+" .");
    }



}
