/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluators;

import java.util.ArrayList;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Igor
 */
public class ProArimaEvaluator extends GenericEvaluator{
    
    private ArrayList<Float> observ; //list with the VIEW_SIZE observations
    static Rengine re;
    static REXP resp;
    static int forecast = 5;
    
    //17/02/2016: constructor
    public ProArimaEvaluator(int viewsize){
        super(viewsize); //call father's constructor
        objname = "evaluators.ProArimaEvaluator";    //rewrite the name
        observ = new ArrayList<>();
        re = new Rengine(null, true, null);
        re.eval("require(forecast)");        
    }
    
    //17/02/2016: Igor
    /**
     * Return if the aging system identified if the factor is out of range between the thresholds.
     * @param load - the current system load.
     * @param upper_threshold - the current upper threshold.
     * @param lower_threshold - the current lower threshold.
     * @return 
     */
    @Override
    public boolean evaluate(float upper_threshold, float lower_threshold){        
        //gera_log(objname, "Main|AginFullEvaluator|evaluate: Aging = " + decision_load);
        if (counter >= VIEW_SIZE - 1){
            //test if the aging is out of the range between the thresholds
            if (decision_load > 80) { //test if we have a violation on the higher threshold after aply the aging
                high_alert = true; 
                low_alert = false; 
                return true;
            } else if (decision_load < 20){ //test if we have a violation on the lower threshold after aply the aging
                high_alert = false;
                low_alert = true;
                return true; 
            } else {
                high_alert = false;
                low_alert = false;
            }
        } else {
            counter++; //here, counter is used to define the observantions amount
        }
        return false;  
    }
    
    @Override
    public float computeLoad(float load){
        observ.add(load);
        double[] aux = new double[observ.size()];
        for (int i = 0; i < observ.size(); i++) {
            aux[i] = observ.get(i);
        }
        re.assign("y", aux);
        re.eval("fit=auto.arima(y)");
        resp = re.eval("f <- forecast(fit, h=" + forecast + ")");
        return (float) resp.asList().at(3).asDoubleArray()[4];
    }
    
        
    @Override
    public void reset(){
        super.reset();
        observ = new ArrayList<>();
    }
    
}
