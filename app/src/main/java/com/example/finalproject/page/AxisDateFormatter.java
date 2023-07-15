package com.example.finalproject.page;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class AxisDateFormatter extends ValueFormatter{
    private final List<String> mValues;

    public String getFormattedValue(float value) {
        if(value>0 && mValues.size()>0){
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
            return sdf.format(new Date(Long.parseLong(mValues.get((int)value))));
        }else{
            return "";
        }

//        return value >= (float)0 ? (this.mValues.length > (int)value ? this.mValues[(int)value] : "") : "";
    }

    public AxisDateFormatter( List<String> mValues) {
        this.mValues = mValues;
    }
}
