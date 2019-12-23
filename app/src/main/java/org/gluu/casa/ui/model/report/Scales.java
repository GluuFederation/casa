package org.gluu.casa.ui.model.report;
import java.util.List;
public class Scales
{
    private List<YAxes> yAxes;

    public void setYAxes(List<YAxes> yAxes){
        this.yAxes = yAxes;
    }
    public List<YAxes> getYAxes(){
        return this.yAxes;
    }
}

