package com.forairan.leap;

import java.io.File;
import java.lang.reflect.Method;
import net.minecraft.client.main.Main;
import org.apache.commons.io.FileUtils;

public class LeapInjector {

    public static void main(String[] args) throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase().startsWith("win");
        boolean forge = checkForForge();

        String arch = System.getProperty("sun.arch.data.model");
        String nativeDir = System.getProperty("java.library.path") + File.separator;

        System.out.println("## Leap: Injecting LeapManager...");
        LeapManager.instance = new LeapManager();

        if (LeapManager.instance.reflect()) {
            System.out.println("## Leap: Injection successful, " + (windows ? "copying native libraries" : "starting Minecraft") + "...");

            if (windows) {
                FileUtils.copyFile(new File(nativeDir + "Leap" + arch + ".dll"), new File(nativeDir + "Leap.dll"));
                FileUtils.copyFile(new File(nativeDir + "LeapJava" + arch + ".dll"), new File(nativeDir + "LeapJava.dll"));
                System.out.println("## Leap: Native libraries copied, starting Minecraft...");
            }

            LeapManager.instance.start();
        } else {
            System.out.println("!! Leap: Starting Minecraft without Leap support!");
        }

        if (false) { // Forge support disabled for release.
        //if (forge) {
            System.out.println("## Leap: Forge detected. Passing control to Forge instead of vanilla.");

            int tweakIndex = -1;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--tweakClass")) {
                    tweakIndex = i;
                    args[tweakIndex + 1] = "cpw.mods.fml.common.launcher.FMLTweaker";
                }
            }

            if (tweakIndex == -1) {
                // Now we have to copy the array, grumble grumble grumble
                String[] oldArgs = args;
                args = new String[args.length + 2];
                System.arraycopy(oldArgs, 0, args, 0, oldArgs.length);
                args[oldArgs.length] = "--tweakClass";
                args[oldArgs.length + 1] = "cpw.mods.fml.common.launcher.FMLTweaker";
            }

            Method m = Class.forName("net.minecraft.launchwrapper.Launch").getDeclaredMethod("main", String[].class);
            m.invoke(null, (String[]) args);
        } else {
            Main.main(args);
        }

        System.out.println("## Leap: Shutting down and cleaning up!");
        LeapManager.instance.stop();
    }

    private static boolean checkForForge() {
        try {
            Class.forName("net.minecraft.launchwrapper.Launch");
            Class.forName("cpw.mods.fml.common.launcher.FMLTweaker");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
