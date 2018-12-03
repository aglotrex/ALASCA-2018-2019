package RequestDispatcher.DispatcherDataVm;

import Request.RequestIP;

import org.apache.commons.math3.util.Pair;

public class DataRequest {

    private DataReqGene Generator;
    private DataVM VM;
    private boolean notification;
    private long charge;
    private long evoie;

    public DataRequest(DataReqGene Generator, DataVM VM, boolean notif, long charge  ){
        this.Generator = Generator;
        this.VM =VM;
        this.notification = notif;
        this.charge = charge;
        this.evoie = System.currentTimeMillis();
        this.VM.addCharge(charge, 0);
    }

    public Pair<Long,Long> reception(RequestIP r){

        if (notification) {
            try {
                Generator.notification(r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long reception = System.currentTimeMillis();

        VM.addCharge(-charge,reception);

        return new Pair<>(reception,reception-evoie);
    }



}
