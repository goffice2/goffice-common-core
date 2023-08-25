package net.gvcc.goffice.step;

/**
 * An interface which can halp you to define StepManager steps more esasily.
 * <p>
 * You can create a custom enumeration, implement this interface, instead of use a "long value" directly.
 * <p>
 * Example:
 * 
 * <pre>
 *	private enum MySteps implements AStepValueEnum {
 *		MY_STEP_1(0x01), //
 *		MY_STEP_2(0x02); //
 *
 *		long bit;
 *
 *		private Steps(long bit) {
 *			this.bit = bit;
 *		}
 *
 *		&#64;Override
 *		public String getDescription() {
 *			return name();
 *		}
 *
 *		&#64;Override
 *		public long getValue() {
 *			return this.bit;
 *		}
 *	};
 * 
 * 	stepManager.step(MySteps.MY_STEP_1);
 * 	// instead of:
 * 	stepManager.step(0x01)
 * </pre>
 * 
 * TIP: when you use the enumeration, the logger is more readable too.
 * <p>
 * The <code>AStepValueEnum</code> class
 * </p>
 * <p>
 * Data: Jul 21, 2023
 * </p>
 * 
 * @author Renzo Poli
 */
public interface AStepValueEnum {
	/**
	 * Description of a step to execute
	 * 
	 * @return The description as a text
	 */
	String getDescription();

	/**
	 * Bit value of the step to execute
	 * 
	 * @return A value of the step as bit notation
	 */
	long getValue();
}
