package de.tum.bgu.msm.data;

import de.tum.bgu.msm.data.dwelling.DwellingType;
import de.tum.bgu.msm.data.dwelling.DwellingTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MunichDwellingTypes implements DwellingTypes {

    private final static List<DwellingType> TYPES = Collections.unmodifiableList(Arrays.asList(DwellingTypeMunich.values()));

    @Override
    public DwellingType valueOf(String tp) {
        return DwellingTypeMunich.valueOf(tp);
    }

    @Override
    public List<DwellingType> getTypes() {
        return TYPES;
    }

    public enum DwellingTypeMunich implements DwellingType {

        /**
         * detached house detached
         */
        EFHFreistehend (0.25f,0.03f),
        /**
         * detached house, terraced/end of terrace
         */
        EFHDoppelhaus(0.22f,0.03f),
        /**
         * flat
         */
        EFHReihenhaus(0.15f,0.03f),
        /**
         * mobile home
         */
        MFH(0.05f,0.04f);

        private final float acresNeeded;
        private final float structuralVacancy;

        DwellingTypeMunich(float acresNeeded, float structuralVacancy) {
            this.acresNeeded = acresNeeded;
            this.structuralVacancy = structuralVacancy;
        }

        @Override
        public float getAreaPerDwelling() {
            return acresNeeded;
        }

        @Override
        public float getStructuralVacancyRate() {
            return structuralVacancy;
        }


        public int getId() {
            return this.ordinal();
        }

        public static DwellingTypeMunich valueOf(int code){
            switch (code){
                case 1:
                    return EFHFreistehend;
                case 2:
                    return EFHDoppelhaus;
                case 3:
                    return EFHReihenhaus;
                case 4:
                    return MFH;
                case 5:
                    return MFH;
                default:
                    throw new IllegalArgumentException(String.format("Code %d not valid.", code));
            }
        }

//        public int getsizeOfDwelling(){
//            switch(this){
//                case MH: return 30;
//                case FLAT: return 60;
//                case SFA: return 120;
//                case SFD: return 200;
//                default: throw new RuntimeException("Housing Type not found: " + this);
//            }
//        }
    }
}
