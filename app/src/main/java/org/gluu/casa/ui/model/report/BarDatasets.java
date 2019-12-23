package org.gluu.casa.ui.model.report;
import java.util.List;
public class BarDatasets
{
    private String label;

    private List<Integer> data;

    private List<String> backgroundColor;

    private List<String> borderColor;

    private int borderWidth;

    public void setLabel(String label){
        this.label = label;
    }
    public String getLabel(){
        return this.label;
    }
    public void setData(List<Integer> data){
        this.data = data;
    }
    public List<Integer> getData(){
        return this.data;
    }
    public void setBackgroundColor(List<String> backgroundColor){
        this.backgroundColor = backgroundColor;
    }
    public List<String> getBackgroundColor(){
        return this.backgroundColor;
    }
    public void setBorderColor(List<String> borderColor){
        this.borderColor = borderColor;
    }
    public List<String> getBorderColor(){
        return this.borderColor;
    }
    public void setBorderWidth(int borderWidth){
        this.borderWidth = borderWidth;
    }
    public int getBorderWidth(){
        return this.borderWidth;
    }
}

