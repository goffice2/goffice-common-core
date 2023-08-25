package net.gvcc.goffice.step;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gvcc.goffice.step.AbstractStep.WorkerType;

/**
 * StepManager herlper to log operations
 *
 * <p>
 * The <code>StepLogger</code> class
 * </p>
 * <p>
 * Data: Jul 21, 2023
 * </p>
 * 
 * @author Renzo Poli
 */
public class StepLogger {
	private static Logger LOGGER = LoggerFactory.getLogger(StepLogger.class);

	@FunctionalInterface
	private interface Printer {
		void print(String msg1, String msg2, WorkerType workerType, String comments, AStepValueEnum step, long bit);
	}

	private static Map<String, Printer> PRINTERS = new HashMap<>();
	static {
		PRINTERS.put("COMMENTS_AND_STEP",
				(msg1, msg2, worker, comments, step, bit) -> LOGGER.info("[SM - {}] step -> {}, handler -> {}: {}...{}", comments, step.getDescription(), worker.name(), msg1, msg2));
		PRINTERS.put("COMMENTS_NO_STEP", (msg1, msg2, worker, comments, step, bit) -> LOGGER.info("[SM - {}] bit -> {}, handler -> {}: {}...{}", comments, bit, worker.name(), msg1, msg2));
		PRINTERS.put("COMMENTS", (msg1, msg2, worker, comments, step, bit) -> LOGGER.info("[SM] step -> {}, handler -> {}...{}", step.getDescription(), worker.name(), msg1, msg2));
		PRINTERS.put("NONE", (msg1, msg2, worker, comments, step, bit) -> LOGGER.info("[SM] bit -> {}, handler -> {}...{}", bit, worker.name(), msg1, msg2));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void print(String msg1, String msg2, WorkerType workerType, String comments, AStepValueEnum step, long bitToCheck) {
		String id;
		if (comments != null && step != null) {
			id = "COMMENTS_AND_STEP";
		} else if (comments != null) {
			id = "COMMENTS_NO_STEP";
		} else if (step != null) {
			id = "COMMENTS";
		} else {
			id = "NONE";
		}

		PRINTERS.get(id).print(msg1, msg2, workerType, comments, step, bitToCheck);
	}
}
