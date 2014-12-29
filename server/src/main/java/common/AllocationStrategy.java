package common;

import java.io.Serializable;

public enum AllocationStrategy implements Serializable {
	RANDOM, SAME_ECU, DIFF_ECU, SPECIFIC, MAX_MEMORY, MAX_CPU, MAX_STORAGE
}
