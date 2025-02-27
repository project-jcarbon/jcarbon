package jcarbon.emissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import jcarbon.emissions.CarbonLocale;
import jcarbon.emissions.CarbonLocaleGenerator;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public class CarbonLocaleTest{
    private static final double USA_LOC = 396.875216;
    private static final double TWN_LOC = 532.763179;
    private static final double FRA_LOC = 55.000000;


    @Test
    public void computeLocale1(){
        CarbonLocaleGenerator result = new CarbonLocaleGenerator();
        List<String> enums  = result.parseList(List.of("ISO,Country,CarbonIntensity","ABW,Aruba,542.8195532899147", "TWN,Taiwan,124.0"));
        for(String e: enums){
            System.out.println(e);
        }
        // assertEquals("USA", result.getCountryName());
        // assertEquals(396.875216, result.getLocaleIntensity(), 0.0);
    }
    // @Test
    // public void computeLocale1(){
    //     CarbonLocale result = CarbonLocale.getDefault();
    //     assertEquals("USA", result.getCountryName());
    //     assertEquals(396.875216, result.getLocaleIntensity(), 0.0);
    // }

    // @Test
    // public void computeLocale2(){
    //     Locale fr = Locale.FRANCE;
    //     CarbonLocale result = CarbonLocale.fromLocale(fr);
    //     assertEquals("France", result.getCountryName());
    //     assertEquals(55.000000, result.getLocaleIntensity(), 0.0);
    // }

    // @Test
    // public void computeLocale3(){
    //     Locale fr = Locale.TAIWAN;
    //     CarbonLocale result = CarbonLocale.fromLocale(fr);
    //     assertEquals("Taiwan", result.getCountryName());
    //     assertEquals(532.763179, result.getLocaleIntensity(), 0.0);
    // }
}