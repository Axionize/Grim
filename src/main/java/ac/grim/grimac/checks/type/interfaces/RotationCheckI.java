package ac.grim.grimac.checks.type.interfaces;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.CheckType;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

public interface RotationCheckI extends AbstractCheck {

    default void process(final RotationUpdate rotationUpdate) {
    }
    @Override
    default int getMask() {
        return CheckType.ROTATION.getMask();
    }
}
