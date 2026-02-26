package frc.robot.subsystems.drive;

public class PIDJson {
    private double dp;
    private double di;
    private double dd;
    private double dv;

    private double sp;
    private double si;
    private double sd;

    public double getDP() {return dp;}
    public double getDI() {return di;}
    public double getDD() {return dd;}
    public double getDV() {return dv;}

    public double getSP() {return sp;}
    public double getSI() {return si;}
    public double getSD() {return sd;}

    public PIDJson(double dp, double di, double dd, double dv, double sp, double si, double sd) {
        this.dp = dp;
        this.di = di;
        this.dd = dd;
        this.dv = dv;
        this.sp = sp;
        this.si = si;
        this.sd = sd;
    }
}
