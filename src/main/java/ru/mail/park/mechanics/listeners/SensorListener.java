package ru.mail.park.mechanics.listeners;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.park.mechanics.WorldParser;

public class SensorListener implements ContactListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorListener.class);

    @Override
    public void beginContact(Contact contact) {
        int keyA = contact.getFixtureA().getFilterData().categoryBits;
        int keyB = contact.getFixtureB().getFilterData().categoryBits;

<<<<<<< HEAD
        if (keyA == 0x0002 && keyA == 0x0002) {
=======
        if (keyA == 0x0002 && keyB == 0x0002) {
>>>>>>> c17a9af5cb3b7b704c90df2051bd4225b18f5791
            LOGGER.warn("CONTACT");
            WorldParser.setCalculation(false);
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
