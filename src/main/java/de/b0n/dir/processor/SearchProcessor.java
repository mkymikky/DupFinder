package de.b0n.dir.processor;

import de.b0n.dir.DupFinderCallback;

/**
 * Created by huluvu424242 on 25.02.17.
 */
public abstract class SearchProcessor {

    protected final ProcessorID ID=new ProcessorID(this.getClass().getName());

    /**
     * Created by huluvu424242 on 25.02.17.
     */
    public static class ProcessorID {

        protected String id;

        public ProcessorID(final String id){
            this.id=id;
        }

        public  String toString(){
            return id;
        }
    }
}
