package de.tum.bgu.msm.syntheticPopulationGenerator.munich2022.preparation;


import de.tum.bgu.msm.syntheticPopulationGenerator.munich2022.DataSetSynPop;
import de.tum.bgu.msm.syntheticPopulationGenerator.properties.PropertiesSynPop;
import de.tum.bgu.msm.utils.SiloUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TranslateMicroDataToCode {

    private static final Logger logger = LogManager.getLogger(TranslateMicroDataToCode.class);

    private DataSetSynPop dataSetSynPop;

    public TranslateMicroDataToCode(DataSetSynPop dataSetSynPop){
        this.dataSetSynPop = dataSetSynPop;
    }


    public void run(){

        //method to translate the categories from the initial micro data to the categories from SILO
        logger.info("   Starting to translate the micro data");

        //convert one by one the records from microPersons
        for (int personCount = 1; personCount <= dataSetSynPop.getPersonTable().rowKeySet().size(); personCount++){
            boolean attendingSchool = translateSchoolAttendance(personCount);
            translateHighestEducationalDegree(personCount);
            translateOccupation(personCount, attendingSchool);
            translateRelationshipToHouseholdHead(personCount);
//            translateAge(personCount);
        }
        //convert one by one the records from microHouseholds
//        for (int hhCount = 1; hhCount <= dataSetSynPop.getHouseholdTable().rowKeySet().size(); hhCount++){
//            //translateIncome(hhCount);
//        }
        //convert one by one the records from microDwellings
        for (int ddCount = 1; ddCount <= dataSetSynPop.getDwellingTable().rowKeySet().size(); ddCount++){
            translateDwellingUsage(ddCount);
//            translateDwellingSpace(ddCount);
//            translateDwellingHeatingEnergy(ddCount);
//            translateDwellingNumberOfRooms(ddCount);
            translateDwellingType(ddCount);
            translateDwellingNumberOfApartments(ddCount);
            translateDwellingYear(ddCount);
//            translateDwellingHeatingType(ddCount);
        }
        logger.info("Finished translating the micro data");
    }

//    private void translateAge(int personCount) {
//        int age = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"p.age");
//
//        int valueCode;
//
//        if(age<=4){
//            valueCode=1;
//        }else if(age<=9){
//            valueCode=2;
//        }else if(age<=14){
//            valueCode=3;
//        }else if(age<=19){
//            valueCode=4;
//        }else if(age<=24){
//            valueCode=5;
//        }else if(age<=29){
//            valueCode=6;
//        }else if(age<=34){
//            valueCode=7;
//        }else if(age<=39){
//            valueCode=8;
//        }else if(age<=44){
//            valueCode=9;
//        }else if(age<=49){
//            valueCode=10;
//        }else if(age<=54){
//            valueCode=11;
//        }else if(age<=59){
//            valueCode=12;
//        }else if(age<=64){
//            valueCode=13;
//        }else if(age<=69){
//            valueCode=14;
//        }else if(age<=74){
//            valueCode=15;
//        }else if(age<=79){
//            valueCode=16;
//        }else if(age<=99){
//            valueCode=17;
//        }else {
//            valueCode=18;
//        }
//
//        dataSetSynPop.getPersonDataSet().setValueAt(personCount,"p.age",valueCode);
//    }


    private void translateOccupation(int personCount, boolean attendingSchool) {
        int occupation = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"p.employmentStatus");
        if (occupation > 1){
            if (attendingSchool){
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"p.employmentStatus",3);

            } else {
                dataSetSynPop.getPersonDataSet().setValueAt(personCount,"p.employmentStatus",2);
            }
        }
    }


    private void translateRelationshipToHouseholdHead(int personCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"p.householdRole");
        switch (valueMicroData){
            case 1: //Household head (hHH)
                valueCode = 1;
                break;
            case 2: //Partner of hHH
                valueCode = 2;
                break;
            case 3: //kid of hHH
                valueCode = 3;
                break;
            case 4: //grandchild of hHH
                valueCode = 3;
                break;
            case 5: //mother or father of hHH
                valueCode = 4;
                break;
            case 6: //grandfather or grandmother of hHH,
                valueCode = 4;
                break;
            case 7: //sibling of hHH
                valueCode = 4;
                break;
            case 8: //other relationship with hHH
                if ((int) dataSetSynPop.getPersonDataSet().getValueAt(personCount, "p.age") < 16) {
                    valueCode = 3;
                } else {
                    valueCode = 4;
                }
                break;
            case 9: //not related with hHH
                if ((int) dataSetSynPop.getPersonDataSet().getValueAt(personCount, "p.age") < 16) {
                    valueCode = 3;
                } else {
                    valueCode = 4;
                }
                break;
        }
        dataSetSynPop.getPersonDataSet().setValueAt(personCount,"p.householdRole", valueCode);
    }


    private boolean translateSchoolAttendance(int personCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"p.school");
        switch (valueMicroData){
            case -5:
                valueCode = 0;
                break;
            case 1: //Grundschule
                valueCode = 1;
                break;
            case 2: //Schulartunabhängige Orientierungsstufe
                valueCode = 2;
                break;
            case 3: //Sonderschule (Förderschule)
                valueCode = 2;
                break;
            case 4: //Schularten mit mehreren Bildungsgängen
                valueCode = 2;
                break;
            case 5: //Hauptschule
                valueCode = 2;
                break;
            case 6: //Realschule
                valueCode = 2;
                break;
            case 7: //Gesamtschule, Waldorfschule
                valueCode = 2;
                break;
            case 8: //Waldorfschule
                valueCode = 2;
                break;
            case 9: //Gymnasium
                valueCode = 2;
                break;
            case 10: //Berufliches, auch Wirtschafts- oder technisches Gymnasium
                valueCode = 2;
                break;
            case 11: //Abendgymnasium, Kolleg
                valueCode = 2;
                break;
            case 12: //Berufliche Schule, die zur mittleren Reife führt (z.B. Berufsfachschule)
                valueCode = 0;
                break;
            case 13: //Berufliche Schule, die zur Hochschul-/ Fachhochschulreife führt: Fachoberschule
                valueCode = 0;
                break;
            case 14: //Berufliche Schule, die zur Hochschul-/ Fachhochschulreife führt: Berufsfachschule
                valueCode = 0;
                break;
            case 15: //Berufliche Schule, die zur Hochschul-/ Fachhochschulreife führt: Berufsoberschule, Technische Oberschule
                valueCode = 0;
                break;
            case 16: //Berufsvorbereitungsjahr
                valueCode = 0;
                break;
            case 17: //Berufsgrundbildungsjahr
                valueCode = 0;
                break;
            case 18: //Berufsschule
                valueCode = 0;
                break;
            case 19: //Berufsfachschule,
                valueCode = 0;
                break;
            case 20: //Ausbildungsstätte/Schule für Gesundheits- und Sozialberufe: einjährig (z.B. Altenpflegehelfer/-in)
                valueCode = 3;
                break;
            case 21: //Ausbildungsstätte/Schule für Gesundheits- und Sozialberufe: zweijährig (z.B. Masseur/-in, PTA)
                valueCode = 3;
                break;
            case 22: //Ausbildungsstätte/Schule für Gesundheits- und Sozialberufe: dreijährig (z.B. Physiotherapie, MTA, Altenpflege)
                valueCode = 3;
                break;
            case 23: //Ausbildungsstätten/Schulen für Erzieher/-innen
                valueCode = 3;
                break;
            case 24: //Meisterausbildung an Fachschulen
                valueCode = 3;
                break;
            case 25: //Fachschule unter anderem für Techniker/-innen, Betriebswirtinnen/Betriebswirte
                valueCode = 3;
                break;
            case 26: //Fachakademie (nur in Bayern)
                valueCode = 3;
                break;
            case 27: //Berufsakademie
                valueCode = 3;
                break;
            case 28: //Verwaltungsfachhochschule
                valueCode = 3;
                break;
            case 29: //Fachhochschule
                valueCode = 3;
                break;
            case 30: //Universität
                valueCode = 3;
                break;
            case 31: //Promotionsstudium
                valueCode = 3;
                break;

        }
        dataSetSynPop.getPersonDataSet().setValueAt(personCount,"p.school", valueCode);
        boolean schoolAttendance = false;
        if (valueCode > 0){
            schoolAttendance = true;
        }
        return schoolAttendance;
    }


    private void translateHighestEducationalDegree(int personCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getPersonDataSet().getValueAt(personCount,"p.education");
        switch (valueMicroData) {
            case -3: //Younger than 15 years old
                valueCode = 1;
                break;
            case 88: //No (vocational) qualification (including students at general education schools)
                valueCode = 1;
                break;
            case 1: //Anlernausbildung oder berufliches Praktikum
                valueCode = 2;
                break;
            case 2: //Berufsvorbereitungsjahr
                valueCode = 2;
                break;
            case 3: //Abschluss einer Lehre/Berufsausbildung im dualen System
                valueCode = 2;
                break;
            case 4: //Berufsqualifizierender Abschluss
                valueCode = 2;
                break;
            case 5: //Vorbereitungsdienst für den mittleren Dienst in der öffentlichen Verwaltung
                valueCode = 4;
                break;
            case 6: //Ausbildungsstätten, Schule für Gesundheits und Sozialberufe: 1-jährig (z.B. Kranken- oder Altenpflegehelfer, Rettungsassistent)
                valueCode = 2;
                break;
            case 7: //Ausbildungsstätten, Schule für Gesundheits- und Sozialberufe: 2-jährig (z.B. Masseur, Medizinischer Bademeister, PTA, Podologe)
                valueCode = 2;
                break;
            case 8: //Abschluss einer Meisterausbildung
                valueCode = 2;
                break;
            case 9: //Abschluss der Fachschule der DDR
                valueCode = 2;
                break;
            case 10: //Fachakademie (nur in Bayern)
                valueCode = 2;
                break;
            case 11: //Berufsakademie (Bachelor)
                valueCode = 4;
                break;
            case 12: //Berufsakademie (Master)
                valueCode = 4;
                break;
            case 13: //Berufsakademie (Diplom*)
                valueCode = 4;
                break;
            case 14: //Ausbildungsstätten, Schule für Gesundheits und Sozialberufe: 3-jährig (z.B. Physiotherapie, Gesundheits- und Krankenpflege, MTA, Altenpflege)
                valueCode = 2;
                break;
            case 15: //Ausbildungsstätten/Schulen für Erzieher/-innen
                valueCode = 2;
                break;
            case 16: //Berufliches Praktikum
                valueCode = 2;
                break;
            case 18: //Abschluss einer Technikerausbildung oder gleichwertiger Fachschulabschluss
                valueCode = 3;
                break;
            case 21: //Verwaltungsfachhochschule (Bachelor)
                valueCode = 3;
                break;
            case 22: //Verwaltungsfachhochschule (Master)
                valueCode = 3;
                break;
            case 23: //Verwaltungsfachhochschule (Diplom*)
                valueCode = 3;
                break;
            case 31: //Fachhochschule (auch Ingenieurschule) / Duale Hochschule (Bachelor)
                valueCode = 3;
                break;
            case 32: // Fachhochschule (auch Ingenieurschule) / Duale Hochschule (Master)
                valueCode = 3;
                break;
            case 33: //Fachhochschule (auch Ingenieurschule) / Duale Hochschule (Diplom)
                valueCode = 3;
                break;
            case 41: //Universität (wissenschaftliche Hochschule, Kunsthochschule) (Bachelor)
                valueCode = 4;
                break;
            case 42: // Universität (wissenschaftliche Hochschule, Kunsthochschule) (Master)
                valueCode = 4;
                break;
            case 43: //Universität (wissenschaftliche Hochschule, Kunsthochschule) (Diplom)
                valueCode = 4;
                break;
            case 50: //Anlernausbildung (bis 1953 geboren: berufliche Qualifizierung für den Arbeitsmarkt)
                valueCode = 4;
                break;
            case 60: //Promotion
                valueCode = 4;
                break;
        }
        dataSetSynPop.getPersonDataSet().setValueAt(personCount,"p.education", valueCode);
    }


    private void translateDwellingUsage(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getDwellingDataSet().getValueAt(ddCount,"d.use");
        switch (valueMicroData){
            case -9: //No data
                valueCode = 0;
                break;
            case 1: //Owner of building
                valueCode = 1;
                break;
            case 2: //Owner of dwelling
                valueCode = 1;
                break;
            case 3: //Renter (principal)
                valueCode = 2;
                break;
            case 4: //Sub-renter
                valueCode = 2;
                break;
            case 5: //other
                valueCode = 0;
                break;
        }
        dataSetSynPop.getDwellingDataSet().setValueAt(ddCount,"d.use",valueCode);
    }

    private void translateDwellingYear(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getDwellingDataSet().getValueAt(ddCount,"d.year");
        switch (valueMicroData){
            case -9: //No data
                valueCode = 0;
                break;
            case 1: // before 1919
                valueCode = 1;
                break;
            case 2: // 1919-1948
                valueCode = 1;
                break;
            case 3: // 1949-1978
                valueCode = 2;
                break;
            case 4: // 1979-1990
                valueCode = 2;
                break;
            case 5: // 1991-2000
                valueCode = 3;
                break;
            case 6: // 2001-2010
                valueCode = 3;
                break;
            case 7: // 2011-2019
                valueCode = 4;
                break;
            case 10: // 2020 and later
                valueCode = 4;
                break;
        }
        dataSetSynPop.getDwellingDataSet().setValueAt(ddCount,"d.year",valueCode);
    }

//    private void translateDwellingSpace(int ddCount) {
//        int space = (int) dataSetSynPop.getDwellingDataSet().getValueAt(ddCount,"d.space");
//
//        int valueCode;
//
//        if(space<=30){
//            valueCode=1;
//        }else if(space<=60){
//            valueCode=2;
//        }else if(space<=80){
//            valueCode=3;
//        }else if(space<=100){
//            valueCode=4;
//        }else if(space<=120){
//            valueCode=5;
//        }else if(space<=2000){
//            valueCode=6;
//        }else {
//            valueCode=7;
//        }
//
//        dataSetSynPop.getDwellingDataSet().setValueAt(ddCount,"d.space",valueCode);
//    }


    private void translateDwellingHeatingEnergy(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getDwellingDataSet().getValueAt(ddCount,"d.heatingEnergy");
        switch (valueMicroData){
            case -9: //No data
                valueCode = 0;
                break;
            case 1: //Fernwärme (bei Fernheizung)
                valueCode = 1;
                break;
            case 2: //Gas
                valueCode = 1;
                break;
            case 3: //Elektrizität, Strom (ohne Wärmepumpe)
                valueCode = 1;
                break;
            case 4: //Heizöl
                valueCode = 1;
                break;
            case 5: //Briketts, Braunkohle
                valueCode = 0;
                break;
            case 6: //Koks, Steinkohle
                valueCode = 0;
                break;
            case 10: //Holz, Holzpellets
                valueCode = 0;
                break;
            case 12: //Biomasse (außer Holz), Biogas
                valueCode = 0;
                break;
            case 13: //Sonnenenergie (Solarkollektoren)
                valueCode = 1;
                break;
            case 14: //Erd- und andere Umweltwärme, Abluftwärme (Wärmepumpen, -tauscher)
                valueCode = 1;
                break;
        }
        dataSetSynPop.getDwellingDataSet().setValueAt(ddCount,"d.heatingEnergy",valueCode);
    }

    public void translateDwellingNumberOfRooms(int ddCount) {
        int raw = (int) dataSetSynPop.getDwellingDataSet().getValueAt(ddCount, "d.numberOfRooms");
        int code;

        if (raw == -9) {                 // no data
            code = 0;
        } else if (raw >= 5) {           // 5 or more rooms
            code = 5;
        } else if (raw >= 1 && raw <= 4) {
            code = raw;                  // 1–4 rooms map as-is
        } else {
            code = 0;                    // any other unexpected value
        }

        dataSetSynPop.getDwellingDataSet().setValueAt(ddCount, "d.numberOfRooms", code);
    }

    public void translateDwellingType(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getDwellingDataSet().getValueAt(ddCount,"d.type");
        switch (valueMicroData){
            case -9: //No data
                valueCode = 0;
                break;
            case 1: //EFH, freistehend
                valueCode = 1;
                break;
            case 2: //EFH als Doppelhaushälfte
                valueCode = 2;
                break;
            case 3: //EFH als Reihenhaus
                valueCode = 3;
                break;
            case 4: //MFH, freistehend
                valueCode = 4;
                break;
            case 5: //MFH, gereiht (einseitig oder beidseitig angebaut)
                valueCode = 4;
                break;
        }
        dataSetSynPop.getDwellingDataSet().setValueAt(ddCount,"d.type",valueCode);
    }

    public void translateDwellingNumberOfApartments(int ddCount){
        int valueCode = 0;
        int valueMicroData = (int) dataSetSynPop.getDwellingDataSet().getValueAt(ddCount,"d.numberOfApartments");
        switch (valueMicroData){
            case -9: //No data
                valueCode = 0;
                break;
            case 1: //1 apartment
                valueCode = 1;
                break;
            case 2: //2 apartments
                valueCode = 1;
                break;
            case 3: //3 or 4 apartments
                valueCode = 2;
                break;
            case 4: //5 or 6 apartments
                valueCode = 2;
                break;
            case 5: //7 to 9 apartments
                valueCode = 3;
                break;
            case 6: //10 to 20 apartments
                valueCode = 3;
                break;
            case 7: //21 or more apartments
                valueCode = 3;
                break;
        }
        dataSetSynPop.getDwellingDataSet().setValueAt(ddCount,"d.numberOfApartments",valueCode);
    }


//    private void translateDwellingHeatingType(int ddCount){
//        int valueCode = 0;
//        int valueMicroData = (int) dataSetSynPop.getDwellingTable().get(ddCount,"ddHeatingType");
//        switch (valueMicroData){
//            case -1: //Group quarter
//                valueCode = 0;
//                break;
//            case -5: //Moved out last year
//                valueCode = 0;
//                break;
//            case 1: //Fernheizung
//                valueCode = 1;
//                break;
//            case 2: //Blockheizung, Zentralheizung
//                valueCode = 1;
//                break;
//            case 3: //Etagenheizung
//                valueCode = 0;
//                break;
//            case 4: //Einzel- oder Mehrraumöfen (auch Elektrospeicher)
//                valueCode = 0;
//                break;
//            case 9: //No data
//                valueCode = 0;
//                break;
//        }
//        dataSetSynPop.getDwellingTable().put(ddCount,"ddHeatingType",valueCode);
//    }

//    public int translateIncome(int valueMicroData){
//        int valueCode = 0;
//        double low = 0;
//        double high = 1;
//        double income = 0;
//        switch (valueMicroData){
//            case 90: // kein Einkommen
//                valueCode = 0;
//                break;
//            case 1: //income class
//                low = 0;
//                high = 0.07998391;
//                break;
//            case 2: //income class
//                low = 0.07998391;
//                high = 0.15981282;
//                break;
//            case 3: //income class
//                low = 0.15981282;
//                high = 0.25837521;
//                break;
//            case 4: //income class
//                low = 0.25837521;
//                high = 0.34694010;
//                break;
//            case 5: //income class
//                low = 0.34694010;
//                high = 0.42580696;
//                break;
//            case 6: //income class
//                low = 0.42580696;
//                high = 0.49569720;
//                break;
//            case 7: //income class
//                low = 0.49569720;
//                high = 0.55744375;
//                break;
//            case 8: //income class
//                low = 0.55744375;
//                high = 0.61188119;
//                break;
//            case 9: //income class
//                low = 0.61188119;
//                high = 0.65980123;
//                break;
//            case 10: //income class
//                low = 0.65980123;
//                high = 0.72104215;
//                break;
//            case 11: //income class
//                low = 0.72104215;
//                high = 0.77143538;
//                break;
//            case 12: //income class
//                low = 0.77143538;
//                high = 0.81284178;
//                break;
//            case 13: //income class
//                low = 0.81284178;
//                high = 0.84682585;
//                break;
//            case 14: //income class
//                low = 0.84682585;
//                high = 0.87469331;
//                break;
//            case 15: //income class
//                low = 0.87469331;
//                high = 0.90418202;
//                break;
//            case 16: //income class
//                low = 0.90418202;
//                high = 0.92677087;
//                break;
//            case 17: //income class
//                low = 0.92677087;
//                high = 0.94770566;
//                break;
//            case 18: //income class
//                low = 0.94770566;
//                high = 0.96267752;
//                break;
//            case 19: //income class
//                low = 0.96267752;
//                high = 0.97337602;
//                break;
//            case 20: //income class
//                low = 0.97337602;
//                high = 0.98101572;
//                break;
//            case 21: //income class
//                low = 0.98101572;
//                high = 0.99313092;
//                break;
//            case 22: //income class
//                low = 0.99313092;
//                high = 0.99874378;
//                break;
//            case 23: //income class
//                low = 0.99874378;
//                high = 0.99999464;
//                break;
//            case 24: //income class
//                low = 0.99999464;
//                high = 1;
//                break;
//        }
//        double cummulativeProb = SiloUtil.getRandomNumberAsDouble()*(high - low) + low;
//        try {
//            income = PropertiesSynPop.get().main.incomeGammaDistribution.inverseCumulativeProbability(cummulativeProb);
//            valueCode = (int) income;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return valueCode;
//    }

}
