package org.gluu.casa.ui.model.report;
public class Ticks
{
    private boolean beginAtZero=true;

    private int padding;
    
    private int min=0;
    
    public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
	private int precision=0;

    public void setBeginAtZero(boolean beginAtZero){
        this.beginAtZero = beginAtZero;
    }
    public boolean getBeginAtZero(){
        return this.beginAtZero;
    }
    public void setPadding(int padding){
        this.padding = padding;
    }
    public int getPadding(){
        return this.padding;
    }
	public int getPrecision() {
		return precision;
	}
	public void setPrecision(int precision) {
		this.precision = precision;
	}
	
	
}

