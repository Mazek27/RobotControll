package pl.mazek.robotcontroll.OpenCv.Color;

/**
 * Created by Mazek on 07.03.2017.
 */
public class Colors {

    public static double[] hsv2bgr(double[] hsv) {
        double red = 0, grn = 0, blu = 0;
        double i, f, p, q, t;
        double result[] = new double[3];
        double hue = hsv[0], sat = hsv[1], val = hsv[2];

        if(val==0) {
            red = 0;
            grn = 0;
            blu = 0;
        } else {
            hue/=60;
            i = Math.floor(hue);
            f = hue-i;
            p = val*(1-sat);
            q = val*(1-(sat*f));
            t = val*(1-(sat*(1-f)));
            if (i==0) {red=val; grn=t; blu=p;}
            else if (i==1) {red=q; grn=val; blu=p;}
            else if (i==2) {red=p; grn=val; blu=t;}
            else if (i==3) {red=p; grn=q; blu=val;}
            else if (i==4) {red=t; grn=p; blu=val;}
            else if (i==5) {red=val; grn=p; blu=q;}
        }
        result[0] = blu;
        result[1] = grn;
        result[2] = red;
        return result;
    }

    public static float[] bgr2hsv(double[] bgr){
        float hue, sat, val;
        float x, f, i;
        float result[] = new float[3];
        float red = (float) bgr[2], grn = (float) bgr[1], blu = (float) bgr[0];


        x = Math.min(Math.min(red, grn), blu);
        val = Math.max(Math.max(red, grn), blu);
        if (x == val){
            hue = 0;
            sat = 0;
        }
        else {
            f = (red == x) ? grn-blu : ((grn == x) ? blu-red : red-grn);
            i = (red == x) ? 3 : ((grn == x) ? 5 : 1);
            hue = ((i-f/(val-x))*60)%360;
            sat = ((val-x)/val);
        }
        result[0] = hue;
        result[1] = sat;
        result[2] = val;
        return result;
    }

    public static double[] bgr2hsvDouble(double[] bgr){
        double hue, sat, val;
        double x, f, i;
        double result[] = new double[3];
        double red =  bgr[2], grn = bgr[1], blu = bgr[0];


        x = Math.min(Math.min(red, grn), blu);
        val = Math.max(Math.max(red, grn), blu);
        if (x == val){
            hue = 0;
            sat = 0;
        }
        else {
            f = (red == x) ? grn-blu : ((grn == x) ? blu-red : red-grn);
            i = (red == x) ? 3 : ((grn == x) ? 5 : 1);
            hue = ((i-f/(val-x))*60)%360;
            sat = ((val-x)/val);
        }
        result[0] = hue;
        result[1] = sat;
        result[2] = val;
        return result;
    }

    public static double[] getColor(double[] hsv){
        switch (getColorIndex(hsv)){
            case 0 : return new double[]{255,255,255};//RED
            case 1 : return new double[]{0,125,255};//BLUE
            case 2 : return new double[]{0,255,255};//GREEN
            case 3 : return new double[]{0,255,0};//YELLOW
            case 4 : return new double[]{255,0,0};//ORANGE
            case 5 : return new double[]{0,0,255};//WHITE
            default: return new double[]{0,0,0};//BLACK
        }
    }

    public static double[] getColor(int hsv){
        switch (hsv){
            case 0 : return new double[]{255,255,255};//RED
            case 1 : return new double[]{0,125,255};//BLUE
            case 2 : return new double[]{0,255,255};//GREEN
            case 3 : return new double[]{0,255,0};//YELLOW
            case 4 : return new double[]{255,0,0};//ORANGE
            case 5 : return new double[]{0,0,255};//WHITE
            default: return new double[]{0,0,0};//BLACK
        }
    }

    public static byte getColorIndex(double[] hsv) {
        double H = hsv[0];
        double S = hsv[1];
        double V = hsv[2];

        if ((H < 11 || H > 290) && S > 0.7 && V > 0.1) {          //RED
            return 5;
        } else if (H > 180 && H < 255 && S > 0.6 && V > 0.1) {    //BLUE
            return 4;
        } else if (H > 75 && H < 150 && S > 0.6 && V > 0.1) {      //GREEN
            return 3;
        } else if (H > 45 && H < 75 && S > 0.80 && V > 0.1) {       //YELLOW
            return 2;
        } else if (H > 11 && H < 40 && S > 0.8 && V > 0.75) {      //ORANGE
            return 1;
        } else if (S < 0.15 && hsv[2] > 0.90) {                //WHITE
            return 0;
        } else//BLACK
            return 6;
    }

    public static String getColorName(double[] hsv) {
        double H = hsv[0];
        double S = hsv[1];
        double V = hsv[2];

        if ((H < 11 || H > 290) && S > 0.7 && V > 0.1) {          //RED
            return "RED";
        } else if (H > 180 && H < 255 && S > 0.6 && V > 0.1) {    //BLUE
            return "BLUE";
        } else if (H > 75 && H < 150 && S > 0.6 && V > 0.1) {      //GREEN
            return "GREEN";
        } else if (H > 45 && H < 75 && S > 0.80 && V > 0.1) {       //YELLOW
            return "YELLOW";
        } else if (H > 11 && H < 40 && S > 0.8 && V > 0.75) {      //ORANGE
            return "ORANGE";
        } else if (S < 0.15 && hsv[2] > 0.90) {                //WHITE
            return "WHITE";
        } else//BLACK
            return "BLACK";
    }
}