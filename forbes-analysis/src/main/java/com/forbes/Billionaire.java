package com.forbes;

public class Billionaire {
    private int rank;
    private String name;
    private double netWorth;
    private int age;
    private String country;
    private String source;
    private String industry;

    public Billionaire(int rank, String name, double netWorth, int age,
                       String country, String source, String industry) {
        this.rank = rank;
        this.name = name;
        this.netWorth = netWorth;
        this.age = age;
        this.country = country;
        this.source = source;
        this.industry = industry;
    }


    public int getRank() { return rank; }
    public String getName() { return name; }
    public double getNetWorth() { return netWorth; }
    public int getAge() { return age; }
    public String getCountry() { return country; }
    public String getSource() { return source; }
    public String getIndustry() { return industry; }

    @Override
    public String toString() {
        return String.format("%-4d %-40s %6.1f %3d %-20s %-30s %s",
                rank, name, netWorth, age, country, source, industry);
    }
}