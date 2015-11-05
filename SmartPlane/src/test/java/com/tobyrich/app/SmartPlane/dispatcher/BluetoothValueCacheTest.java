package com.tobyrich.app.SmartPlane.dispatcher;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BluetoothValueCacheTest extends TestCase {


    @Inject
    private BluetoothValueCache classUnderTest;

    @Before
    public void setUp() throws Exception {
        // Perform injection
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);
    }

    @Test
    public void testIsMotorValueChange() throws Exception {
        // Given
        final short testValue1 = 0;
        final short testValue2 = 1;

        // When
        classUnderTest.isMotorValueChange(testValue1);
        final boolean result = classUnderTest.isMotorValueChange(testValue2);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsMotorValueNoChange() throws Exception {
        // Given
        final short testValue = 0;

        // When
        classUnderTest.isMotorValueChange(testValue);
        final boolean result = classUnderTest.isMotorValueChange(testValue);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsRudderValueChange() throws Exception {
        // Given
        final short testValue1 = 0;
        final short testValue2 = 1;

        // When
        classUnderTest.isRudderValueChange(testValue1);
        final boolean result = classUnderTest.isRudderValueChange(testValue2);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsRudderValueNoChange() throws Exception {
        // Given
        final short testValue = 0;

        // When
        classUnderTest.isRudderValueChange(testValue);
        final boolean result = classUnderTest.isRudderValueChange(testValue);

        // Then
        assertFalse(result);
    }
}