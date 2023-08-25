package net.gvcc.goffice.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoidStep extends AbstractStep<Void> {
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractStep.class);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@FunctionalInterface
	public interface VoidWorker extends BaseWorker {
		void doWork() throws Exception;
	}

	////////// ALWAYS WORKERS

	@FunctionalInterface
	public interface VoidAlwaysWorker extends BaseWorker {
		void doWork() throws Exception;
	}

	@FunctionalInterface
	public interface VoidAlwaysWorker_Handler extends BaseWorker {
		void doWork(Handler handler) throws Exception;
	}

	@FunctionalInterface
	public interface VoidAlwaysWorker_Handler_Error extends BaseWorker {
		void doWork(Handler handler, Exception error) throws Exception;
	}

	////////// ERROR WORKERS

	public interface VoidErrorWorker_Error extends BaseWorker {
		void doWork(Exception error) throws Exception;
	}

	public interface VoidErrorWorker_Error_Handler extends BaseWorker {
		void doWork(Exception error, Handler handler) throws Exception;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected VoidStep(StepManager sm, AStepValueEnum step) {
		super(sm, step);
	}

	protected VoidStep(StepManager sm, long bit) {
		super(sm, bit);
	}

	// append EXEC workers

	public VoidStep exec(VoidWorker worker) {
		addWorker(WorkerType.exec, worker);
		return this;
	}

	// append OTHERWISE workers

	public VoidStep otherwise(VoidWorker worker) {
		addWorker(WorkerType.otherwise, worker);
		return this;
	}

	// append ALWAYS workers

	public VoidStep always(VoidAlwaysWorker worker) throws Exception {
		addWorker(WorkerType.always, worker);
		return this;
	}

	public VoidStep always(VoidAlwaysWorker_Handler worker) throws Exception {
		addWorker(WorkerType.always, worker);
		return this;
	}

	public VoidStep always(VoidAlwaysWorker_Handler_Error worker) throws Exception {
		addWorker(WorkerType.always, worker);
		return this;
	}

	// append ERROR workers

	public VoidStep whenError(VoidErrorWorker_Error worker) throws Exception {
		addWorker(WorkerType.whenError, worker);
		return this;
	}

	public VoidStep whenError(VoidErrorWorker_Error_Handler worker) throws Exception {
		addWorker(WorkerType.whenError, worker);
		return this;
	}

	public VoidStep check() throws Exception {
		super.internalCheck();
		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void runSpecializedWorker() {
		// nothing to do
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void runWorker(BaseWorker worker) throws Exception {
		LOGGER.debug("runGenericWorker - START");

		String workerClassName = worker.getClass().getName();
		LOGGER.debug("runGenericWorker - worker class={}", workerClassName);

		if (worker instanceof VoidWorker) {
			((VoidWorker) worker).doWork();
			// ALWAYS
		} else if (worker instanceof VoidAlwaysWorker) {
			((VoidAlwaysWorker) worker).doWork();
		} else if (worker instanceof VoidAlwaysWorker_Handler) {
			((VoidAlwaysWorker_Handler) worker).doWork(getLastExecutedHandler());
		} else if (worker instanceof VoidAlwaysWorker_Handler_Error) {
			((VoidAlwaysWorker_Handler_Error) worker).doWork(getLastExecutedHandler(), getError());
			// ERROR
		} else if (worker instanceof VoidErrorWorker_Error) {
			((VoidErrorWorker_Error) worker).doWork(getError());
		} else if (worker instanceof VoidErrorWorker_Error_Handler) {
			((VoidErrorWorker_Error_Handler) worker).doWork(getError(), getLastExecutedHandler());
			// UNKNOWN
		} else {
			throw new RuntimeException("Internal server error (unhandled worker type: '".concat(workerClassName).concat("')"));
		}

		LOGGER.debug("runGenericWorker - END");
	}
}
