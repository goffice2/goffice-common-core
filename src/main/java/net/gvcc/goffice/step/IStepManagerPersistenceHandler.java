package net.gvcc.goffice.step;

/**
 * This interface has to be used to perform the current bit value storate.
 * 
 * The StepManager uses this interface to retrieve the current bit value and to store the new bit value at the end of the step elaboration.
 *
 * <p>
 * The <code>IStepManagerPersistenceHandler</code> class
 * </p>
 * <p>
 * Data: Jul 21, 2023
 * </p>
 * 
 * @author Renzo Poli
 */
public interface IStepManagerPersistenceHandler {
	/**
	 * Retrieve the current value of bit/step from a repository (e.g.: a JPA repository)
	 * 
	 * @return the current value
	 */
	long get();

	/**
	 * This method is called each time the system have to store (e.g: JPA repository) the new value of bit/step.
	 * 
	 * @param bit
	 */
	void store(long bit);

}
