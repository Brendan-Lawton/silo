package run;

import de.tum.bgu.msm.SiloModel;
import de.tum.bgu.msm.container.DataContainer;
import de.tum.bgu.msm.container.ModelContainer;
import de.tum.bgu.msm.data.dwelling.DwellingFactory;
import de.tum.bgu.msm.data.household.Household;
import de.tum.bgu.msm.data.household.HouseholdFactory;
import de.tum.bgu.msm.data.person.Person;
import de.tum.bgu.msm.data.person.PersonFactory;
import de.tum.bgu.msm.events.impls.household.MoveEvent;
import de.tum.bgu.msm.io.output.DefaultResultsMonitor;
import de.tum.bgu.msm.io.output.HouseholdSatisfactionMonitor;
import de.tum.bgu.msm.io.output.MultiFileResultsMonitor;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Random;

//@ExtendWith(MatsimTestUtils.class)
public class RunFabilandModelCombinationTests {

    //region Define global variables
    private static final Logger log = LogManager.getLogger(RunFabilandModelCombinationTests.class);
    private static MovesModel movesModel;
    private static BirthModel birthModel;
    private static PersonFactory ppFactory;
    private static HouseholdFactory hhFactory;
    private static DwellingFactory ddFactory;
    private static BirthdayModel birthdayModel;
    private static DeathModel deathModel;
    private static EducationModel educationModel;
    private static CreateCarOwnershipModel carOwnershipModel;
    private static DivorceModel divorceModel;
    private static DriversLicenseModel driversLicenseModel;
    private static LeaveParentHhModel leaveParentsModel;
    private static JobMarketUpdate jobMarketUpdateModel;
    private static EmploymentModel employmentModel;
    private static MarriageModel marriageModel;
    private static PricingModel pricingModel;
    private static TransportModel transportModel;
    private static ConstructionOverwrite constructionOverwrite;
    private static InOutMigration inOutMigration;
    private static ConstructionModel constructionModel;
    private static DemolitionModel demolitionModel;
    private static RenovationModel renovationModel;

    private static DataContainer dataContainer = null;
    private static Properties siloConfig = null;
    private static Config matsimConfig = null;
    //endregion

    @RegisterExtension
	public MatsimTestUtils utils = new MatsimTestUtils();
    private String popfiles;

    @BeforeEach
    public void setup() {

        String[] args = {"./scenario/test2.properties",
                "./scenario/config_cap30_1-l_nes_smc.xml",
//					"--config:controler.outputDirectory", utils.getOutputDirectory(), // has no effect; evidently overwritten by code
                "--config:controler.lastIteration", "1"
        };

        siloConfig = SiloUtil.siloInitialization(args[0]);
        popfiles = "/Users/jakob/git/silo-BL/useCases/fabiland/scenario/scenOutput/" + siloConfig.main.scenarioName + "/microData/";

        String[] matsimArgs = Arrays.copyOfRange( args, 1, args.length );

        matsimConfig = ConfigUtils.loadConfig(matsimArgs);

        // The following is obviously just a dirty quickfix until access/egress is default in MATSim
        if (siloConfig.transportModel.includeAccessEgress) {
////            config.plansCalcRoute().setInsertingAccessEgressWalk(true); // in matsim-12
            matsimConfig.routing().setAccessEgressType(RoutingConfigGroup.AccessEgressType.accessEgressModeToLink); // in matsim-13-w37
        }

        matsimConfig.controller().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

        dataContainer = DataBuilderFabiland.buildDataContainer(siloConfig, matsimConfig);
        DataBuilderFabiland.readInput(siloConfig, dataContainer);

        setupModels(dataContainer, siloConfig, matsimConfig);
    }

	@Test
	public void testAllModels() {

        /* BASE TEST: This test runs with models.
        Moves model has already been overwritten by Kai in this base case.   */

    ModelContainer modelContainer = new ModelContainer(
            birthModel, birthdayModel,
            deathModel, marriageModel,
            divorceModel, driversLicenseModel,
            educationModel, employmentModel,
            leaveParentsModel, jobMarketUpdateModel,
            constructionModel, demolitionModel, pricingModel, renovationModel,
            constructionOverwrite, inOutMigration, movesModel, transportModel);


//        ModelContainer modelContainer = ModelBuilderFabiland.getModelContainer(dataContainer, siloConfig, matsimConfig);
        SiloModel model = new SiloModel(siloConfig, dataContainer, modelContainer);
        model.addResultMonitor( new DefaultResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new MultiFileResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new HouseholdSatisfactionMonitor(dataContainer, siloConfig, modelContainer) );

        model.runModel();


    // directory for population files

    log.info("############################################");
    log.info("############################################");
    {
        Table ppStart = Table.read().csv(popfiles + "pp_0.csv");
        Table ppEnd = Table.read().csv(popfiles + "pp_10.csv");

        // full outer join on "id" — Tablesaw will rename colliding columns from the
        // right-hand table (check joined.columnNames() to confirm the actual suffix,
        // it's typically something like "age_2" but can vary by version)
        Table joined = ppStart.joinOn("id").fullOuter(ppEnd, true, false, "id");

        log.info("Joined columns: " + joined.columnNames());

        String ageStartCol = "age";
        String ageEndCol = "T2.age"; // <-- verify this against the logged column names above

        // people born = present in pp_10 but not pp_0 -> age.x is missing
        Table born = joined.where(joined.column(ageStartCol).isMissing());
        long numBorn = born.rowCount();
        Assertions.assertNotEquals(0, numBorn);
        log.info("Number born: " + numBorn);

        // people who died = present in pp_0 but not pp_10 -> age.y is missing
        Table died = joined.where(joined.column(ageEndCol).isMissing());
        long numDied = died.rowCount();
        Assertions.assertNotEquals(0, numDied);

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
        Assertions.assertNotEquals(normal.rowCount(), stayedPutCount);
        log.info("Household change breakdown:\n" + hhChangeCounts.print());
    }
    }

    @Test
    public void testModelsLarge() {

        /* BASE TEST: This test runs without
         construction/constructionOverwrite/demolition/renovation models */

        ModelContainer modelContainer = new ModelContainer(
                birthModel, birthdayModel,
                deathModel, marriageModel,
                divorceModel, driversLicenseModel,
                educationModel, employmentModel,
                leaveParentsModel, jobMarketUpdateModel,
                null, null, pricingModel, null,
                null, inOutMigration, movesModel, transportModel);


//        ModelContainer modelContainer = ModelBuilderFabiland.getModelContainer(dataContainer, siloConfig, matsimConfig);
        SiloModel model = new SiloModel(siloConfig, dataContainer, modelContainer);
        model.addResultMonitor( new DefaultResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new MultiFileResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new HouseholdSatisfactionMonitor(dataContainer, siloConfig, modelContainer) );

        model.runModel();


        // directory for population files:

        log.info("############################################");
        log.info("############################################");
        {
            Table ppStart = Table.read().csv(popfiles + "pp_0.csv");
            Table ppEnd = Table.read().csv(popfiles + "pp_10.csv");

            // full outer join on "id" — Tablesaw will rename colliding columns from the
            // right-hand table (check joined.columnNames() to confirm the actual suffix,
            // it's typically something like "age_2" but can vary by version)
            Table joined = ppStart.joinOn("id").fullOuter(ppEnd, true, false, "id");

            log.info("Joined columns: " + joined.columnNames());

            String ageStartCol = "age";
            String ageEndCol = "T2.age"; // <-- verify this against the logged column names above

            // people born = present in pp_10 but not pp_0 -> age.x is missing
            Table born = joined.where(joined.column(ageStartCol).isMissing());
            long numBorn = born.rowCount();
            Assertions.assertNotEquals(0, numBorn);
            log.info("Number born: " + numBorn);

            // people who died = present in pp_0 but not pp_10 -> age.y is missing
            Table died = joined.where(joined.column(ageEndCol).isMissing());
            long numDied = died.rowCount();
            Assertions.assertNotEquals(0, numDied);

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
            Assertions.assertNotEquals(normal.rowCount(), stayedPutCount);
            log.info("Household change breakdown:\n" + hhChangeCounts.print());
        }
    }

    @Test
    public void testModelsMedium() {

        /* BASE TEST: This test runs without
         construction/constructionOverwrite/demolition/renovation
         /driversLicense/education/employment/jobMarket models */

        ModelContainer modelContainer = new ModelContainer(
                birthModel, birthdayModel,
                deathModel, marriageModel,
                divorceModel, null,
                null, null,
                leaveParentsModel, null,
                null, null, pricingModel, null,
                null, inOutMigration, movesModel, transportModel);


//        ModelContainer modelContainer = ModelBuilderFabiland.getModelContainer(dataContainer, siloConfig, matsimConfig);
        SiloModel model = new SiloModel(siloConfig, dataContainer, modelContainer);
        model.addResultMonitor( new DefaultResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new MultiFileResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new HouseholdSatisfactionMonitor(dataContainer, siloConfig, modelContainer) );

        model.runModel();


        // directory for population files:

        log.info("############################################");
        log.info("############################################");
        {
            Table ppStart = Table.read().csv(popfiles + "pp_0.csv");
            Table ppEnd = Table.read().csv(popfiles + "pp_10.csv");

            // full outer join on "id" — Tablesaw will rename colliding columns from the
            // right-hand table (check joined.columnNames() to confirm the actual suffix,
            // it's typically something like "age_2" but can vary by version)
            Table joined = ppStart.joinOn("id").fullOuter(ppEnd, true, false, "id");

            log.info("Joined columns: " + joined.columnNames());

            String ageStartCol = "age";
            String ageEndCol = "T2.age"; // <-- verify this against the logged column names above

            // people born = present in pp_10 but not pp_0 -> age.x is missing
            Table born = joined.where(joined.column(ageStartCol).isMissing());
            long numBorn = born.rowCount();
            Assertions.assertNotEquals(0, numBorn);
            log.info("Number born: " + numBorn);

            // people who died = present in pp_0 but not pp_10 -> age.y is missing
            Table died = joined.where(joined.column(ageEndCol).isMissing());
            long numDied = died.rowCount();
            Assertions.assertNotEquals(0, numDied);

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
            Assertions.assertNotEquals(normal.rowCount(), stayedPutCount);
            log.info("Household change breakdown:\n" + hhChangeCounts.print());
        }
    }

    @Test
    public void testModelsSmall() {

        /* BASE TEST: This test runs without
         construction/constructionOverwrite/demolition/renovation
         /driversLicense/education/employment/jobMarket models */

        ModelContainer modelContainer = new ModelContainer(
                birthModel, birthdayModel,
                deathModel, null,
                null, null,
                null, null,
                null, null,
                null, null, pricingModel, null,
                null, inOutMigration, movesModel, transportModel);


//        ModelContainer modelContainer = ModelBuilderFabiland.getModelContainer(dataContainer, siloConfig, matsimConfig);
        SiloModel model = new SiloModel(siloConfig, dataContainer, modelContainer);
        model.addResultMonitor( new DefaultResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new MultiFileResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new HouseholdSatisfactionMonitor(dataContainer, siloConfig, modelContainer) );

        model.runModel();


        // directory for population files:

        log.info("############################################");
        log.info("############################################");
        {
            Table ppStart = Table.read().csv(popfiles + "pp_0.csv");
            Table ppEnd = Table.read().csv(popfiles + "pp_10.csv");

            // full outer join on "id" — Tablesaw will rename colliding columns from the
            // right-hand table (check joined.columnNames() to confirm the actual suffix,
            // it's typically something like "age_2" but can vary by version)
            Table joined = ppStart.joinOn("id").fullOuter(ppEnd, true, false, "id");

            log.info("Joined columns: " + joined.columnNames());

            String ageStartCol = "age";
            String ageEndCol = "T2.age"; // <-- verify this against the logged column names above

            // people born = present in pp_10 but not pp_0 -> age.x is missing
            Table born = joined.where(joined.column(ageStartCol).isMissing());
            long numBorn = born.rowCount();
            Assertions.assertNotEquals(0, numBorn);
            log.info("Number born: " + numBorn);

            // people who died = present in pp_0 but not pp_10 -> age.y is missing
            Table died = joined.where(joined.column(ageEndCol).isMissing());
            long numDied = died.rowCount();
            Assertions.assertNotEquals(0, numDied);

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
    }

    @Test
    public void testModelsOnlyBirths() {

        /* BASE TEST: This test runs with birth and transport models */

        ModelContainer modelContainer = new ModelContainer(
                null, birthdayModel,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null, null, null,
                null, null, movesModel, transportModel);


//        ModelContainer modelContainer = ModelBuilderFabiland.getModelContainer(dataContainer, siloConfig, matsimConfig);
        SiloModel model = new SiloModel(siloConfig, dataContainer, modelContainer);
        model.addResultMonitor( new DefaultResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new MultiFileResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new HouseholdSatisfactionMonitor(dataContainer, siloConfig, modelContainer) );

        model.runModel();


        // directory for population files:

        log.info("############################################");
        log.info("############################################");
        {
            Table ppStart = Table.read().csv(popfiles + "pp_0.csv");
            Table ppEnd = Table.read().csv(popfiles + "pp_10.csv");

            // full outer join on "id" — Tablesaw will rename colliding columns from the
            // right-hand table (check joined.columnNames() to confirm the actual suffix,
            // it's typically something like "age_2" but can vary by version)
            Table joined = ppStart.joinOn("id").fullOuter(ppEnd, true, false, "id");

            log.info("Joined columns: " + joined.columnNames());

            String ageStartCol = "age";
            String ageEndCol = "T2.age"; // <-- verify this against the logged column names above

            // people born = present in pp_10 but not pp_0 -> age.x is missing
            Table born = joined.where(joined.column(ageStartCol).isMissing());
            long numBorn = born.rowCount();
            Assertions.assertEquals(0, numBorn);
            log.info("Number born: " + numBorn);

            // people who died = present in pp_0 but not pp_10 -> age.y is missing
            Table died = joined.where(joined.column(ageEndCol).isMissing());
            long numDied = died.rowCount();
            Assertions.assertEquals(0, numDied);

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
    }

    @Test
    public void testModelsKai() {

        /* BASE TEST: This test runs with
         birth, birthday, death, marriage (with no moving),
         driversLicense, education,
         employment models */

        marriageModel = new MarriageModelImpl(dataContainer, movesModel, inOutMigration,
                carOwnershipModel, hhFactory, siloConfig, new DefaultMarriageStrategy(), SiloUtil.provideNewRandom()) {
            @Override
            protected boolean moveTogether(Person person1, Person person2, Household moveTo) {
                return true;
            }
        };

        // breaks if we take moves out completely
        movesModel = new MovesModel(){
            @Override public int searchForNewDwelling( Household household ){
                return -1; // means no dwelling was found
            }
            @Override public void moveHousehold( Household hh, int idOldDD, int idNewDD ){
                // do nothing
            }
            @Override public Collection<MoveEvent> getEventsForCurrentYear(int year ){
                return Collections.emptyList();
            }
            @Override public boolean handleEvent( MoveEvent event ){
                // this should not happen.  Maybe test?
                // If this is needed, one cold use MovesModelImpl as a delegate and then go from there.
                return false;
            }
            @Override public void setup(){
                // do nothing
            }
            @Override public void prepareYear( int year ){
                // do nothing
            }
            @Override public void endYear( int year ){
                // do nothing
            }
            @Override public void endSimulation(){
                // do nothing
            }
        };

        ModelContainer modelContainer = new ModelContainer(
                birthModel, birthdayModel,
                deathModel, marriageModel,
                null, driversLicenseModel,
                educationModel, employmentModel,
                null, null,
                null, null, null, null,
                null, null, movesModel, transportModel);

//        ModelContainer modelContainer = ModelBuilderFabiland.getModelContainer(dataContainer, siloConfig, matsimConfig);
        SiloModel model = new SiloModel(siloConfig, dataContainer, modelContainer);
        model.addResultMonitor( new DefaultResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new MultiFileResultsMonitor(dataContainer, siloConfig) );
        model.addResultMonitor( new HouseholdSatisfactionMonitor(dataContainer, siloConfig, modelContainer) );

        model.runModel();


        // directory for population files:

        log.info("############################################");
        log.info("############################################");
        {
            Table ppStart = Table.read().csv(popfiles + "pp_0.csv");
            Table ppEnd = Table.read().csv(popfiles + "pp_10.csv");

            // full outer join on "id" — Tablesaw will rename colliding columns from the
            // right-hand table (check joined.columnNames() to confirm the actual suffix,
            // it's typically something like "age_2" but can vary by version)
            Table joined = ppStart.joinOn("id").fullOuter(ppEnd, true, false, "id");

            log.info("Joined columns: " + joined.columnNames());

            String ageStartCol = "age";
            String ageEndCol = "T2.age"; // <-- verify this against the logged column names above

            // people born = present in pp_10 but not pp_0 -> age.x is missing
            Table born = joined.where(joined.column(ageStartCol).isMissing());
            long numBorn = born.rowCount();
            Assertions.assertNotEquals(0, numBorn);
            log.info("Number born: " + numBorn);

            // people who died = present in pp_0 but not pp_10 -> age.y is missing
            Table died = joined.where(joined.column(ageEndCol).isMissing());
            long numDied = died.rowCount();
            Assertions.assertNotEquals(0, numDied);

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
    }

    public void setupModels(DataContainer dataContainer, Properties properties, Config config) {

        try {
        ppFactory = dataContainer.getHouseholdDataManager().getPersonFactory();
        hhFactory = dataContainer.getHouseholdDataManager().getHouseholdFactory();
        ddFactory = dataContainer.getRealEstateDataManager().getDwellingFactory();

        birthModel = new BirthModelImpl(dataContainer, ppFactory, properties, new DefaultBirthStrategy(), SiloUtil.provideNewRandom());

        birthdayModel = new BirthdayModelImpl(dataContainer, properties, SiloUtil.provideNewRandom());

        deathModel = new DeathModelImpl(dataContainer, properties, new DefaultDeathStrategy(), SiloUtil.provideNewRandom());

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

        carOwnershipModel = new FabilandCarOwnership();

        divorceModel = new DivorceModelImpl( dataContainer, movesModel, carOwnershipModel, hhFactory, properties, new DefaultDivorceStrategy(), SiloUtil.provideNewRandom());

        driversLicenseModel = new DriversLicenseModelImpl(dataContainer, properties, new DefaultDriversLicenseStrategy(), SiloUtil.provideNewRandom());

        educationModel = new EducationModelImpl(dataContainer, properties, SiloUtil.provideNewRandom());

        employmentModel = new EmploymentModelImpl(dataContainer, properties, SiloUtil.provideNewRandom());

        leaveParentsModel = new LeaveParentHhModelImpl(dataContainer, movesModel,
                carOwnershipModel, hhFactory, properties, new DefaultLeaveParentalHouseholdStrategy(), SiloUtil.provideNewRandom());

        jobMarketUpdateModel = new JobMarketUpdateImpl(dataContainer, properties, SiloUtil.provideNewRandom());

        constructionModel = new ConstructionModelImpl(dataContainer, ddFactory,
                properties, new FabilandConstructionLocationStrategy(), new DefaultConstructionDemandStrategy(), SiloUtil.provideNewRandom());

        pricingModel = new PricingModelImpl(dataContainer, properties, new DefaultPricingStrategy(), SiloUtil.provideNewRandom());

        renovationModel = new RenovationModelImpl(dataContainer, properties, new DefaultRenovationStrategy(), SiloUtil.provideNewRandom());

        constructionOverwrite = new ConstructionOverwriteImpl(dataContainer, ddFactory, properties, SiloUtil.provideNewRandom());

        inOutMigration = new InOutMigrationImpl(dataContainer, employmentModel, movesModel,
                carOwnershipModel, driversLicenseModel, properties, SiloUtil.provideNewRandom());

        demolitionModel = new DemolitionModelImpl(dataContainer, movesModel,
                inOutMigration, properties, new DefaultDemolitionStrategy(), SiloUtil.provideNewRandom());

        marriageModel = new MarriageModelImpl(dataContainer, movesModel, inOutMigration,
                carOwnershipModel, hhFactory, properties, new DefaultMarriageStrategy(), SiloUtil.provideNewRandom());

        MatsimScenarioAssembler scenarioAssembler;

        MatsimData matsimData = null;
        if (config != null) {
            final Scenario scenario = ScenarioUtils.loadScenario(config);
//			matsimData = new MatsimData(config, properties, ZoneConnectorManagerImpl.ZoneConnectorMethod.WEIGHTED_BY_POPULATION, dataContainer, scenario.getNetwork(), scenario.getTransitSchedule());
            matsimData = new MatsimData(properties, ZoneConnectorManager.ZoneConnectorMethod.WEIGHTED_BY_POPULATION, dataContainer, (MutableScenario) scenario );
            // yyyyyy the scenario is now generated in the MatsimData class, and in consequence the above constructor could/should be removed again
        }
        switch (properties.transportModel.transportModelIdentifier) {
            case MATSIM:
//                SimpleCommuteModeChoice commuteModeChoice = new SimpleCommuteModeChoice(dataContainer, properties, SiloUtil.provideNewRandom());
                SimpleMatsimCommuteModeChoice commuteModeChoice = new SimpleMatsimCommuteModeChoice(dataContainer, properties, SiloUtil.provideNewRandom());
                // (yyyy is also instantiated above.  why not re-use?)
                // --> fails the regression test.  Which is no wonder, since it changes the random number sequence.

                scenarioAssembler = new SimpleCommuteModeChoiceMatsimScenarioAssembler(dataContainer, properties, commuteModeChoice, SimpleCommuteModeChoiceMatsimScenarioAssembler.HandlingOfRandomness.localInstanceFromMatsimWithAlwaysSameSeed);
                // yyyyyy the above needs to be re-coded from a MATSim perspective ... i.e. (1) select the same agents, and (2) take the mode "suggestion" from silo only if home or
                // job location has changed, otherwise keep existing plan with existing mode.  kai, apr'26

                transportModel = new MatsimTransportModel(dataContainer, config, properties, scenarioAssembler, matsimData);
                break;
            case NONE:
            default:
                transportModel = null;
        }
        } catch (Exception ee) {
            log.fatal("there was an exception: \n" + ee);

            ee.printStackTrace();

            // if one catches an exception, then one needs to explicitly fail the test:
            Assertions.fail();
        }
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

