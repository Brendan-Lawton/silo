package de.tum.bgu.msm.syntheticPopulationGenerator.munich2022;

import de.tum.bgu.msm.syntheticPopulationGenerator.munich2022.DataSetSynPop;

abstract class AbstractInputReader {

    protected final DataSetSynPop dataSet;

    AbstractInputReader(DataSetSynPop dataSet) {
        this.dataSet = dataSet;
    }

    public abstract void read();
}
