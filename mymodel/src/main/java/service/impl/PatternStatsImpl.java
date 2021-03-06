package service.impl;

import com.myproject.mymodel.domain.Pivot;
import service.PatternStats;

import java.util.List;

public class PatternStatsImpl implements PatternStats {

    @Override
    public void statsofGainExtension(List<Pivot> pivotListForStats) {
        int totalBreakOut=0;
        int case25=0;
        int case50=0;
        int case70=0;
        int case100=0;
        int case140=0;
        int case200=0;

        int trigR70=0;
        int trigR100=0;
        int trigR140=0;
        for (Pivot pivot:pivotListForStats){
            if(pivot.getScratches().size()<=2){
                System.out.println("Pivot of nomatch: "+pivot.toString());
                continue;
            }
            if(pivot.getScratches().get(1).getStatus()>0){
                if(pivot.getScratches().get(pivot.getScratches().size()-1).getLow()<pivot.getScratches().get(1).getLow()){
                    totalBreakOut++;
                }
            }else {
                if(pivot.getScratches().get(pivot.getScratches().size()-1).getHigh()>pivot.getScratches().get(1).getHigh()){
                    totalBreakOut++;
                }
            }
            int maxLength=0;
            for(int n=2;n<pivot.getScratches().size();n++){
                if(pivot.getScratches().get(n).getLength()>maxLength){maxLength=pivot.getScratches().get(n).getLength();}
            }
            float maxratio=(float)maxLength/pivot.getLength();
            float triggarRatio=(float)pivot.getScratches().get(1).getLength()/pivot.getScratches().get(0).getLength();
            if(maxratio<=0.25){
                /*System.out.println("The noticeable pivot is "+pivot.toString());
                System.out.println("The maxratio= "+maxratio);*/
                case25++;
            }else if(maxratio>0.25 && maxratio<=0.5){
                case50++;
            }else if(maxratio>0.5 && maxratio<=0.7){
                case70++;
            }else if(maxratio>0.7 && maxratio<=1){
                case100++;
            }else if(maxratio>1 && maxratio<=1.4){
                case140++;
            }else if(maxratio>1.4){

                case200++;
            }

            if(triggarRatio>=0.7 && triggarRatio<1){
                trigR70++;
            }else if(triggarRatio>=1 && triggarRatio<1.4){
                trigR100++;
            }else if(triggarRatio>=1.4){
                //System.out.println("The noticeable pivot is "+pivot.toString());
                trigR140++;
            }
        }
        System.out.println("Total Breakout= "+ totalBreakOut);
        System.out.println("maxratio<=0.25: "+case25);
        System.out.println("maxratio>0.25 && maxratio<=0.5: "+case50);
        System.out.println("maxratio>0.5 && maxratio<=0.7: "+case70);
        System.out.println("maxratio>0.7 && maxratio<=1: "+case100);
        System.out.println("maxratio>1 && maxratio<=1.4: "+case140);
        System.out.println("maxratio>1.4: "+case200);

        System.out.println("triggarRatio>=0.7 && triggarRatio<1 is: "+trigR70);
        System.out.println("triggarRatio>=1 && triggarRatio<1.4 is: "+trigR100);
        System.out.println("triggarRatio>=1.4 is: "+trigR140);
    }
}
