package de.tum.bgu.msm.syntheticPopulationGenerator.munich2022.allocation;

import de.tum.bgu.msm.container.DataContainer;
import de.tum.bgu.msm.data.MunichDwellingTypes;
import de.tum.bgu.msm.data.dwelling.*;
import de.tum.bgu.msm.data.household.Household;
import de.tum.bgu.msm.data.household.HouseholdDataManager;
import de.tum.bgu.msm.data.household.HouseholdFactory;
import de.tum.bgu.msm.data.person.*;
import de.tum.bgu.msm.syntheticPopulationGenerator.munich2022.DataSetSynPop;
import de.tum.bgu.msm.syntheticPopulationGenerator.munich2022.preparation.MicroDataManager;
import de.tum.bgu.msm.syntheticPopulationGenerator.properties.PropertiesSynPop;
import de.tum.bgu.msm.utils.SiloUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GenerateHouseholdsPersonsDwellings {

    private static final Logger logger = LogManager.getLogger(GenerateHouseholdsPersonsDwellings.class);

    private final DataContainer dataContainer;

    private final DataSetSynPop dataSetSynPop;
    private final MicroDataManager microDataManager;
    private int previousHouseholds;
    private int previousPersons;
    private Map<Integer, Map<Integer, Float>> ddQuality;
    private int totalHouseholds;
    private float ddTypeProbOfSFAorSFD;
    private float ddTypeProbOfMF234orMF5plus;
    private Map<Integer, Float> probTAZ;
    private Map<Integer, Float> probMicroData;
    private Map<Integer, Float> probVacantBuildingSize;
    private Map<Integer, Float> probVacantFloor;
    private double[] probabilityId;
    private double sumProbabilities;
    private double[] probabilityTAZ;
    private double sumTAZs;
    private int[] ids;
    private int[] idTAZs;
    private int personCounter;
    private int householdCounter;

    private HashMap<Person, Integer> educationalLevel;

    private HouseholdDataManager householdData;


    public GenerateHouseholdsPersonsDwellings(DataContainer dataContainer, DataSetSynPop dataSetSynPop, HashMap<Person, Integer> educationalLevel){
        this.dataContainer = dataContainer;
        this.dataSetSynPop = dataSetSynPop;
        this.educationalLevel = educationalLevel;
        microDataManager = new MicroDataManager(dataSetSynPop);
    }

    public void run(){
        logger.info("   Running module: household, person and dwelling generation");
        previousHouseholds = 0;
        previousPersons = 0;
        householdData = dataContainer.getHouseholdDataManager();
        for (int municipality : dataSetSynPop.getMunicipalities()){
            initializeMunicipalityData(municipality);
            double logging = 2;
            int it = 12;
            int[] hhSelection = selectMultipleHouseholds(totalHouseholds);
            int[] tazSelection = selectMultipleTAZ(totalHouseholds);
            for (int draw = 0; draw < totalHouseholds; draw++) {
                int hhSelected = hhSelection[draw];
                int tazSelected = tazSelection[draw];
                Household household = generateHousehold();
                generateDwelling(hhSelected, household.getId(), tazSelected, municipality);
                generatePersons(hhSelected, household);
                if (draw == logging & draw > 2) {
                    logger.info("   Municipality " + municipality + ". Generated household " + draw);
                    it++;
                    logging = Math.pow(2, it);
                }
            }
        }
    }


    private Household generateHousehold(){

        HouseholdFactory factory = householdData.getHouseholdFactory();
        int id = householdData.getNextHouseholdId();
        Household household = factory.createHousehold(id,id,0);
                householdData.addHousehold(household);
        householdCounter++;
        return household;
    }


    private void generatePersons(int hhSelected, Household hh){

        int hhSize = (int) dataSetSynPop.getHouseholdDataSet().getValueAt(hhSelected, "h.Size");
        PersonFactory factory = householdData.getPersonFactory();
        for (int person = 0; person < hhSize; person++) {
            int id = householdData.getNextPersonId();
            int personSelected = (int) (dataSetSynPop.getHouseholdDataSet().getValueAt(hhSelected, "personCount") + person);
            int age = (int) dataSetSynPop.getPersonDataSet().getValueAt(personSelected, "p.age");
            Gender gender = Gender.valueOf((int) dataSetSynPop.getPersonDataSet().getValueAt(personSelected, "p.gender"));
            Occupation occupation = Occupation.valueOf((int) dataSetSynPop.getPersonDataSet().getValueAt(personSelected, "p.employmentStatus"));
            Nationality nationality1 = microDataManager.translateNationality((int) dataSetSynPop.getPersonDataSet().getValueAt(personSelected, "p.nationality"));
            int income = microDataManager.translateIncome((int) dataSetSynPop.getPersonDataSet().getValueAt(personSelected, "p.income"));
            boolean license = MicroDataManager.obtainLicense(gender, age);
            int educationDegree = (int) dataSetSynPop.getPersonDataSet().getValueAt(personSelected, "p.education");
            PersonRole personRole = microDataManager.translatePersonRole((int) dataSetSynPop.getPersonDataSet().getValueAt(personSelected, "p.householdRole"));
            int school = (int) dataSetSynPop.getPersonDataSet().getValueAt(personSelected, "p.school");
            PersonMuc pers = (PersonMuc) factory.createPerson(id, age, gender, occupation,personRole, 0, income); //(int id, int age, int gender, Race race, int occupation, int workplace, int income)
            pers.setNationality(nationality1);
            pers.setDriverLicense(license);
            pers.setSchoolType(school);
            householdData.addPerson(pers);
            householdData.addPersonToHousehold(pers, hh);
            educationalLevel.put(pers, educationDegree);
            personCounter++;
        }
    }


    private void generateDwelling(int hhSelected, int idHousehold, int tazSelected, int municipality){

        RealEstateDataManager realEstate = dataContainer.getRealEstateDataManager();
        int newDdId = realEstate.getNextDwellingId();
        int yearBracket = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.year");
        int year = microDataManager.dwellingYearfromBracket(yearBracket);
        int floorSpace = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.space");
        int useInteger = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.use");
        DwellingUsage usage = DwellingUsage.valueOf(useInteger);
        int buildingSize = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.numberOfApartments");
        int ddHeatingEnergy = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.heatingEnergy");
        int ddHeatingDistrict = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.heating.district");
        int ddHeatingCentral = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.heating.central");
        int ddAdHeating = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.numberOfHeatingTypes");
        int quality = microDataManager.guessDwellingQuality(ddHeatingDistrict, ddHeatingCentral, ddHeatingEnergy, ddAdHeating, yearBracket);
//        DwellingType type = microDataManager.translateDwellingType(buildingSize, ddTypeProbOfSFAorSFD, ddTypeProbOfMF234orMF5plus);

        //TO DO: check if this is correct
        int typeInteger = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.type");
        MunichDwellingTypes.DwellingTypeMunich type = MunichDwellingTypes.DwellingTypeMunich.valueOf(typeInteger);

//        int bedRooms = microDataManager.guessBedrooms(floorSpace);
        int bedRooms = (int) dataSetSynPop.getDwellingDataSet().getValueAt(hhSelected, "d.numberOfRooms");
        int groundPrice = dataSetSynPop.getDwellingPriceByTypeAndZone().get(tazSelected).get(type);
        int price = microDataManager.guessPrice(groundPrice, quality, floorSpace, usage);

        Dwelling dwell = DwellingUtils.getFactory().createDwelling(newDdId, tazSelected, null, idHousehold, type , bedRooms, quality, price, year);
        realEstate.addDwelling(dwell);
        dwell.setFloorSpace(floorSpace);
        dwell.setUsage(usage);
    }


    private int selectMicroHouseholdWithReplacement() {

        int hhSelected = SiloUtil.select(probMicroData);
        if (probMicroData.get(hhSelected) > 1){
            probMicroData.put(hhSelected, probMicroData.get(hhSelected) - 1);
        } else {
            probMicroData.remove(hhSelected);
        }
        return hhSelected;
    }


    private int selectTAZwithoutReplacement(int hhSelected){

        int taz = SiloUtil.select(probTAZ);
        return taz;
    }


    private void initializeMunicipalityData(int municipality){

        logger.info("   Municipality " + municipality + ". Starting to generate households and persons");
        totalHouseholds = (int) PropertiesSynPop.get().main.marginalsMunicipality.getIndexedValueAt(municipality, "hhTotal");
//        ddTypeProbOfSFAorSFD = PropertiesSynPop.get().main.marginalsMunicipality.getIndexedValueAt(municipality,"ddProbSFAorSFD");
//        ddTypeProbOfMF234orMF5plus = PropertiesSynPop.get().main.marginalsMunicipality.getIndexedValueAt(municipality,"ddProbMF234orMF5plus");
        probTAZ = dataSetSynPop.getProbabilityZone().get(municipality);
        probMicroData = new HashMap<>();
        probabilityId = new double[dataSetSynPop.getWeights().getRowCount()];
        ids = new int[probabilityId.length];
        sumProbabilities = 0;
        for (int id : dataSetSynPop.getWeights().getColumnAsInt("ID")){
            probMicroData.put(id, dataSetSynPop.getWeights().getValueAt(id, Integer.toString(municipality)));
        }
        for (int i = 0; i < probabilityId.length; i++){
            sumProbabilities = sumProbabilities + dataSetSynPop.getWeights().getValueAt(i+1, Integer.toString(municipality));
            probabilityId[i] = dataSetSynPop.getWeights().getValueAt(i+1, Integer.toString(municipality));
            ids[i] = (int) dataSetSynPop.getWeights().getValueAt(i+1, "ID");
        }
        probabilityTAZ = new double[dataSetSynPop.getProbabilityZone().get(municipality).keySet().size()];
        sumTAZs = 0;
        probabilityTAZ = dataSetSynPop.getProbabilityZone().get(municipality).values().stream().mapToDouble(Number::doubleValue).toArray();
        for (int i = 1; i < probabilityTAZ.length; i++){
            probabilityTAZ[i] = probabilityTAZ[i] + probabilityTAZ[i-1];
        }
        idTAZs = dataSetSynPop.getProbabilityZone().get(municipality).keySet().stream().mapToInt(Number::intValue).toArray();
        sumTAZs = dataSetSynPop.getProbabilityZone().get(municipality).values().stream().mapToDouble(Number::doubleValue).sum();
        personCounter = 0;
        householdCounter = 0;
    }


    public int[] selectMultipleHouseholds(int selections) {

        int[] selected;
        selected = new int[selections];
        int completed = 0;
        for (int iteration = 0; iteration < 100; iteration++){
            int m = selections - completed;
            double[] randomChoices = new double[m];
            for (int k = 0; k < randomChoices.length; k++) {
                randomChoices[k] = SiloUtil.getRandomNumberAsDouble()*selections;
            }
            Arrays.sort(randomChoices);

            //look up for the n travellers
            int p = 0;
            double cumulative = probabilityId[p];
            for (double randomNumber : randomChoices){
                while (randomNumber > cumulative && p < probabilityId.length - 1) {
                    p++;
                    cumulative += probabilityId[p];
                }
                if (probabilityId[p] > 0) {
                    selected[completed] = ids[p];
                    completed++;
                }
            }
        }
        return selected;

    }

    private int[] selectMultipleTAZ(int selections){

        int[] selected;
        selected = new int[selections];
        int completed = 0;
        for (int iteration = 0; iteration < 100; iteration++){
            int m = selections - completed;
            //double[] randomChoice = new double[(int)(numberOfTrips*1.1) ];
            double[] randomChoices = new double[m];
            for (int k = 0; k < randomChoices.length; k++) {
                randomChoices[k] = SiloUtil.getRandomNumberAsDouble();
            }
            Arrays.sort(randomChoices);

            //look up for the n travellers
            int p = 0;
            double cumulative = probabilityTAZ[p];
            for (double randomNumber : randomChoices){
                while (randomNumber > cumulative && p < probabilityTAZ.length - 1) {
                    p++;
                    cumulative += probabilityTAZ[p];
                }
                if (probabilityTAZ[p] > 0) {
                    selected[completed] = idTAZs[p];
                    completed++;
                }
            }
        }
        return selected;
    }
}
