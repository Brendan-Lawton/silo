package run;

import de.tum.bgu.msm.container.DataContainer;
import de.tum.bgu.msm.container.ModelContainer;
import de.tum.bgu.msm.data.dwelling.DwellingFactory;
import de.tum.bgu.msm.data.household.Household;
import de.tum.bgu.msm.data.household.HouseholdFactory;
import de.tum.bgu.msm.data.person.PersonFactory;
import de.tum.bgu.msm.matsim.*;
import de.tum.bgu.msm.models.autoOwnership.CreateCarOwnershipModel;
import de.tum.bgu.msm.models.demography.birth.BirthModel;
import de.tum.bgu.msm.models.demography.birth.BirthModelImpl;
import de.tum.bgu.msm.models.demography.birth.DefaultBirthStrategy;
import de.tum.bgu.msm.models.demography.birthday.BirthdayModel;
import de.tum.bgu.msm.models.demography.birthday.BirthdayModelImpl;
import de.tum.bgu.msm.models.demography.death.DeathModel;
import de.tum.bgu.msm.models.demography.death.DeathModelImpl;
import de.tum.bgu.msm.models.demography.death.DefaultDeathStrategy;
import de.tum.bgu.msm.models.demography.divorce.DefaultDivorceStrategy;
import de.tum.bgu.msm.models.demography.divorce.DivorceModel;
import de.tum.bgu.msm.models.demography.divorce.DivorceModelImpl;
import de.tum.bgu.msm.models.demography.driversLicense.DefaultDriversLicenseStrategy;
import de.tum.bgu.msm.models.demography.driversLicense.DriversLicenseModel;
import de.tum.bgu.msm.models.demography.driversLicense.DriversLicenseModelImpl;
import de.tum.bgu.msm.models.demography.education.EducationModel;
import de.tum.bgu.msm.models.demography.education.EducationModelImpl;
import de.tum.bgu.msm.models.demography.employment.EmploymentModel;
import de.tum.bgu.msm.models.demography.employment.EmploymentModelImpl;
import de.tum.bgu.msm.models.demography.leaveParentalHousehold.DefaultLeaveParentalHouseholdStrategy;
import de.tum.bgu.msm.models.demography.leaveParentalHousehold.LeaveParentHhModel;
import de.tum.bgu.msm.models.demography.leaveParentalHousehold.LeaveParentHhModelImpl;
import de.tum.bgu.msm.models.demography.marriage.DefaultMarriageStrategy;
import de.tum.bgu.msm.models.demography.marriage.MarriageModel;
import de.tum.bgu.msm.models.demography.marriage.MarriageModelImpl;
import de.tum.bgu.msm.models.jobmography.JobMarketUpdate;
import de.tum.bgu.msm.models.jobmography.JobMarketUpdateImpl;
import de.tum.bgu.msm.models.modeChoice.CommuteModeChoice;
import de.tum.bgu.msm.models.realEstate.construction.*;
import de.tum.bgu.msm.models.realEstate.demolition.DefaultDemolitionStrategy;
import de.tum.bgu.msm.models.realEstate.demolition.DemolitionModel;
import de.tum.bgu.msm.models.realEstate.demolition.DemolitionModelImpl;
import de.tum.bgu.msm.models.realEstate.pricing.DefaultPricingStrategy;
import de.tum.bgu.msm.models.realEstate.pricing.PricingModel;
import de.tum.bgu.msm.models.realEstate.pricing.PricingModelImpl;
import de.tum.bgu.msm.models.realEstate.renovation.DefaultRenovationStrategy;
import de.tum.bgu.msm.models.realEstate.renovation.RenovationModel;
import de.tum.bgu.msm.models.realEstate.renovation.RenovationModelImpl;
import de.tum.bgu.msm.models.relocation.migration.InOutMigration;
import de.tum.bgu.msm.models.relocation.migration.InOutMigrationImpl;
import de.tum.bgu.msm.models.relocation.moves.*;
import de.tum.bgu.msm.models.transportModel.TransportModel;
import de.tum.bgu.msm.properties.Properties;
import de.tum.bgu.msm.utils.SiloUtil;
import models.FabilandConstructionLocationStrategy;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Random;

import static de.tum.bgu.msm.matsim.SimpleCommuteModeChoiceMatsimScenarioAssembler.*;
import static de.tum.bgu.msm.matsim.ZoneConnectorManagerImpl.*;

public class ModelBuilderFabiland {

    public static ModelContainer getModelContainer(DataContainer dataContainer, Properties properties, Config config) {

        PersonFactory ppFactory = dataContainer.getHouseholdDataManager().getPersonFactory();
        HouseholdFactory hhFactory = dataContainer.getHouseholdDataManager().getHouseholdFactory();
        DwellingFactory ddFactory = dataContainer.getRealEstateDataManager().getDwellingFactory();

        final BirthModel birthModel = new BirthModelImpl(dataContainer, ppFactory, properties, new DefaultBirthStrategy(), SiloUtil.provideNewRandom());

        BirthdayModel birthdayModel = new BirthdayModelImpl(dataContainer, properties, SiloUtil.provideNewRandom());

        DeathModel deathModel = new DeathModelImpl(dataContainer, properties, new DefaultDeathStrategy(), SiloUtil.provideNewRandom());

        MovesModel movesModel;
        {

            final DwellingUtilityStrategy dwellingUtilityStrategy = new DwellingUtilityStrategyImpl();
            // (This is something like
            // [alpha * sizeUtility + beta * autoAccessibilityUtility + gamma * transitAccessibilityUtility + (1.0 - alpha - beta - gamma) * qualityUtility]^delta   * priceUtl^eps * workDistanceUtl^{1-delta-eps}
            // That is, some kind of Cobbs Douglas function with contributions
            // * workDistanceUtil
            // * priceUtl
            // * a weighted sum of sizeUtl, autoAccUtl, transitAccUtl, qualityUtl.
            // All of the params depend on income and hh size, and are given for corresponding categories.
            // )

            final DwellingProbabilityStrategy dwellingProbabilityStrategy = new DefaultDwellingProbabilityStrategy();
            // (is just exp(beta*util); should be called "weight" instead of "probability" since it is not normalized.  kai, apr'26)

            final RegionUtilityStrategy regionUtilityStrategy = new RegionUtilityStrategyImpl();
            // (This is something like (1 - alpha) * price + alpha * accessibility, with alpha depending on the income category.)

            final RegionProbabilityStrategy regionProbabilityStrategy = new RegionProbabilityStrategyImpl();
            //( same as DefaultDwellingProbabilityStrategy, see above. kai, apr'26)

            final CommuteModeChoice commuteModeChoice1 = new SimpleMatsimCommuteModeChoice( dataContainer, properties, SiloUtil.provideNewRandom() );
            // (there is a comment in SimpleCommuteModeChoiceHousingStrategyImpl that the constructor should actually work w/o providing the CommuteChoiceModel.  It then provides
            // CommuteModeChoice internally, as SimpleCommuteModeChoice.  That model looks similar; presumably some of the lookups (e.g. travel time) used to have different arguments.
            // kai, apr'26)
            // (I think that the existing implementations do the following: compute the logit probas for car and pt if car is an option; then go from HH member with largest car
            // proba down and select car with logit proba as long as another car is available in the HH.)
            // (yy this implies, as long as nothing else comes in, that moves consider new residencies under the assumption that they will not change the number of vehicles)

            final HousingStrategy housingStrategy = new SimpleCommuteModeChoiceHousingStrategyImpl( dataContainer,
                    properties,
                    dataContainer.getTravelTimes(),
                    dwellingUtilityStrategy,
                    dwellingProbabilityStrategy,
                    regionUtilityStrategy,
                    regionProbabilityStrategy,
                    commuteModeChoice1
            );
            /// (If I see this right, this is computing the necessary inputs to {@link DwellingUtilityStrategy} using all the other dependencies, and then computing the dwelling
            /// probabilities using the {@link DwellingUtilityStrategy}.

            final MovesStrategy movesStrategy = new DefaultMovesStrategy();
            // (This says that the moving proba is  1 - 1/(1+0.03 * Math.exp(10*(householdSatisfaction - currentDwellingUtility)))

            movesModel = new MovesModelImpl( dataContainer, properties, movesStrategy, housingStrategy, SiloUtil.provideNewRandom() );
        }
        // (Overall, I think that life events are not explicitly triggering a move.  They will, however, shift the respective utilities, and so a move becomes more probable.)

        // (For our own issue, which (only) is to "age" the population, this seems like a lot of overhead.  On the other hand, it is not immediately clear how else this should be
        // resolved; location choice needs to be something like logit based on location utility.  What might end up being a bit of a problem is (commute) mode choice ... we would
        // need to say that maybe the commute mode choice to make a residence decision is based on what is defined above, but the final mode choice may be different once people
        // optimize into their new environment. kai, apr'26)

        // (Also, the MatsimScenarioAssembler takes the commute mode from the Silo mode choice model (although I am not sure that it will give the same results are for housing
        // choice, since the random numbers are different--????).  Evidently, we can override this in MATSim.  Clearly, this will be two models pull into different directions.
        // Unfortunately, I fail to see how we calibrade mode choice on the MATSim side if we rely on upstream mode choice.  Maybe possible in principle, but will not be sensitive
        // to (the details of) many transport policies.  kai, apr'26)

        // (As we have known for some time now, this is two competing modelling paradigms: mode (and time) choice as part of the DTA vs mode (and time?) choice as part of the
        // upstream model.  Rolf seems to have been a proponent of the former.  However, from a MATSim side and MATSim research side, this is not something we can continue since we
        // have experience with mode choice in MATSim but not in upstream models, and this is also a path that we do not want to take (would need to be done by someone else).
        // We should consider to re-code the commute mode choice for our purposes, i.e. always allow for car and pt during housing search, using income-dependent utilities and
        // fixed costs for cars (and pt), etc., and then sort out the actual choice later.  However, this may end up not so different from the current approach, so why not leave
        // the current approach in place, but change as follows: Only if a silo person has moved residency or job, then we use the mode choice from silo as an initial suggestion;
        // otherwise we keep the plan (including mode) from previous iterations. -- Such an approach would make a lot of sense anyways; it was already encoded as "hot start" in
        // both matsim-urbansim couplings (by KN and by TN); and it would be far enough downstream of SILO, i.e. only in the adapter class. --  A bit disappointing that 20 year
        // later we are not any further here. :-(   kai, apr'26)

        CreateCarOwnershipModel carOwnershipModel = new FabilandCarOwnership();

        DivorceModel divorceModel = new DivorceModelImpl( dataContainer, movesModel, carOwnershipModel, hhFactory, properties, new DefaultDivorceStrategy(), SiloUtil.provideNewRandom());

        DriversLicenseModel driversLicenseModel = new DriversLicenseModelImpl(dataContainer, properties, new DefaultDriversLicenseStrategy(), SiloUtil.provideNewRandom());

        EducationModel educationModel = new EducationModelImpl(dataContainer, properties, SiloUtil.provideNewRandom());

        EmploymentModel employmentModel = new EmploymentModelImpl(dataContainer, properties, SiloUtil.provideNewRandom());

        LeaveParentHhModel leaveParentsModel = new LeaveParentHhModelImpl(dataContainer, movesModel,
                carOwnershipModel, hhFactory, properties, new DefaultLeaveParentalHouseholdStrategy(), SiloUtil.provideNewRandom());

        JobMarketUpdate jobMarketUpdateModel = new JobMarketUpdateImpl(dataContainer, properties, SiloUtil.provideNewRandom());

        ConstructionModel construction = new ConstructionModelImpl(dataContainer, ddFactory,
                properties, new FabilandConstructionLocationStrategy(), new DefaultConstructionDemandStrategy(), SiloUtil.provideNewRandom());

        PricingModel pricing = new PricingModelImpl(dataContainer, properties, new DefaultPricingStrategy(), SiloUtil.provideNewRandom());

        RenovationModel renovation = new RenovationModelImpl(dataContainer, properties, new DefaultRenovationStrategy(), SiloUtil.provideNewRandom());

        ConstructionOverwrite constructionOverwrite = new ConstructionOverwriteImpl(dataContainer, ddFactory, properties, SiloUtil.provideNewRandom());

        InOutMigration inOutMigration = new InOutMigrationImpl(dataContainer, employmentModel, movesModel,
                carOwnershipModel, driversLicenseModel, properties, SiloUtil.provideNewRandom());

        DemolitionModel demolition = new DemolitionModelImpl(dataContainer, movesModel,
                inOutMigration, properties, new DefaultDemolitionStrategy(), SiloUtil.provideNewRandom());

        MarriageModel marriageModel = new MarriageModelImpl(dataContainer, movesModel, inOutMigration,
                carOwnershipModel, hhFactory, properties, new DefaultMarriageStrategy(), SiloUtil.provideNewRandom());

        TransportModel transportModel;
        MatsimScenarioAssembler scenarioAssembler;

        MatsimData matsimData = null;
        if (config != null) {
            final Scenario scenario = ScenarioUtils.loadScenario(config);
//			matsimData = new MatsimData(config, properties, ZoneConnectorManagerImpl.ZoneConnectorMethod.WEIGHTED_BY_POPULATION, dataContainer, scenario.getNetwork(), scenario.getTransitSchedule());
			matsimData = new MatsimData(properties, ZoneConnectorMethod.WEIGHTED_BY_POPULATION, dataContainer, (MutableScenario) scenario );
			// yyyyyy the scenario is now generated in the MatsimData class, and in consequence the above constructor could/should be removed again
        }
        switch (properties.transportModel.transportModelIdentifier) {
            case MATSIM:
//                SimpleCommuteModeChoice commuteModeChoice = new SimpleCommuteModeChoice(dataContainer, properties, SiloUtil.provideNewRandom());
                SimpleMatsimCommuteModeChoice commuteModeChoice = new SimpleMatsimCommuteModeChoice(dataContainer, properties, SiloUtil.provideNewRandom());
				// (yyyy is also instantiated above.  why not re-use?)
                // --> fails the regression test.  Which is no wonder, since it changes the random number sequence.

                scenarioAssembler = new SimpleCommuteModeChoiceMatsimScenarioAssembler(dataContainer, properties, commuteModeChoice, HandlingOfRandomness.localInstanceFromMatsimWithAlwaysSameSeed);
                // yyyyyy the above needs to be re-coded from a MATSim perspective ... i.e. (1) select the same agents, and (2) take the mode "suggestion" from silo only if home or
                // job location has changed, otherwise keep existing plan with existing mode.  kai, apr'26

                transportModel = new MatsimTransportModel(dataContainer, config, properties, scenarioAssembler, matsimData);
                break;
            case NONE:
            default:
                transportModel = null;
        }

        final ModelContainer modelContainer = new ModelContainer(
                birthModel, birthdayModel,
                deathModel, marriageModel,
                divorceModel, driversLicenseModel,
                educationModel, employmentModel,
                leaveParentsModel, jobMarketUpdateModel,
                construction, demolition, pricing, renovation,
                constructionOverwrite, inOutMigration, movesModel, transportModel);
        return modelContainer;
    }

    private static class FabilandCarOwnership implements CreateCarOwnershipModel {

        private final Random random;

        FabilandCarOwnership() {
            this.random = SiloUtil.provideNewRandom();
        }

        @Override
        public void run() {

        }

        @Override
        public void simulateCarOwnership(Household hh) {
            hh.setAutos(random.nextInt(3)+1);
        }
    }
}
