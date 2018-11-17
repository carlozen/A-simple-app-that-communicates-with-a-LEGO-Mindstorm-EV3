package it.unive.dais.legodroid.lib.plugs;

import android.support.annotation.NonNull;
import android.util.Log;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Bytecode;
import it.unive.dais.legodroid.lib.comm.Const;
import it.unive.dais.legodroid.lib.util.Prelude;

import java.io.IOException;
import java.util.concurrent.Future;

public class TachoMotor extends Plug<EV3.OutputPort> implements AutoCloseable {
    private static final String TAG = Prelude.ReTAG("TachoMotor");

    public TachoMotor(@NonNull EV3.Api api, EV3.OutputPort port) {
        super(api, port);
    }

    public Future<Float> getPosition() throws IOException {
        Future<float[]> r = api.getSiValue(port.toByteAsRead(), Const.L_MOTOR, Const.L_MOTOR_DEGREE, 1);
        return api.execAsync(() -> r.get()[0]);
    }

    public void resetPosition() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_RESET);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
    }

    public boolean isStalled() {
        return false;
    }

    public void goToPositionRel(int amount) {
        // TODO
    }

    public void goToPositionAbs(int pos) {
        // TODO
    }

    public void setSpeed(int speed) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_SPEED);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) speed);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor speed set: %d", speed));
    }

    public void setPower(int power) throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_POWER);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter((byte) power);
        api.sendNoReply(bc);
        Log.d(TAG, String.format("motor power set: %d", power));
    }


    public void start() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_START);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        api.sendNoReply(bc);
        Log.d(TAG, "motor started");
    }

    public void brake() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STOP);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter(Const.BRAKE);
        api.sendNoReply(bc);
        Log.d(TAG, "motor brake");
    }

    public void stop() throws IOException {
        Bytecode bc = new Bytecode();
        bc.addOpCode(Const.OUTPUT_STOP);
        bc.addParameter(Const.LAYER_MASTER);
        bc.addParameter(port.toBitmask());
        bc.addParameter(Const.COAST);
        api.sendNoReply(bc);
        Log.d(TAG, "motor stop");
    }

    public boolean isMoving() { // TODO: questo non è un doppione con isStill()?
        return false;
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
