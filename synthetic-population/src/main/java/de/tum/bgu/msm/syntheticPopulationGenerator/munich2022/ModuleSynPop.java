package de.tum.bgu.msm.syntheticPopulationGenerator.munich2022;

import de.tum.bgu.msm.syntheticPopulationGenerator.munich2022.DataSetSynPop;

/**
 * Created by Ana Moreno on 29.11.2017. Adapted from MITO
 */


public abstract class ModuleSynPop {

    protected final de.tum.bgu.msm.syntheticPopulationGenerator.munich2022.DataSetSynPop dataSetSynPop;

    protected ModuleSynPop(DataSetSynPop dataSetSynPop){this.dataSetSynPop = dataSetSynPop;}

    public abstract void run();

}
