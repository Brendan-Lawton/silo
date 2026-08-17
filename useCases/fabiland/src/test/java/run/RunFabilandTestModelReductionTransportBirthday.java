package run;

import de.tum.bgu.msm.SiloModel;
import de.tum.bgu.msm.container.DataContainer;
import de.tum.bgu.msm.container.ModelContainer;
import de.tum.bgu.msm.data.SummarizeData;
import de.tum.bgu.msm.data.dwelling.DwellingFactory;
import de.tum.bgu.msm.data.household.Household;
import de.tum.bgu.msm.data.household.HouseholdFactory;
import de.tum.bgu.msm.data.person.Person;
import de.tum.bgu.msm.data.person.PersonFactory;
import de.tum.bgu.msm.events.impls.household.MoveEvent;
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
import de.tum.bgu.msm.models.realEstate.construction.ConstructionOverwrite;
import de.tum.bgu.msm.models.realEstate.construction.ConstructionOverwriteImpl;
import de.tum.bgu.msm.models.realEstate.pricing.DefaultPricingStrategy;
import de.tum.bgu.msm.models.realEstate.pricing.PricingModel;
import de.tum.bgu.msm.models.realEstate.pricing.PricingModelImpl;
import de.tum.bgu.msm.models.relocation.migration.InOutMigration;
import de.tum.bgu.msm.models.relocation.migration.InOutMigrationImpl;
import de.tum.bgu.msm.models.relocation.moves.MovesModel;
import de.tum.bgu.msm.models.transportModel.TransportModel;
import de.tum.bgu.msm.properties.Properties;
import de.tum.bgu.msm.properties.PropertiesUtil;
import de.tum.bgu.msm.utils.SiloUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.RoutingConfigGroup;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.testcases.MatsimTestUtils;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

//@ExtendWith(MatsimTestUtils.class)
public class RunFabilandTestModelReductionTransportBirthday {
	private static final Logger log = LogManager.getLogger( RunFabilandTestModelReductionTransportBirthday.class );


	@RegisterExtension
	public MatsimTestUtils utils = new MatsimTestUtils();

    @Before
    public void setup() {

    }

	@Test
	public void testMain() {

		/* BASE TEST: This test runs with models.
		Moves model has already been overwritten by Kai in this base case.   */

        final String inputDirectory = utils.getInputDirectory();

        Properties properties = null;
        try {
            String[] args = {"./scenario/test2.properties",
                    "./scenario/config_cap30_1-l_nes_smc.xml",
//					"--config:controler.outputDirectory", utils.getOutputDirectory(), // has no effect; evidently overwritten by code
                    "--config:controler.lastIteration", "1"
            };

            // see regression test

            // args: SILO config, MATSim config
            // e.g., "useCases/fabiland/scenario/1r_ae.properties useCases/fabiland/scenario/config_cap30_1-l_nes_smc.xml"
            // or, to match regression test ...
            // "useCases/fabiland/scenario/test.properties useCases/fabiland/scenario/config_cap30_1-l_nes_smc.xml --config:controller.lastIteration 1 "

            properties = Properties.initializeProperties(args[0]);
            properties.main.scenarioName = "OnlyTransportBirthday";
            final String outputDirectory = properties.main.baseDirectory + "scenOutput/" + properties.main.scenarioName;
            SiloUtil.createDirectoryIfNotExistingYet(outputDirectory);

            SummarizeData.resultFileSpatial("open");
            SummarizeData.resultFileSpatial_2("open");
            PropertiesUtil.writePropertiesForThisRun(args[0]);

            SiloUtil.initializeRandomNumber(properties.main.randomSeed);
            SiloUtil.trackingFile("open");
            SiloUtil.loadHdf5Lib();
            Properties siloConfig = properties;

            String[] matsimArgs = Arrays.copyOfRange(args, 1, args.length);

            Config matsimConfig = null;
//        if (args.length > 1 && args[1] != null) {
            matsimConfig = ConfigUtils.loadConfig(matsimArgs);
//        }
//			RunFabiland.logger.info("Started SILO Fabiland sandbox model");

            // The following is obviously just a dirty quickfix until access/egress is default in MATSim
            if (siloConfig.transportModel.includeAccessEgress) {
                ////            config.plansCalcRoute().setInsertingAccessEgressWalk(true); // in matsim-12
                matsimConfig.routing().setAccessEgressType(RoutingConfigGroup.AccessEgressType.accessEgressModeToLink); // in matsim-13-w37
            }
//		config.routing().setAccessEgressType( RoutingConfigGroup.AccessEgressType.none );
            // yyyyyy Silo uses a re-implementation of a lot of matsim infrastructure, and that is outside injection.  The more advanced access/egress types are not implemented there.
            // kai, apr'26

            matsimConfig.controller().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

            DataContainer dataContainer = DataBuilderFabiland.buildDataContainer(siloConfig, matsimConfig);
            DataBuilderFabiland.readInput(siloConfig, dataContainer);

            final PersonFactory ppFactory = dataContainer.getHouseholdDataManager().getPersonFactory();
            final HouseholdFactory hhFactory = dataContainer.getHouseholdDataManager().getHouseholdFactory();
            final DwellingFactory ddFactory = dataContainer.getRealEstateDataManager().getDwellingFactory();
            // (in most if not all cases, the dataContainer is handed over anyways.  --> add separate constructor w/o those factories;
            // set old constructor to deprecated (but do not make effort to remove).

            final BirthModel birthModel = new BirthModelImpl(dataContainer, ppFactory, siloConfig, new DefaultBirthStrategy(), SiloUtil.provideNewRandom());

            final BirthdayModel birthdayModel = new BirthdayModelImpl(dataContainer, siloConfig, SiloUtil.provideNewRandom());

            final DeathModel deathModel = new DeathModelImpl(dataContainer, siloConfig, new DefaultDeathStrategy(), SiloUtil.provideNewRandom());

            final MovesModel movesModel = new MovesModel() {
                @Override
                public int searchForNewDwelling(Household household) {
                    return -1; // means no dwelling was found
                }

                @Override
                public void moveHousehold(Household hh, int idOldDD, int idNewDD) {
                    // do nothing
                }

                @Override
                public Collection<MoveEvent> getEventsForCurrentYear(int year) {
                    return Collections.emptyList();
                }

                @Override
                public boolean handleEvent(MoveEvent event) {
                    // this should not happen.  Maybe test?
                    // If this is needed, one cold use MovesModelImpl as a delegate and then go from there.
                    return false;
                }

                @Override
                public void setup() {
                    // do nothing
                }

                @Override
                public void prepareYear(int year) {
                    // do nothing
                }

                @Override
                public void endYear(int year) {
                    // do nothing
                }

                @Override
                public void endSimulation() {
                    // do nothing
                }
            };

            final CreateCarOwnershipModel carOwnershipModel = new ModelBuilderFabilandSimplified.FabilandCarOwnership();
            // yy (for VSP purposes, car ownership could  be done by matsim)

            final DivorceModel divorceModel = new DivorceModelImpl(
                    dataContainer, movesModel, carOwnershipModel, hhFactory,
                    siloConfig, new DefaultDivorceStrategy(), SiloUtil.provideNewRandom());

            final DriversLicenseModel driversLicenseModel = new DriversLicenseModelImpl(dataContainer, siloConfig, new DefaultDriversLicenseStrategy(), SiloUtil.provideNewRandom());
            // yy (for VSP purposes, this might not be needed)

            final EducationModel educationModel = new EducationModelImpl(dataContainer, siloConfig, SiloUtil.provideNewRandom());

            EmploymentModel employmentModel = new EmploymentModelImpl(dataContainer, siloConfig, SiloUtil.provideNewRandom()) {
                @Override
                public boolean lookForJob(int perId) {
                    return false;   // stub this one
                }
            };

            final LeaveParentHhModel leaveParentsModel = new LeaveParentHhModelImpl(dataContainer, movesModel,
                    carOwnershipModel, hhFactory, siloConfig, new DefaultLeaveParentalHouseholdStrategy(), SiloUtil.provideNewRandom());

            final JobMarketUpdate jobMarketUpdateModel = new JobMarketUpdateImpl(dataContainer, siloConfig, SiloUtil.provideNewRandom());

            final PricingModel pricing = new PricingModelImpl(dataContainer, siloConfig, new DefaultPricingStrategy(), SiloUtil.provideNewRandom());

            final ConstructionOverwrite constructionOverwrite = new ConstructionOverwriteImpl(dataContainer, ddFactory, siloConfig, SiloUtil.provideNewRandom());

            final InOutMigration inOutMigration = new InOutMigrationImpl(dataContainer, employmentModel, movesModel,
                    carOwnershipModel, driversLicenseModel, siloConfig, SiloUtil.provideNewRandom());
            // (do we need this at VSP?)
            // (if we need it, the car ownership model can be null.  Presumably, also the drivers licence model.)

            final MarriageModel marriageModel = new MarriageModelImpl(dataContainer, movesModel, inOutMigration,
                    carOwnershipModel, hhFactory, siloConfig, new DefaultMarriageStrategy(), SiloUtil.provideNewRandom()) {
                @Override
                protected boolean moveTogether(Person person1, Person person2, Household moveTo) {
                    return true;
                }

                ;
            };
            // (do we need this at VSP?  We could also women have children.)

            TransportModel transportModel;
            MatsimScenarioAssembler scenarioAssembler;

            MatsimData matsimData = null;
            if (matsimConfig != null) {
                final Scenario scenario = ScenarioUtils.loadScenario(matsimConfig);
                //  seems to be breaking
                //            matsimData = new MatsimData(config, properties, ZoneConnectorMethod.WEIGHTED_BY_POPULATION, dataContainer, scenario.getNetwork(), scenario.getTransitSchedule());
                // (only the constructor is deprecated)

                // old version
                matsimData = new MatsimData(siloConfig, ZoneConnectorManager.ZoneConnectorMethod.WEIGHTED_BY_POPULATION, dataContainer, (MutableScenario) scenario);

            }
            switch (siloConfig.transportModel.transportModelIdentifier) {
                case MATSIM:
                    //                SimpleCommuteModeChoice commuteModeChoice = new SimpleCommuteModeChoice(dataContainer, properties, SiloUtil.provideNewRandom());
                    SimpleMatsimCommuteModeChoice commuteModeChoice = new SimpleMatsimCommuteModeChoice(dataContainer, siloConfig, SiloUtil.provideNewRandom());
                    // (for VSP purposes, this might not be needed)

                    scenarioAssembler = new SimpleCommuteModeChoiceMatsimScenarioAssembler(dataContainer, siloConfig, commuteModeChoice, SimpleCommuteModeChoiceMatsimScenarioAssembler.HandlingOfRandomness.localInstanceFromMatsimWithAlwaysSameSeed);
                    transportModel = new MatsimTransportModel(dataContainer, matsimConfig, siloConfig, scenarioAssembler, matsimData);
                    break;
                case NONE:
                default:
                    transportModel = null;
            }

            final ModelContainer modelContainer = new ModelContainer(
                    null, birthdayModel,
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null, null, null, null,
                    null, null, null, transportModel);

            SiloModel model = new SiloModel(siloConfig, dataContainer, modelContainer);
//			model.addResultMonitor( new DefaultResultsMonitor(dataContainer, siloConfig) );
//			model.addResultMonitor( new MultiFileResultsMonitor(dataContainer, siloConfig) );
//			model.addResultMonitor( new HouseholdSatisfactionMonitor(dataContainer, siloConfig, modelContainer) );

            model.runModel();

//			{
//				Population expected = PopulationUtils.createPopulation( ConfigUtils.createConfig() ) ;
//				PopulationUtils.readPopulation( expected,  utils.getInputDirectory() + "10.output_plans.xml.gz" );
//
//				Population actual = PopulationUtils.createPopulation( ConfigUtils.createConfig() ) ;
//				PopulationUtils.readPopulation( actual, "scenario/scenOutput/base/matsim/10/10.output_plans.xml.gz" );
//
//				boolean result = PopulationUtils.comparePopulations( expected, actual );
//				Assert.assertTrue( result );
//			}
//			{
//				String expected = utils.getInputDirectory() + "/10.output_events.xml.gz" ;
//				String actual = "scenario/scenOutput/base/matsim/10/10.output_events.xml.gz" ;
//				EventsFileComparator.Result result = EventsUtils.compareEventsFiles( expected, actual );
//				Assert.assertEquals( EventsFileComparator.Result.FILES_ARE_EQUAL, result );
//			}

        } catch (Exception ee) {
            log.fatal("there was an exception: \n" + ee);

            ee.printStackTrace();

            // if one catches an exception, then one needs to explicitly fail the test:
            Assertions.fail();
        }

        // directory for population files:
        String popfiles = "/home/brendan/git/silo/useCases/fabiland/scenario/scenOutput/" + properties.main.scenarioName + "/microData/";

        log.info("############################################");
        log.info("############################################");
        {
            Table ppStart = Table.read().csv(popfiles + "pp_0.csv");
            Table ppEnd = Table.read().csv(popfiles + "pp_10.csv");

            Table joined = ppStart.joinOn("id").fullOuter(ppEnd, true, false,"id");

            log.info("Joined columns: " + joined.columnNames());

            String ageStartCol = "age";
            String ageEndCol = "T2.age";

            // people born = present in pp_10 but not pp_0 -> age is missing
            Table born = joined.where(joined.column(ageStartCol).isMissing());
            long numBorn = born.rowCount();
            log.info("Number born: " + numBorn);

            // people who died = present in pp_0 but not pp_10 -> T2.age is missing
            Table died = joined.where(joined.column(ageEndCol).isMissing());
            long numDied = died.rowCount();
            log.info("Number died: " + numDied);

            // people present in both
            Table normal = joined.dropWhere(
                    joined.column(ageStartCol).isMissing()
                            .or(joined.column(ageEndCol).isMissing())
            );

            DoubleColumn ageStart = normal.numberColumn(ageStartCol).asDoubleColumn();
            DoubleColumn ageEnd = normal.numberColumn(ageEndCol).asDoubleColumn();
            DoubleColumn ageDiff = ageEnd.subtract(ageStart).setName("age_diff");
            normal.addColumns(ageDiff);

            double[] uniqueDiffs = ageDiff.unique().asDoubleArray();

            Assertions.assertEquals(1, uniqueDiffs.length,
                    "Age difference should be identical for every person, but found: " + Arrays.toString(uniqueDiffs));
            Assertions.assertEquals(9.0, uniqueDiffs[0], 1e-6,
                    "Expected age difference of 9 years between pp_0 and pp_10");

            // ---- 3. Household change (moved) ----
            StringColumn hhChanged = StringColumn.create("household_changed", normal.rowCount());

            for (Row row : normal) {
                Integer hhx = row.getInt("hhid");
                Integer hhy = row.getInt("T2.hhid");

                String category = !hhx.equals(hhy) ? "moved household" : "stayed put";
                hhChanged.set(row.getRowNumber(), category);
            }
            normal.addColumns(hhChanged);

            Table hhChangeCounts = hhChanged.countByCategory();
            Table stayedPutRow = hhChangeCounts.where(
                    hhChangeCounts.stringColumn("Category").isEqualTo("stayed put")
            );

            int stayedPutCount = stayedPutRow.intColumn("Count").get(0);
            log.info("Stayed put count: " + stayedPutCount);
            Assertions.assertEquals(normal.rowCount(), stayedPutCount);
            log.info("Household change breakdown:\n" + hhChangeCounts.print());
        }

//        ADD MARRIAGE / DIVORCE & GetJob / ChangeJob


        log.info("############################################");
        log.info("############################################");
        log.info("Age difference corresponds to number of years.");
        log.info("############################################");
        log.info("############################################");

        log.info("############################################");
        log.info("############################################");
        log.info("Test without:");
        log.info("Construction, Renovation, Demolition, ConstructionOverwrite, JobMarketUpdate");
        log.info("Employment, DriversLicense, LeaveParents, Divorce, Marriage, Education");
        log.info("ran successfully.");
        log.info("############################################");
        log.info("############################################");

    }
}
