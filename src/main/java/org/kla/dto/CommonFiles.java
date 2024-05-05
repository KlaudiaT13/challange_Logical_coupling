package org.kla.dto;

public class CommonFiles implements Comparable<CommonFiles>{
    private int numberOfCommonFiles;
    private String firstContributor;
    private String secondContributor;

    public CommonFiles(int numberOfCommonFiles, String firstContributor, String secondContributor) {
        this.numberOfCommonFiles = numberOfCommonFiles;
        this.firstContributor = firstContributor;
        this.secondContributor = secondContributor;
    }

    @Override
    public int compareTo(CommonFiles o) {
        return this.numberOfCommonFiles>o.numberOfCommonFiles?-1:1;
    }

    public int getNumberOfCommonFiles() {
        return numberOfCommonFiles;
    }

    public String getFirstContributor() {
        return firstContributor;
    }

    public String getSecondContributor() {
        return secondContributor;
    }

    @Override
    public String toString() {
        return "numberOfCommonFiles=" + numberOfCommonFiles +
                ", firstContributor='" + firstContributor +
                ", secondContributor='" + secondContributor;
    }
}
