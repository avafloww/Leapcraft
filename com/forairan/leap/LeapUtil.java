package com.forairan.leap;

import com.leapmotion.leap.Vector;

public class LeapUtil {

    /**
     * Gets the delta between two vectors.
     *
     * @param newVector new vector
     * @param oldVector old vector
     * @return delta
     */
    public static Vector delta(Vector newVector, Vector oldVector) {
        return new Vector(newVector.getX() - oldVector.getX(),
                newVector.getY() - oldVector.getY(),
                newVector.getZ() - oldVector.getZ());
    }
}
