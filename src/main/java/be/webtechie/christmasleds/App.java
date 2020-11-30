package be.webtechie.christmasleds;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    private static final List<GpioPinPwmOutput> leds = new ArrayList<>();
    private static final Random rd = new Random();

    private static final int PWM_MAX = 100;

    public static void main(String[] args) {
        logger.info("Warming up...");

        try {
            // Initialize the GPIO controller
            final GpioController gpio = GpioFactory.getInstance();

            // you can optionally use these wiringPi methods to further customize the PWM generator
            // see: http://wiringpi.com/reference/raspberry-pi-specifics/
            Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
            Gpio.pwmSetRange(PWM_MAX);
            Gpio.pwmSetClock(500);

            // Initialize the led pins as a digital output pin with initial low state
            leds.add(gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_04, "LeftGreen"));    // Pin 16
            leds.add(gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_05, "LeftBlue"));     // Pin 18
            leds.add(gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_06, "LeftRed"));      // Pin 22
            leds.add(gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_10, "Top"));          // Pin 24
            leds.add(gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_11, "LedRightGreen"));// Pin 26
            leds.add(gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_31, "RightYellow"));  // Pin 28
            leds.add(gpio.provisionPwmOutputPin(RaspiPin.GPIO_26, "RightRed"));         // Pin 32
            logger.info("LEDs initialized");

            allFlash(5, 250);
            allOff();
            runLight(5, 100);
            allOff();
            allRandomFlash(20, 500);
            allOff();
            allFade(50, PWM_MAX / 50);
            allOff();
            fadeOneByOne(25, PWM_MAX / 50);
            allOff();

            // Shut down the GPIO controller
            gpio.shutdown();

            logger.info("Done");
        } catch (Exception ex) {
            logger.error("Error: {}", ex.getMessage());
        }
    }

    private static void allOff() {
        logger.info("All off");
        leds.forEach(l -> l.setPwm(0));
    }

    private static void allOn() {
        logger.info("All on");
        leds.forEach(l -> l.setPwm(PWM_MAX));
    }

    private static void allFlash(int repeat, int speed) {
        logger.info("All flash {} times at speed {}", repeat, speed);
        try {
            for (int i = 0; i < repeat; i++) {
                allOff();
                Thread.sleep(speed);
                allOn();
                Thread.sleep(speed);
            }
        } catch (Exception ex) {
            logger.error("Thread got interrupted");
        }
    }

    private static void runLight(int repeat, int speed) {
        logger.info("One by one on and then off {} times at speed {}", repeat, speed);
        try {
            for (int i = 0; i < repeat; i++) {
                for (GpioPinPwmOutput led : leds) {
                    led.setPwm(PWM_MAX);
                    Thread.sleep(speed);
                }
                for (GpioPinPwmOutput led : leds) {
                    led.setPwm(0);
                    Thread.sleep(speed);
                }
            }
        } catch (Exception ex) {
            logger.error("Thread got interrupted");
        }
    }

    private static void allRandomFlash(int repeat, int speed) {
        logger.info("All random flash {} times at speed {}", repeat, speed);
        try {
            for (int i = 0; i < repeat; i++) {
                for (GpioPinPwmOutput led : leds) {
                    if (rd.nextBoolean()) {
                        led.setPwm(PWM_MAX);
                    } else {
                        led.setPwm(0);
                    }
                }
                Thread.sleep(speed);
            }
        } catch (Exception ex) {
            logger.error("Thread got interrupted");
        }
    }

    private static void allFade(int speed, int fadeSteps) {
        logger.info("All fade at speed {} with steps of {}", speed, fadeSteps);
        try {
            logger.info("Fading all up");
            for (int fade = 0; fade <= PWM_MAX; fade += fadeSteps) {
                for (GpioPinPwmOutput led : leds) {
                    led.setPwm(fade);
                }
                Thread.sleep(speed);
            }
            logger.info("Fading all down");
            for (int fade = PWM_MAX; fade >= 0; fade -= fadeSteps) {
                for (GpioPinPwmOutput led : leds) {
                    led.setPwm(fade);
                }
                Thread.sleep(speed);
            }
        } catch (Exception ex) {
            logger.error("Thread got interrupted");
        }
    }

    private static void fadeOneByOne(int speed, int fadeSteps) {
        logger.info("Fade one by one at speed {} with steps of {}", speed, fadeSteps);
        try {
            for (GpioPinPwmOutput led : leds) {
                logger.info("Fading up {}", led.getPin());
                for (int fade = 0; fade <= PWM_MAX; fade += fadeSteps) {
                    led.setPwm(fade);
                    Thread.sleep(speed);
                }
            }
            for (GpioPinPwmOutput led : leds) {
                logger.info("Fading down {}", led.getPin());
                for (int fade = PWM_MAX; fade >= 0; fade -= fadeSteps) {
                    led.setPwm(fade);
                    Thread.sleep(speed);
                }
            }
        } catch (Exception ex) {
            logger.error("Thread got interrupted");
        }
    }
}
