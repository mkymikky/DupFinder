package de.b0n.dir;

import de.b0n.dir.processor.AbstractModel;
import de.b0n.dir.processor.ModelFactory;

/**
 * Created by huluvu424242 on 05.03.17.
 */
public class DupFinderModel<G,E> extends AbstractModel<G,E>{

    public DupFinderModel(){

    }


    @Override
    public ModelFactory getModelFactory() {

        return new ModelFactory(){

            @Override
            public AbstractModel createModel() {
                return new DupFinderModel();
            }
        };
    }
}
