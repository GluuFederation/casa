package org.gluu.casa.ui.model.report;

import java.util.HashMap;

public class BarChart
{
	
	
	private String type;

    private BarData data;

    private BarOptions options;

    public void setType(String type){
        this.type = type;
    }
    public String getType(){
        return this.type;
    }
    public void setData(BarData data){
        this.data = data;
    }
    public BarData getData(){
        return this.data;
    }
    public void setOptions(BarOptions options){
        this.options = options;
    }
    public BarOptions getOptions(){
        return this.options;
    }
}
