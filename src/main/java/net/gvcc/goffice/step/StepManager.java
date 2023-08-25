package net.gvcc.goffice.step;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class StepManager {

	@Getter(value = AccessLevel.PROTECTED)
	private IStepManagerPersistenceHandler persistenceHandler;

	@Setter
	@Getter(value = AccessLevel.PROTECTED)
	private String logComments;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public StepManager(IStepManagerPersistenceHandler persistenceHandler) {
		this.persistenceHandler = persistenceHandler;
	}

	/**
	 * Identifies a step that the StepManager procedure has to elaborate.
	 * <p>
	 * This method returns a value, as requested.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * sm.step(Steps.STEP_NOTIFY, Notification.class).check();
	 * </pre>
	 * 
	 * @param <T>
	 *            The class of the elaborated data
	 * @param step
	 *            The step to elaborate
	 * @param clazz
	 *            The class of the object which expected as result
	 * @return A value which type corresponding to the 'clazz' parameter
	 */
	public <T> SpecializedStep<T> step(AStepValueEnum step, Class<T> clazz) {
		return new SpecializedStep<T>(this, step);
	}

	/**
	 * Identifies a step that the StepManager procedure has to elaborate.
	 * <p>
	 * This method returns a value, as requested.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * sm.step(0x01, Notification.class).check();
	 * </pre>
	 * 
	 * 
	 * @param <T>
	 *            The class of the elaborated data
	 * @param bit
	 *            The value step to elaborate, as a bit notation
	 * @param clazz
	 *            The class of the object which expected as result
	 * @return A value which type corresponding to the 'clazz' parameter
	 */
	public <T> SpecializedStep<T> step(long bit, Class<T> clazz) {
		return new SpecializedStep<T>(this, bit);
	}

	/**
	 * Identifies a step that the StepManager procedure has to elaborate.
	 * <p>
	 * This method returns a list of values, as requested.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * sm.step(Steps.STEP_DMS_ALLEGATI, Collections.singletonList(DMSDocument.class)).check();
	 * </pre>
	 * 
	 * @param <T>
	 *            The class of the elaborated data
	 * @param step
	 *            The step to elaborate
	 * @param clazz
	 *            The class of the list objects which expected as result
	 * @return A list of values which type corresponding to the 'clazz' parameter
	 * 
	 * @see SpecializedStep step(long bit, List clazz)
	 */
	public <T> SpecializedStep<List<T>> step(AStepValueEnum step, List<Class<T>> clazz) {
		return new SpecializedStep<List<T>>(this, step);
	}

	/**
	 * Identifies a step that the StepManager procedure has to elaborate.
	 * <p>
	 * This method returns a list of values, as requested.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * sm.step(0x01, Collections.singletonList(DMSDocument.class)).check();
	 * </pre>
	 * 
	 * @param <T>
	 *            The class of the elaborated data
	 * @param bit
	 *            The value step to elaborate, as a bit notation
	 * @param clazz
	 *            The class of the list objects which expected as result
	 * @return A list of values which type corresponding to the 'clazz' parameter
	 * 
	 * @see SpecializedStep step(AStepValueEnum step, List clazz)
	 */
	public <T> SpecializedStep<List<T>> step(long bit, List<Class<T>> clazz) {
		return new SpecializedStep<List<T>>(this, bit);
	}

	/**
	 * Identifies a step that the StepManager procedure has to elaborate.
	 * <p>
	 * This method returns no value.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * sm.step(Steps.STEP_NOTIFICATION).check();
	 * </pre>
	 * 
	 * @param step
	 *            The step to elaborate
	 * @return
	 */
	public VoidStep step(AStepValueEnum step) {
		return new VoidStep(this, step);
	}

	/**
	 * Identifies a step that the StepManager procedure has to elaborate.
	 * <p>
	 * This method returns no value.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * sm.step(0x01).check();
	 * </pre>
	 * 
	 * @param bit
	 *            The value step to elaborate, as a bit notation
	 * @return
	 */
	public VoidStep step(long bit) {
		return new VoidStep(this, bit);
	}
}
