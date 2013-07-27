package com.forairan.leap;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import static com.leapmotion.leap.Gesture.Type.TYPE_KEY_TAP;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;
import java.lang.reflect.Method;
import net.minecraft.src.Minecraft;

/**
 * LeapManager manages the Leap Motion Controller in the Leapcraft mod.
 */
public class LeapManager {

    public static LeapManager instance;
    private LeapListener listener;
    private Controller controller;
    private Frame frame;
    private Method mcClickMouseMethod;

    public Minecraft getMinecraft() {
        return Minecraft.getMinecraft();
    }

    /**
     * Reflects some necessary fields for the Leap to work properly.
     *
     * @return true on success
     */
    public boolean reflect() {
        // Attempt to retrieve the methods we need
        try {
            // Unobfuscated
            mcClickMouseMethod = Minecraft.class.getDeclaredMethod("clickMouse", int.class);

            System.out.println("## Leap: Non-obfuscated environment detected. Using non-obfuscated methods.");
        } catch (NoSuchMethodException ex) {
            try {
                mcClickMouseMethod = Minecraft.class.getDeclaredMethod("c", int.class);
            } catch (NoSuchMethodException ex2) {
                System.out.println("!! Leap: Error: Can't reflect non-obfuscated OR obfuscated methods!");
                System.out.println("!! Leap: This likely means that (1) Leapcraft is outdated, or (2) Leapcraft does not yet support this Minecraft version.");
                System.out.println("!! Leap: Leap support will not work.");
                return false;
            }

            System.out.println("## Leap: Obfuscated environment detected. Using obfuscated methods.");
        }

        // Change method accessibility
        mcClickMouseMethod.setAccessible(true);

        return true;
    }

    /**
     * Initializes LeapManager and the Leap.
     */
    public void start() {
        System.out.println("## Leap: Starting LeapManager.");

        this.controller = new Controller();
        this.listener = new LeapListener(this);
        this.frame = controller.frame();

        this.controller.addListener(this.listener);

        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);

        if (controller.config().setFloat("Gesture.Swipe.MinLength", 10.0f)
                && controller.config().setFloat("Gesture.Swipe.MinVelocity", 50.0f)
                && controller.config().setFloat("Gesture.KeyTap.MinDownVelocity", 20.0f)
                && controller.config().setFloat("Gesture.ScreenTap.MinForwardVelocity", 10.0f)) {
            System.out.println("## Leap: Updated device configuration.");
            controller.config().save();
        }
    }

    /**
     * Called from LeapListener when the Leap has a new frame.
     *
     * @param frame the new frame
     */
    public void tick(Frame frame) {
        boolean hadGestureThisFrame = !frame.gestures().empty();
        this.frame = frame;

        if (getMinecraft() == null || !getMinecraft().inGameHasFocus || getMinecraft().thePlayer == null) {
            return;
        }

        for (Gesture g : frame.gestures()) {
            if (g.state() != Gesture.State.STATE_STOP) {
                continue;
            }

            switch (g.type()) {
                case TYPE_KEY_TAP:
                    handleKeyTap(new KeyTapGesture(g));
                    break;
            }
        }

        if (!hadGestureThisFrame) {
            int fingerCount = getFingerCount();

            // Movement check
            for (Finger finger : frame.fingers()) {
                Finger lastFinger = controller.frame(1).finger(finger.id());
                Finger olderFinger = controller.frame(4).finger(finger.id());

                if (lastFinger.isValid()) {
                    boolean cancelled = false;

                    if (olderFinger.isValid()) {
                        Vector olderDelta = LeapUtil.delta(finger.stabilizedTipPosition(), olderFinger.stabilizedTipPosition());

                        if (olderDelta.getY() <= 15 && olderDelta.getY() >= -15
                                && olderDelta.getX() <= 1 && olderDelta.getX() >= 1
                                && olderDelta.getZ() <= 1 && olderDelta.getZ() >= -1) {
                            cancelled = true;
                        }
                    }

                    if (!cancelled) {
                        Vector delta = LeapUtil.delta(finger.stabilizedTipPosition(), lastFinger.stabilizedTipPosition());
                        float multiplier = ((float) getMinecraft().gameSettings.mouseSensitivity * 15.0f) / (float) fingerCount;
                        getMinecraft().thePlayer.setAngles(delta.getX() * multiplier, delta.getY() * multiplier);
                    }
                }
            }

            // Touchzone (block break) check
            Finger frontmost = frame.fingers().frontmost();

            if (frontmost.isValid()) {
                if (frontmost.touchZone() == Pointable.Zone.ZONE_TOUCHING) {
                    getMinecraft().gameSettings.keyBindAttack.pressed = true;
                } else {
                    getMinecraft().gameSettings.keyBindAttack.pressed = false;
                }
            }
        }
    }

    /**
     * Called from {@link Minecraft#shutdownMinecraftApplet} when Minecraft is
     * shutting down.
     */
    public void stop() {
        controller.delete();
    }

    /**
     * Gets the current Leap framerate.
     *
     * @return current Leap framerate
     */
    public float getFPS() {
        if (frame == null || !isConnected()) {
            return 0.0f;
        } else {
            return frame.currentFramesPerSecond();
        }
    }

    /**
     * Gets the amount of hands in the current frame.
     *
     * @return amount of hands
     */
    public int getHandCount() {
        if (frame == null || !isConnected()) {
            return 0;
        } else {
            return frame.hands().count();
        }
    }

    /**
     * Gets the amount of fingers in the current frame.
     *
     * @return amount of fingers
     */
    public int getFingerCount() {
        if (frame == null || !isConnected()) {
            return 0;
        } else {
            return frame.fingers().count();
        }
    }

    /**
     * Returns the current connection state of the Leap.
     *
     * @return true if the Leap is connected
     */
    public boolean isConnected() {
        return controller.isConnected();
    }

    /**
     * Handles a key tap gesture.
     *
     * @param g the KeyTapGesture to use
     */
    private void handleKeyTap(KeyTapGesture g) {
        try {
            mcClickMouseMethod.invoke(getMinecraft(), 2);
        } catch (Exception ex) {
            System.out.println("!! Leap: Error invoking Minecraft method!");
            ex.printStackTrace();
        }
    }
}
