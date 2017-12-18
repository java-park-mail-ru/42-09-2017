package ru.mail.park.mechanics.listeners;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.park.domain.Id;
import ru.mail.park.mechanics.WorldRunner;
import ru.mail.park.mechanics.domain.objects.body.GBody;

public class SensorListener implements ContactListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorListener.class);
    private final WorldRunner worldRunner;

    private static final Long CONTACT_SCORE = 5L;

    public SensorListener(WorldRunner worldRunner) {
        this.worldRunner = worldRunner;
    }

    @Override
    public void beginContact(Contact contact) {
        final int keyA = contact.getFixtureA().getFilterData().categoryBits;
        final int keyB = contact.getFixtureB().getFilterData().categoryBits;

        if (keyA == 0x0002 && keyB == 0x0002) {
            LOGGER.warn("CONTACT");
            worldRunner.setCalculation(false);
            return;
        }

        final Long playerIdA = ((GBody) contact.getFixtureA().getUserData()).getPlayerID();
        final Long playerIdB = ((GBody) contact.getFixtureB().getUserData()).getPlayerID();

        if (keyA == 0x0002 && playerIdB != null) {
            worldRunner.setScore(Id.of(playerIdB), CONTACT_SCORE);
        } else if (keyB == 0x0002 && playerIdA != null) {
            worldRunner.setScore(Id.of(playerIdA), CONTACT_SCORE);
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
