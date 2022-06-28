package com.arsinex.com.Exchange;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

public class CustomBarChartDataset extends BarDataSet {
    public CustomBarChartDataset(List<BarEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public int getColor(int index) {
        if(index == 0 || getEntryForIndex(index-1).getY() <= getEntryForIndex(index).getY()){
            return mColors.get(0); // index 0 is green
        } else {
            return mColors.get(1); // index 1 is red
        }
    }

}
