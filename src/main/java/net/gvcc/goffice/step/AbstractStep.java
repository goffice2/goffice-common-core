package net.gvcc.goffice.step;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 *
 * <p>
 * The <code>AbstractStep</code> class
 * </p>
 * <p>
 * Data: Jul 21, 2023
 * </p>
 * 
 * @author Renzo Poli
 * @version 1.0
 */
public abstract class AbstractStep<T> {
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractStep.class);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * The worker executed at the last operation
	 *
	 * <p>
	 * The <code>Handler</code> class
	 * </p>
	 * <p>
	 * Data: Jul 21, 2023
	 * </p>
	 * 
	 * @author Renzo Poli
	 */
	public enum Handler {
		/**
		 * When a 'init()' worker has been used
		 */
		INIT, //
		/**
		 * When a 'exec()' worker has been used
		 */
		EXEC, //
		/**
		 * When a 'otherwise()' worker has been used
		 */
		OTHERWISE //
	}

	/**
	 * The type of the step worker
	 *
	 * <p>
	 * The <code>WorkerType</code> class
	 * </p>
	 * <p>
	 * Data: Jul 21, 2023
	 * </p>
	 * 
	 * @author Renzo Poli
	 */
	protected enum WorkerType {
		/**
		 * Identified the 'init()' worker
		 */
		init, //
		/**
		 * Identified the 'exec()' worker
		 */
		exec, //
		/**
		 * Identified the 'otherwise()' worker
		 */
		otherwise, //
		/**
		 * Identified the 'whenError()' worker
		 */
		whenError, //
		/**
		 * Identified the 'always()' worker
		 */
		always; //
	}

	/**
	 * A gase class of workers
	 *
	 * <p>
	 * The <code>BaseWorker</code> class
	 * </p>
	 * <p>
	 * Data: Jul 21, 2023
	 * </p>
	 * 
	 * @author Renzo Poli
	 */
	protected interface BaseWorker {
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Runs a worker which is defined in a subclass
	 * 
	 * @throws Exception
	 *             An exception that identifies a configuration error or a runtime execution problem
	 */
	protected abstract void runSpecializedWorker() throws Exception;

	/**
	 * 
	 * @param worker
	 *            Runs a worker which is defined in a subclass
	 * @throws Exception
	 *             An exception that identifies a configuration error or a runtime execution problem
	 */
	protected abstract void runWorker(BaseWorker worker) throws Exception;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private StepManager sm;
	private long bitToCheck;
	private AStepValueEnum step;
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	private boolean checkPerformed = false; // true when check() was called
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	private Handler lastExecutedHandler;
	@Getter(value = AccessLevel.PUBLIC) // public!
	@Setter(value = AccessLevel.PROTECTED)
	private Exception error;

	@Getter(value = AccessLevel.PROTECTED)
	private Map<WorkerType, BaseWorker> workers = new HashMap<>();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates an instance of a step to elaborate
	 * 
	 * @param sm
	 *            The StepManager instance
	 * @param step
	 *            The step to elaborate
	 */
	protected AbstractStep(StepManager sm, AStepValueEnum step) {
		this(sm, step.getValue());
		this.step = step;
	}

	/**
	 * Creates an instance of a step to elaborate
	 * 
	 * @param sm
	 *            The StepManager instance
	 * @param bit
	 *            The step value to elaborate, as bit notation
	 */
	protected AbstractStep(StepManager sm, long bit) {
		this.sm = sm;
		this.bitToCheck = bit;
	}

	/**
	 * This method performs an internal check about the StepManager configuration
	 * 
	 * @throws Exception
	 *             An exception that identifies a configuration error or a runtime execution problem
	 */
	protected void internalCheck() throws Exception {
		LOGGER.debug("check - START");

		if (isCheckPerformed()) {
			throw new StepManagerException("The method 'check()' is called twice!");
		}

		setCheckPerformed(true);

		BaseWorker worker = workers.get(WorkerType.exec);
		if (worker == null) {
			throw new StepManagerException("Worker type '".concat(WorkerType.exec.name()).concat("()' is not defined!"));
		}

		try {
			runSpecializedWorker();

			if (!checkStep()) {
				runWorkerExec();
			} else {
				runWorkerOtherwise();
			}
		} catch (Exception e) {
			this.error = e;
		} finally {
			try {
				if (this.error != null) {
					runWorkerWhenError();
				}
			} finally {
				runWorkerAlways();
			}
		}

		if (this.error != null) { // means there was an error but no <whenError> worker was configured
			throw this.error;
		}

		LOGGER.debug("check - END");
	}

	/**
	 * Append a worker to the StepManager
	 * 
	 * @param type
	 *            The type of the worker {@link WorkerType}
	 * @param worker
	 *            The instance of the worker.
	 */
	protected void addWorker(WorkerType type, BaseWorker worker) {
		LOGGER.debug("addWorker - START");

		LOGGER.debug("addWorker - type={}, worker={}", type.name(), worker);

		if (isCheckPerformed()) {
			throw new StepManagerException("The method 'check()' was already called!");
		}

		if (workers.containsKey(type)) {
			throw new StepManagerException("Worker type '".concat(type.name()).concat("()' has already been defined!"));
		}

		workers.put(type, worker);

		LOGGER.debug("addWorker - END");
	}

	/**
	 * Retrieve the worker type of a worker instance.
	 * 
	 * @param worker
	 *            The instance of the worker.
	 * @return The worker type of the instance
	 */
	protected WorkerType getWorkerType(BaseWorker worker) {
		return this.workers.keySet().stream() //
				.filter(key -> this.workers.get(key) == worker) //
				.findFirst() //
				.get();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private boolean checkStep() {
		return (sm.getPersistenceHandler().get() & this.bitToCheck) != 0;
	}

	/**
	 * Starts the worker type: 'exec'
	 * 
	 * @throws Exception
	 */

	private void runWorkerExec() throws Exception {
		LOGGER.debug("runWorkerExec - START");

		BaseWorker worker = workers.get(WorkerType.exec);

		setLastExecutedHandler(Handler.EXEC);
		startWorker(worker, WorkerType.exec);

		IStepManagerPersistenceHandler persistenceHandler = sm.getPersistenceHandler();

		// aggiorno lo stato di elaborazione della domanda
		long newValue = persistenceHandler.get() | this.bitToCheck;
		persistenceHandler.store(newValue);

		LOGGER.debug("runWorkerExec - END");
	}

	/**
	 * Starts the worker type: 'otherwise'
	 * 
	 * @throws Exception
	 */
	private void runWorkerOtherwise() throws Exception {
		LOGGER.debug("runWorkerOtherwise - START");

		BaseWorker worker = workers.get(WorkerType.otherwise);
		if (worker != null) {
			setLastExecutedHandler(Handler.OTHERWISE);
			startWorker(worker, WorkerType.otherwise);
		}

		LOGGER.debug("runWorkerOtherwise - END");
	}

	/**
	 * Starts the worker type: 'whenError'
	 * 
	 * @throws Exception
	 */
	private void runWorkerWhenError() throws Exception {
		LOGGER.debug("runWorkerWhenError - START");

		BaseWorker worker = workers.get(WorkerType.whenError);
		if (worker != null) {
			startWorker(worker, WorkerType.whenError);
			setError(null); // error already handled by the <whenError> worker
		}

		LOGGER.debug("runWorkerWhenError - END");
	}

	/**
	 * Starts the worker type: 'whenError'
	 * 
	 * 
	 * @throws Exception
	 */
	private void runWorkerAlways() throws Exception {
		LOGGER.debug("runWorkerAlways - START");

		BaseWorker worker = workers.get(WorkerType.always);
		if (worker != null) {
			startWorker(worker, WorkerType.always);
		}

		LOGGER.debug("runWorkerAlways - END");
	}

	/**
	 * Starts a generic worker
	 * 
	 * 
	 * @param worker
	 *            The instance of the worker
	 * @param workerType
	 *            The type of the worker
	 * @throws Exception
	 */
	private void startWorker(BaseWorker worker, WorkerType workerType) throws Exception {
		LOGGER.debug("startWorker - START");

		final String comments = StringUtils.trimToNull(sm.getLogComments());
		final String msg = "executing step";

		StepLogger.print(msg, "", workerType, comments, step, bitToCheck);

		runWorker(worker);

		StepLogger.print(msg, "DONE", workerType, comments, step, bitToCheck);

		LOGGER.debug("startWorker - END");
	}
}
