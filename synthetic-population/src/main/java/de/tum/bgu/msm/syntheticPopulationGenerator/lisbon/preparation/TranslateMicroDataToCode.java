package de.tum.bgu.msm.syntheticPopulationGenerator.lisbon.preparation;


import de.tum.bgu.msm.common.datafile.TableDataSet;
import de.tum.bgu.msm.syntheticPopulationGenerator.DataSetSynPop;
import de.tum.bgu.msm.utils.SiloUtil;
import org.apache.log4j.Logger;

public class TranslateMicroDataToCode {

    private static final Logger logger = Logger.getLogger(TranslateMicroDataToCode.class);

    private DataSetSynPop dataSetSynPop;


    public TranslateMicroDataToCode(DataSetSynPop dataSetSynPop){
        this.dataSetSynPop = dataSetSynPop;
    }


    public void run(){

        //method to translate the categories from the initial micro data to the categories from SILO
        logger.info("   Starting to translate the micro data");

       initializeNewVariables();

       //convert one by1 one the records from microPersons
        //TODO [Amrutha]: translate person attributes from microPerson to census code/category
        for (int personCount : dataSetSynPop.getPersonDataSet().getColumnAsInt("IndividualID")){
            translateOccupation(personCount);
            translateAge(personCount);
            translateRelationshipToHouseholdHead(personCount); //available for lisbon(?) to be re-checked
            translateEducationLevel(personCount); //to create new method from JP or Capetown
            //translateMarriage(personCount); //marriage variable still not available
            translateIncome(personCount); //to create method from JP/CT
            translateGender(personCount); //gender method to be made
        }
        //convert one by one the records from microHouseholds
        for (int hhCount : dataSetSynPop.getHouseholdDataSet().getColumnAsInt("HouseholdID")){
            translateDwellingType(hhCount);
            translateDwellingUsage(hhCount);
            translateDwellingSize(hhCount); //new method
            translateDwellingsBathroom(hhCount); //new method
            translateDwellingsYearBuilt(hhCount); //new method
            translateIncome(hhCount); //new method
            //added variables - size, dwellings with bathrooms, year built, income
        }
        logger.info("   Finished translating the micro data");
    }

    private void initializeNewVariables(){
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(), "employmentCode");
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(),"ageCode");
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(),"relationshipCode");
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(),"personRole");
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(), "educationCode"); //added education level code
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(), "genderCode"); //added gender code
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(), "incomeCode"); //added income code
        //-- household/dwelling data variables --
        appendNewColumnToTDS(dataSetSynPop.getHouseholdDataSet(),"ddTypeCode");
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(), "dwellingSizeCode"); //added dwelling size code
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(), "dwellingsBathroomCode"); //added dwelling bathroom yes/no code
        appendNewColumnToTDS(dataSetSynPop.getPersonDataSet(), "dwellingsYearBuiltCode"); //added dwelling year built code
        //appendNewColumnToTDS(dataSetSynPop.getHouseholdDataSet(),"tenureCode"); not sure if we need this
    }

    private void translateOccupation(int personCount) {
        int occupation = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"employed");
        int age = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"age");
        switch(occupation) {
            case 1:
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"employmentCode", 1); //employed
                break;
            case 2:
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"employmentCode", 2); //unemployed
                break;
            case 3:
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"employmentCode", 3); //student etc.
                break;
            case 4:
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"employmentCode", 98); //doesn't know
                break;
            case 5:
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"employmentCode", 99); //not applicable
                break;
            case 6:
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"employmentCode", 0); //prefer not to answer
                break;
            case 7:
                int guessOccupation = guessOccupation(age);
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"employmentCode", guessOccupation);
                break;
            default:
                throw new IllegalArgumentException(String.format("Code %d not valid.", occupation));
        }

//        Value = 1.0	Label = Employed (EconFull, EconPart, EconGovT)
//        Value = 2.0	Label = Unemployed (EconSick, EconRgUn, EconSkng, EconNSkg)
//        Value = 3.0	Label = Student, retired (EconStdt, EconRtrd)
//        Value = 98.0	Label = Does not know
//        Value = 99.0	Label = Not applicable
//        Value = 0.0   Label = Prefer Not to Say

    }

    private int guessOccupation(int age) {
        if(age <= 6){
            return 0;
        }else if(age <=18){
            return 3; //student
        }else if(age >=66){
            return 3; //retired; statutory retirement age in Lisbon/Portugal is 66 y 7 mo.
        }else if(SiloUtil.getRandomNumberAsDouble()<=0.045){
            return 2;
        }else {
            return 1;
        }
    }

    private void translateAge(int personCount) {
        int age = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"age");

        int valueCode;

        if (age <= 14) {
            valueCode = 1;
        } else if (age <= 24) {
            valueCode = 2;
        } else if (age <= 44) {
            valueCode = 3;
        } else if (age <= 64) {
            valueCode = 4;
        } else if (age <= 84) {
            valueCode = 5;
        } else {
            valueCode = 6;
        }


        dataSetSynPop.getPersonDataSet().setValueAt(personCount,"ageCode",valueCode);

//                1 Value = 1.0	Label = Less than or == 14 year
//                2 Value = 2.0	Label = 15 - 24 years (both included)
//                3 Value = 3.0	Label = 25 - 44 years (both included)
//                4 Value = 4.0	Label = 45 - 64 years (both included)
//                5 Value = 5.0	Label = 65 - 84 years (both included)
//                6 Value = 6.0	Label = more than or == 85 years

    } //reworked new age codes

    private void translateGender(int personCount) {
        int gender = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"gender");

        int valueCode;
        switch(gender){
            case 1:
                valueCode = 1; //male/masculine
                break;
            case 2:
                valueCode = 2; //female/feminine
                break;
            default:
                throw new IllegalArgumentException(String.format("Gender code %d not in survey data", gender));
        }


        dataSetSynPop.getPersonDataSet().setValueAt(personCount,"genderCode",valueCode);

//                1 Value = 1.0	Label = Male/masculine
//                2 Value = 2.0	Label = Female/feminine
//                unfortunately the data has recorded only in a gender-binary format :(

    } //reworked new age codes
    private void translateEducationLevel(int personCount) {
        int educationLevel = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"educationLevel");

        int valueCode;

        if(educationLevel == 0){
            valueCode= 0; //TODO: does not apply, students? need to check
        }else if(educationLevel == 1){
            valueCode= 1;
        }else if(educationLevel == 8){
            valueCode= 3;
        }else {
            valueCode= 2;
        }


        dataSetSynPop.getPersonDataSet().setValueAt(personCount,"educationLevelCode",valueCode);

//      Input values (Portuguese census education levels):
//      0  = Does not apply
//      1  = None or only 1st, 2nd or 3rd year completed               -> NoQual
//      2  = Basic education (1st, 2nd or 3rd cycle)                   -> LowQual
//      3  = Upper secondary / post-secondary non-higher               -> OtherQual
//      4  = Higher education (Bachelor's, Master's, Doctorate, etc.)  -> HighQual
//     98  = Prefer not to say                                         -> Unknown
//     99  = Does not know                                             -> Unknown
// ------------------Output educationCode ---------
//       NAQual Value = 0	    Label = does not apply
//       1 NoQual Value =    1	Label = None or only 1st, 2nd or 3rd year completed
//       2 LowQual Value = 2	Label = Basic education (1st, 2nd or 3rd cycle)
//       3 MidQual Value = 3	Label = Upper secondary education (12th grade completed) or post‑secondary (non‑higher technological specialization course)
//       4 HighQual Value = 4	Label = Higher education (Bachelor’s, Licentiate, Master’s, Doctorate, Short‑cycle professional higher technical course)
//       98 Unknown Value = 5	Label = Prefer not to say
//       99 Unknown Value = 6	Label = Does not know

    }

    private void translateRelationshipToHouseholdHead(int personCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"relationship");
        switch (valueMicroData){
            case 1: //Household head (hHH)
                valueCode = 1;
                break;
            case 2: //Spouse or Partner of hHH
                valueCode = 2;
            case 20: // civil Partner of hHH
                valueCode = 2;
                break;
            case 3: //child or step-child or child-in-law
                valueCode = 3;
                break;
            case 6://grandchild
                valueCode = 3;
                break;
            case 4: //parent, step-parent
            case 5: //sibling or sibling-in-law
            case 7: //grandparent
            case 8: //other family member
            case 9: //non-family member
                valueCode = 4;
                break;
            default:
                throw new IllegalArgumentException(String.format("Relationship code 5d not valid", valueMicroData));
        }
        dataSetSynPop.getPersonDataSet().setValueAt(personCount,"relationshipCode", valueCode);
    }

//* Input values (Portuguese census):
//               1 = Household head / respondent)
//               2 = (Spouse / partner)
//               3 = (Child / stepchild / child-in-law)
//               4 = (Parent / stepparent / parent-in-law)
//               5 = (Sibling / sibling-in-law)
//               6 = (Grandchild)
//               7 = (Grandparent)
//               8 = (Other family members)
//               9 = (Non-family members)
//    -----  Output relationshipCode: -----
//               1 = Household head
//               2 = Partner (spouse / cohabitee)
//               3 = Child / grandchild
//               4 = Other (parent, sibling, grandparent, other relative/non-relative)


    private void translateDwellingType(int hhCount){
        int valueCode = 0;
        int ddType = (int) dataSetSynPop.getHouseholdDataSet().getValueAt(hhCount,"ddType");
        switch (ddType){
            case 1:
                valueCode = 1;
                break;
            case 2:
                valueCode = 2;
                break;
            case 3: //
                valueCode = 3;
                break;
            case 4:
                valueCode = 4;
                break;
        }
//         Value = 1.0	Label = house detached
//         Value = 2.0	Label = other house: semi-detached, terraced/end of terrace
//         Value = 3.0	Label = flat or maisonette, room/rooms
//         Value = 4.0	Label = other


        dataSetSynPop.getHouseholdDataSet().setValueAt(hhCount,"ddTypeCode",valueCode);
    }
//dwelling usage (ownership status)
    private void translateDwellingUsage(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getHouseholdDataSet().getValueAt(ddCount,"tenure");
        switch (valueMicroData){
            case 1:
                valueCode = 1;//own
                break;
            case 2:
                valueCode = 2;//rent private
                break;
            case 3:
            case 4:
                valueCode = 3;//rent social
                break;
        }
        // 1 = own (with/without mortgage)
        // 2 = rent
        // 3 = local authority
        // 4 = RSL

        dataSetSynPop.getHouseholdDataSet().setValueAt(ddCount,"tenureCode",valueCode);
    }
//----dwelling size
    private void translateDwellingSize(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getHouseholdDataSet().getValueAt(ddCount,"tenure");
        switch (valueMicroData){
            case 1:
                valueCode = 1;//own
                break;
            case 2:
                valueCode = 2;//rent private
                break;
            case 3:
            case 4:
                valueCode = 3;//rent social
                break;
        }
        // 1 = own (with/without mortgage)
        // 2 = rent
        // 3 = local authority
        // 4 = RSL

        dataSetSynPop.getHouseholdDataSet().setValueAt(ddCount,"tenureCode",valueCode);
    }

    //----dwelling bathroom
    private void translateDwellingsBathroom(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getHouseholdDataSet().getValueAt(ddCount,"bathroom");
        switch (valueMicroData){
            case 1:
                valueCode = 1;//present
                break;
            case 2:
                valueCode = 2;//not present
                break;
        }
        // 1 = bathroom present
        // 2 = bathroom not present

        dataSetSynPop.getHouseholdDataSet().setValueAt(ddCount,"bathroomCode",valueCode);
    }

    //----dwelling year built -- important if control totals are present then they need to be exactly mapped
    private void translateDwellingsYearBuilt(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getHouseholdDataSet().getValueAt(ddCount,"tenure");
        switch (valueMicroData){
            case 1:
                valueCode = 1;//own
                break;
            case 2:
                valueCode = 2;//rent private
                break;
            case 3:
            case 4:
                valueCode = 3;//rent social
                break;
        }
        // 1 = own (with/without mortgage)
        // 2 = rent
        // 3 = local authority
        // 4 = RSL

        dataSetSynPop.getHouseholdDataSet().setValueAt(ddCount,"tenureCode",valueCode);
    }

    //----household-level income
    private void translateIncome(int ddCount){

        int incomeBracket = (int) dataSetSynPop.getHouseholdDataSet().getValueAt(ddCount,"income");
        int valueCode;
        if (incomeBracket == 99) {
            valueCode = 0; // Not applicable / unknown
        } else if (incomeBracket >= 1 && incomeBracket <= 9) {
            valueCode = incomeBracket; // Pass through directly
        } else {
            throw new IllegalArgumentException(String.format("Income bracket code %d not valid.", incomeBracket));
        }
        dataSetSynPop.getHouseholdDataSet().setValueAt(ddCount, "incomeCode", valueCode);
    }
   //     * Input values:
   //     1  = Less than 430 euros
   //     2  = 430  to less than 600 euros
   //     3  = 600  to less than 1000 euros
  //      4  = 1000 to less than 1500 euros
  //      5  = 1500 to less than 2600 euros
  //      6  = 2600 to less than 3600 euros
   //     7  = 3600 to less than 5700 euros
  //      8  = 5700 to less than 7000 euros
   //     9  = 7000 euros or more
  //      99 = Not applicable / Ns/Nr -> random draw across full distribution (0 to 1)

    private void appendNewColumnToTDS(TableDataSet tableDataSet, String columnName){
        int length = tableDataSet.getRowCount();
        int[] dummy = SiloUtil.createArrayWithValue(length, 0);
        tableDataSet.appendColumn(dummy, columnName);
    }
}
