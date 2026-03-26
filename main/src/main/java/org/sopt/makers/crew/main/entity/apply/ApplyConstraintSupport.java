package org.sopt.makers.crew.main.entity.apply;

public final class ApplyConstraintSupport {

	public static final String DUPLICATE_APPLY_CONSTRAINT_NAME = "uq_apply_meeting_user";

	private ApplyConstraintSupport() {
	}

	public static boolean isDuplicateApplyViolation(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			String message = current.getMessage();
			if (message != null && message.toLowerCase().contains(DUPLICATE_APPLY_CONSTRAINT_NAME)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}
}
