package org.gluu.casa.ui.model.report;
import java.util.ArrayList;
import java.util.List;
public class BarData
{
    private List<String> labels;

    private List<BarDatasets> datasets;

    public void setLabels(List<String> labels){
        this.labels = labels;
    }
    public List<String> getLabels(){
        return this.labels;
    }
    public void setDatasets(List<BarDatasets> datasets){
        this.datasets = datasets;
    }
    public List<BarDatasets> getDatasets(){
        return this.datasets;
    }
}

