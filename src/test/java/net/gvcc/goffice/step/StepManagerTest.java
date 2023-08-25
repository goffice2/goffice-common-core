package net.gvcc.goffice.step;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestTemplate;

import net.gvcc.goffice.step.AbstractStep.Handler;

@SpringBootTest
@EnableAutoConfiguration
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = { RestTemplate.class }, loader = AnnotationConfigContextLoader.class)
public class StepManagerTest {
	private static Logger LOGGER = LoggerFactory.getLogger(StepManagerTest.class);

	enum TestStep implements AStepValueEnum {
		FIRST(0x01), //
		SECOND(0x02), //
		THIRD(0x04), //
		FOURTH(0x08);

		long bit;

		private TestStep(long bit) {
			this.bit = bit;
		}

		@Override
		public String getDescription() {
			return name();
		}

		@Override
		public long getValue() {
			return this.bit;
		}
	}

	// ======================================================================================================================== //

	private long storedBit = 0L;

	IStepManagerPersistenceHandler handler = new IStepManagerPersistenceHandler() {

		@Override
		public void store(long bit) {
			storedBit = bit;
		}

		@Override
		public long get() {
			return storedBit;
		}
	};

	private static class FakeException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public FakeException(String msg) {
			super(msg);
		}
	}

	// ======================================================================================================================== //
	@BeforeEach
	void reset() {
		storedBit = 0L;
	}

	@Test
	public void stepManagerCheckCallErrorTest() {
		LOGGER.info("stepManagerCheckCallErrorTest - START");

		final String exceptionAnnotation = "This error identifies a positive test result";
		final String unhandledSituationMessage = "If this exception was thrown, the test is not verified!";

		StepManager sm = new StepManager(handler);
		sm.setLogComments("TEST COMMENTS");

		try {
			// 'check()' called before others method
			try {
				sm.step(TestStep.FIRST) //
						.exec(() -> {
							LOGGER.info("exec");
						}) //
						.check() // error!!! check has to be the latest method to call
						.otherwise(() -> {
							throw new Exception("otherwise");
						}) //
						.always(() -> {
							LOGGER.info("always");
						});

				throw new Exception(unhandledSituationMessage);
			} catch (StepManagerException e) {
				LOGGER.error("stepManagerCheckCallErrorTest - =========== {}: {} ===========", exceptionAnnotation, e.getMessage());
				assertTrue(e.getMessage().startsWith("The method 'check()' "));
			}

			// 'check()' called twice
			try {
				sm.step(TestStep.FIRST) //
						.exec(() -> {
							LOGGER.info("exec");
						}) //
						.otherwise(() -> {
						}) //
						.check() //
						.check(); // error!!! check is called twice

				throw new Exception(unhandledSituationMessage);
			} catch (StepManagerException e) {
				LOGGER.error("stepManagerCheckCallErrorTest - =========== {}: {} ===========", exceptionAnnotation, e.getMessage());
				assertTrue(e.getMessage().startsWith("The method 'check()' "));
			}

			// worker added more time
			try {
				sm.step(TestStep.FIRST) //
						.exec(() -> {
							LOGGER.info("exec");
						}) //
						.exec(() -> {
							LOGGER.info("exec");
						}) //
						.otherwise(() -> {
						}) //
						.check();

				throw new Exception(unhandledSituationMessage);
			} catch (StepManagerException e) {
				LOGGER.error("stepManagerCheckCallErrorTest - =========== {}: {} ===========", exceptionAnnotation, e.getMessage());
				assertEquals("Worker type 'exec()' has already been defined!", e.getMessage());
			}

			// missing 'exec()' worker
			try {
				sm.step(TestStep.FIRST) //
						.otherwise(() -> {
						}) //
						.check();

				throw new Exception(unhandledSituationMessage);
			} catch (StepManagerException e) {
				LOGGER.error("stepManagerCheckCallErrorTest - =========== {}: {} ===========", exceptionAnnotation, e.getMessage());
				assertEquals("Worker type 'exec()' is not defined!", e.getMessage());
			}

			// throwing an exception without catching it (missing 'whenError()' method)
			final String fakeExceptionMessage = "This is a fake exception which identifies a positive test result";
			try {
				sm.step(TestStep.FIRST) //
						.exec(() -> {
							LOGGER.info("exec");
						}) //
						.otherwise(() -> {
							throw new FakeException(fakeExceptionMessage);
						}) //
						.check();

				throw new Exception(unhandledSituationMessage);
			} catch (FakeException e) {
				LOGGER.error("stepManagerCheckCallErrorTest - =========== {} ===========", e.getMessage());
				assertEquals(fakeExceptionMessage, e.getMessage());
			}
		} catch (Exception e) {
			LOGGER.error("stepManagerCheckCallErrorTest", e);
			fail(e);
		}

		LOGGER.info("stepManagerCheckCallErrorTest - END");
	}

	@Test
	public void stepManagerWithVoidStepsAndEnumTest() {
		LOGGER.info("stepManagerWithVoidStepsAndEnumTest - START");

		StepManager sm = new StepManager(handler);
		sm.setLogComments("TEST COMMENTS");

		try {
			sm.step(TestStep.FIRST) //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						LOGGER.info("otherwise");
					}) //
					.whenError((err, handler) -> {
						assertEquals(Handler.EXEC, handler);
						assertNotNull(err);
						assertEquals("exec", err.getMessage());
					}) //
					.always((handler) -> {
						LOGGER.info("always");
					}) //
					.check();

			sm.step(TestStep.FIRST) //
					.exec(() -> {
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError((err, handler) -> {
						assertEquals(Handler.OTHERWISE, handler);
						assertNotNull(err);
						assertEquals("otherwise", err.getMessage());
					}) //
					.always((handler) -> {
						LOGGER.info("always");
					}) //
					.check();
		} catch (Exception e) {
			LOGGER.error("stepManagerWithVoidStepsAndEnumTest", e);
			fail(e);
		}

		LOGGER.info("stepManagerWithVoidStepsAndEnumTest - END");
	}

	@Test
	public void stepManagerWithVoidStepsAndLongTest() {
		LOGGER.info("stepManagerWithVoidStepsAndLongTest - START");

		StepManager sm = new StepManager(handler);
		sm.setLogComments("TEST COMMENTS");

		try {
			sm.step(TestStep.FIRST.getValue()) //
					.exec(() -> {
						LOGGER.info("exec");
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError(err -> {
						throw err;
					}) //
					.always((handler) -> {
						LOGGER.info("always");
					}) //
					.check();

			sm.step(TestStep.FIRST.getValue()) //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						LOGGER.info("otherwise");
					}) //
					.whenError(err -> {
						throw err;
					}) //
					.always((handler, err) -> {
						LOGGER.info("always");
					}) //
					.check();

			sm.step(TestStep.FIRST.getValue()) //
					.exec(() -> {
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError((err) -> {
						assertNotNull(err);
						assertEquals("otherwise", err.getMessage());
					}) //
					.check();

			sm.step(TestStep.FIRST.getValue()) //
					.exec(() -> {
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError((err, handler) -> {
						assertEquals(Handler.OTHERWISE, handler);
						assertNotNull(err);
						assertEquals("otherwise", err.getMessage());
					}) //
					.check();
		} catch (Exception e) {
			LOGGER.error("stepManagerWithVoidStepsAndLongTest", e);
			fail(e);
		}

		LOGGER.info("stepManagerWithVoidStepsAndLongTest - END");
	}

	@Test
	public void stepManagerWithSpecializedStepsAndEnumTest() {
		LOGGER.info("stepManagerWithSpecializedStepsAndEnumTest - START");

		StepManager sm = new StepManager(handler);

		try {
			String result = sm.step(TestStep.FIRST, String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.check();
			assertEquals("exec", result);

			result = sm.step(TestStep.FIRST, String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.check();
			assertEquals("otherwise", result);

			result = sm.step(TestStep.FIRST, String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.always(() -> "always") //
					.check();
			assertEquals("always", result);

			// ============================================================= //

			result = sm.step(TestStep.SECOND, String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.always((value) -> {
						assertEquals("exec", value);
						return "always";
					}) //
					.check();
			assertEquals("always", result);

			result = sm.step(TestStep.SECOND, String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.always((value) -> {
						assertEquals("otherwise", value);
						return "always";
					}) //
					.check();
			assertEquals("always", result);

			// ============================================================= //

			result = sm.step(TestStep.THIRD, String.class) //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError(err -> {
						assertEquals("exec", err.getMessage());
						return "error-exec";
					}) //
					.check();
			assertEquals("error-exec", result);

			result = sm.step(TestStep.THIRD, String.class) //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError(err -> {
						assertEquals("exec", err.getMessage()); // "exec" expected because the previous elaboration thrown an exception
						return "error-exec";
					}) //
					.check();
			assertEquals("error-exec", result);

			// ============================================================= //

			result = sm.step(TestStep.THIRD, String.class) //
					.exec(() -> {
						return "exec";
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError((err, value) -> {
						assertEquals("exec", err.getMessage());
						return "error-exec";
					}) //
					.check();
			assertEquals("exec", result);

			result = sm.step(TestStep.THIRD, String.class) //
					.init("init") //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError((err, value) -> {
						assertEquals("init", value);
						assertEquals("otherwise", err.getMessage()); // "otherwise" expected because the previous elaboration was successfull
						return "error-otherwise";
					}) //
					.check();
			assertEquals("error-otherwise", result);

			result = sm.step(TestStep.THIRD, String.class) //
					.init("init") //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError((err, value, handler) -> {
						assertEquals("init", value);
						assertEquals(Handler.OTHERWISE, handler);
						assertEquals("otherwise", err.getMessage()); // "otherwise" expected because the previous elaboration was successfull
						return "error-otherwise";
					}) //
					.check();
			assertEquals("error-otherwise", result);

			// ============================================================= //

			result = sm.step(TestStep.FOURTH, String.class) //
					.init(() -> "init") //
					.exec(value -> {
						assertEquals("init", value);
						return value;
					}) //
					.otherwise(value -> {
						throw new Exception("otherwise");
					}) //
					.always((value, handler) -> {
						assertEquals("init", value);
						assertEquals(Handler.EXEC, handler);
						return value;
					}) //
					.check();
			assertEquals("init", result);

			result = sm.step(TestStep.FOURTH, String.class) //
					.init("init") //
					.exec(value -> {
						throw new Exception("exec");
					}) //
					.otherwise(value -> {
						assertEquals("init", value);
						return value;
					}) //
					.always((value, handler, error) -> {
						assertEquals("init", value);
						assertEquals(Handler.OTHERWISE, handler);
						assertNull(error);
						return value;
					}) //
					.check();
			assertEquals("init", result);
		} catch (Exception e) {
			LOGGER.error("stepManagerWithSpecializedStepsAndEnumTest", e);
			fail(e);
		}

		LOGGER.info("stepManagerWithSpecializedStepsAndEnumTest - END");
	}

	@Test
	public void stepManagerWithSpecializedStepsAndLongTest() {
		LOGGER.info("stepManagerWithSpecializedStepsAndLongTest - START");

		StepManager sm = new StepManager(handler);

		try {
			String result = sm.step(TestStep.FIRST.getValue(), String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.check();
			assertEquals("exec", result);

			result = sm.step(TestStep.FIRST.getValue(), String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.check();
			assertEquals("otherwise", result);

			result = sm.step(TestStep.FIRST.getValue(), String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.always(() -> "always") //
					.check();
			assertEquals("always", result);

			// ============================================================= //

			result = sm.step(TestStep.SECOND.getValue(), String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.always((value) -> {
						assertEquals("exec", value);
						return "always";
					}) //
					.check();
			assertEquals("always", result);

			result = sm.step(TestStep.SECOND.getValue(), String.class) //
					.exec(() -> "exec") //
					.otherwise(() -> "otherwise") //
					.whenError(err -> {
						throw err;
					}) //
					.always((value) -> {
						assertEquals("otherwise", value);
						return "always";
					}) //
					.check();
			assertEquals("always", result);

			// ============================================================= //

			result = sm.step(TestStep.THIRD.getValue(), String.class) //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError(err -> {
						assertEquals("exec", err.getMessage());
						return "error-exec";
					}) //
					.check();
			assertEquals("error-exec", result);

			result = sm.step(TestStep.THIRD.getValue(), String.class) //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError(err -> {
						assertEquals("exec", err.getMessage()); // "exec" expected because the previous elaboration thrown an exception
						return "error-exec";
					}) //
					.check();
			assertEquals("error-exec", result);

			// ============================================================= //

			result = sm.step(TestStep.THIRD.getValue(), String.class) //
					.exec(() -> {
						return "exec";
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError(err -> {
						assertEquals("exec", err.getMessage());
						return "error-exec";
					}) //
					.check();
			assertEquals("exec", result);

			result = sm.step(TestStep.THIRD.getValue(), String.class) //
					.exec(() -> {
						throw new Exception("exec");
					}) //
					.otherwise(() -> {
						throw new Exception("otherwise");
					}) //
					.whenError(err -> {
						assertEquals("otherwise", err.getMessage()); // "otherwise" expected because the previous elaboration was successfull
						return "error-otherwise";
					}) //
					.check();
			assertEquals("error-otherwise", result);

			// ============================================================= //

			result = sm.step(TestStep.FOURTH.getValue(), String.class) //
					.init(() -> "init") //
					.exec(value -> {
						assertEquals("init", value);
						return value;
					}) //
					.otherwise(value -> {
						throw new Exception("otherwise");
					}) //
					.always((value, handler) -> {
						assertEquals("init", value);
						assertEquals(Handler.EXEC, handler);
						return value;
					}) //
					.check();
			assertEquals("init", result);

			result = sm.step(TestStep.FOURTH.getValue(), String.class) //
					.init("init") //
					.exec(value -> {
						throw new Exception("exec");
					}) //
					.otherwise(value -> {
						assertEquals("init", value);
						return value;
					}) //
					.always((value, handler, error) -> {
						assertEquals("init", value);
						assertEquals(Handler.OTHERWISE, handler);
						assertNull(error);
						return value;
					}) //
					.check();
			assertEquals("init", result);
		} catch (Exception e) {
			LOGGER.error("stepManagerWithSpecializedStepsAndLongTest", e);
			fail(e);
		}

		LOGGER.info("stepManagerWithSpecializedStepsAndLongTest - END");
	}
}
