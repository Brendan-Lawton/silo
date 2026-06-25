package de.tum.bgu.msm.data.accessibility;

import de.tum.bgu.msm.models.ModelUpdateListener;

public interface CommutingTimeProbability extends ModelUpdateListener {
	/**
	 * @param minutes
	 * @param mode
	 * @return Presumably, this returns a (non-normalized) weight and not a (normalized) probability.
	 */
    float getCommutingTimeProbability(int minutes, String mode);
}
