package de.tum.bgu.msm.data.dwelling;


public enum DwellingTypesMunich2022Enum {
    EFHFreistehend, EFHDoppelhaus, EFHReihenhaus, MFH;

    public static DwellingTypesMunich2022Enum valueOf(int code) {
        switch (code) {
            case 1:
                return EFHFreistehend;
            case 2:
                return EFHDoppelhaus;
            case 3:
                return EFHReihenhaus;
            case 4:
                return MFH;
        }
        return null;
    }
}


