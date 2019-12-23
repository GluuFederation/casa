package org.gluu.casa.ui.model.report;
public class BarOptions
{
	private boolean responsive = true;
    private Scales scales;

    public void setScales(Scales scales){
        this.scales = scales;
    }
    public Scales getScales(){
        return this.scales;
    }
	public boolean isResponsive() {
		return responsive;
	}
	public void setResponsive(boolean responsive) {
		this.responsive = responsive;
	}
}

