package net.gvcc.goffice.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecializedStep<T> extends AbstractStep<T> {
	private static Logger LOGGER = LoggerFactory.getLogger(SpecializedStep.class);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private T value;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@FunctionalInterface
	public interface SpecializedWorker<T> extends BaseWorker {
		T doWork() throws Exception;
	}

	@FunctionalInterface
	public interface SpecializedWorker_Value<T> extends BaseWorker {
		T doWork(T value) throws Exception;
	}

	////////// ALWAYS WORKERS

	@FunctionalInterface
	public interface SpecializedAlwaysWorker<T> extends BaseWorker {
		T doWork() throws Exception;
	}

	@FunctionalInterface
	public interface SpecializedAlwaysWorker_Value<T> extends BaseWorker {
		T doWork(T value) throws Exception;
	}

	@FunctionalInterface
	public interface SpecializedAlwaysWorker_Value_Handler<T> extends BaseWorker {
		T doWork(T value, Handler handler) throws Exception;
	}

	@FunctionalInterface
	public interface SpecializedAlwaysWorker_Value_Handler_Error<T> extends BaseWorker {
		T doWork(T value, Handler handler, Exception error) throws Exception;
	}

	////////// ERROR WORKERS

	@FunctionalInterface
	public interface SpecializedErrorWorker_Error<T> extends BaseWorker {
		T doWork(Exception error) throws Exception;
	}

	@FunctionalInterface
	public interface SpecializedErrorWorker_Error_Value<T> extends BaseWorker {
		T doWork(Exception error, T value) throws Exception;
	}

	@FunctionalInterface
	public interface SpecializedErrorWorker_Error_Value_Handler<T> extends BaseWorker {
		T doWork(Exception error, T value, Handler handler) throws Exception;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected SpecializedStep(StepManager sm, AStepValueEnum step) {
		super(sm, step);
	}

	protected SpecializedStep(StepManager sm, long bit) {
		super(sm, bit);
	}

	// append INIT method/workers

	public SpecializedStep<T> init(T initValue) {
		this.value = initValue;
		return this;
	}

	public SpecializedStep<T> init(SpecializedWorker<T> worker) {
		addWorker(WorkerType.init, worker);
		return this;
	}

	// append EXEC workers

	public SpecializedStep<T> exec(SpecializedWorker<T> worker) {
		addWorker(WorkerType.exec, worker);
		return this;
	}

	public SpecializedStep<T> exec(SpecializedWorker_Value<T> worker) {
		addWorker(WorkerType.exec, worker);
		return this;
	}

	// append OTHERWISE workers

	public SpecializedStep<T> otherwise(SpecializedWorker<T> worker) {
		addWorker(WorkerType.otherwise, worker);
		return this;
	}

	public SpecializedStep<T> otherwise(SpecializedWorker_Value<T> worker) {
		addWorker(WorkerType.otherwise, worker);
		return this;
	}

	// append ALWAYS workers

	public SpecializedStep<T> always(SpecializedAlwaysWorker<T> worker) throws Exception {
		addWorker(WorkerType.always, worker);
		return this;
	}

	public SpecializedStep<T> always(SpecializedAlwaysWorker_Value<T> worker) throws Exception {
		addWorker(WorkerType.always, worker);
		return this;
	}

	public SpecializedStep<T> always(SpecializedAlwaysWorker_Value_Handler<T> worker) throws Exception {
		addWorker(WorkerType.always, worker);
		return this;
	}

	public SpecializedStep<T> always(SpecializedAlwaysWorker_Value_Handler_Error<T> worker) throws Exception {
		addWorker(WorkerType.always, worker);
		return this;
	}

	// append ERROR workers

	public SpecializedStep<T> whenError(SpecializedErrorWorker_Error<T> worker) throws Exception {
		addWorker(WorkerType.whenError, worker);
		return this;
	}

	public SpecializedStep<T> whenError(SpecializedErrorWorker_Error_Value<T> worker) throws Exception {
		addWorker(WorkerType.whenError, worker);
		return this;
	}

	public SpecializedStep<T> whenError(SpecializedErrorWorker_Error_Value_Handler<T> worker) throws Exception {
		addWorker(WorkerType.whenError, worker);
		return this;
	}

	public T check() throws Exception {
		super.internalCheck();
		return (T) value;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void runSpecializedWorker() throws Exception {
		runWorkerInit();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void runWorker(BaseWorker worker) throws Exception {
		LOGGER.debug("runWorker - START");

		if (LOGGER.isDebugEnabled()) {
			String workerClassName = worker.getClass().getName();
			LOGGER.debug("runWorker - worker type={}, class={}", getWorkerType(worker), workerClassName);
		}

		if (worker instanceof SpecializedWorker) {
			this.value = ((SpecializedWorker<T>) worker).doWork();
		} else if (worker instanceof SpecializedWorker_Value) {
			this.value = ((SpecializedWorker_Value<T>) worker).doWork(this.value);

			// ALWAYS
		} else if (worker instanceof SpecializedAlwaysWorker) {
			this.value = ((SpecializedAlwaysWorker<T>) worker).doWork();
		} else if (worker instanceof SpecializedAlwaysWorker_Value) {
			this.value = ((SpecializedAlwaysWorker_Value<T>) worker).doWork(this.value);
		} else if (worker instanceof SpecializedAlwaysWorker_Value_Handler) {
			this.value = ((SpecializedAlwaysWorker_Value_Handler<T>) worker).doWork(this.value, getLastExecutedHandler());
		} else if (worker instanceof SpecializedAlwaysWorker_Value_Handler_Error) {
			this.value = ((SpecializedAlwaysWorker_Value_Handler_Error<T>) worker).doWork(this.value, getLastExecutedHandler(), getError());

			// ERROR
		} else if (worker instanceof SpecializedErrorWorker_Error) {
			this.value = ((SpecializedErrorWorker_Error<T>) worker).doWork(getError());
		} else if (worker instanceof SpecializedErrorWorker_Error_Value) {
			this.value = ((SpecializedErrorWorker_Error_Value<T>) worker).doWork(getError(), this.value);
		} else if (worker instanceof SpecializedErrorWorker_Error_Value_Handler) {
			this.value = ((SpecializedErrorWorker_Error_Value_Handler<T>) worker).doWork(getError(), this.value, getLastExecutedHandler());

			// UNKNOWN
		} else {
			throw new RuntimeException("Internal server error (unhandled worker type: '".concat(getWorkerType(worker).name()).concat("')")); // it must never happen!
		}

		LOGGER.debug("runWorker - END");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void runWorkerInit() throws Exception {
		LOGGER.debug("runWorkerInit - START");

		BaseWorker worker = getWorkers().get(WorkerType.init);
		if (worker != null) {
			setLastExecutedHandler(Handler.INIT);
			runWorker(worker);
		}

		LOGGER.debug("runWorkerInit - END");
	}
}
