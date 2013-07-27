package com.forairan.leap;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;

public class LeapListener extends Listener {

    private LeapManager manager;

    public LeapListener(LeapManager leapManager) {
        super();
        this.manager = leapManager;
    }
    
    @Override
    public void onInit(Controller controller) {
        System.out.println("## Leap: Initialized.");
    }
    
    @Override
    public void onConnect(Controller controller) {
        System.out.println("## Leap: Device connected. Current devices = " + controller.devices().count() + ".");
    }
    
    @Override
    public void onDisconnect(Controller controller) {
        System.out.println("## Leap: Device disconnected. Current devices = " + controller.devices().count() + ".");
    }
    
    @Override
    public void onExit(Controller controller) {
        System.out.println("## Leap: Exiting.");
    }
    
    @Override
    public void onFrame(Controller controller) {
        manager.tick(controller.frame());
    }
}
