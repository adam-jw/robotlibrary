package au.org.teambacon.ftc.robot.velocityvortex;

import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.hardware.adafruit.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

import au.org.teambacon.ftc.component.AdafruitBNO055IMUComponent;
import au.org.teambacon.ftc.element.ButtonState;
import au.org.teambacon.ftc.element.Robot;
import au.org.teambacon.ftc.component.MotorComponent;
import au.org.teambacon.ftc.game.Alliance;

public abstract class VelocityVortex extends Robot {
    public static final boolean DEBUG = true;
    public static final boolean DRIVE_ENABLED = true;
    public static final boolean IMU_ENABLED = true;

    public static final double DRIVE_WHEEL_DIAMETER = 0.09814;
    public static final double DRIVE_GEAR_REDUCTION = 0.5;

    protected MotorComponent driveLeft;
    protected MotorComponent driveRight;

    protected AdafruitBNO055IMUComponent imu;

    @Override
    public void load() throws InterruptedException {
        if (DRIVE_ENABLED) {
            driveLeft = hardwareMap.getMotor("drive_left", MotorComponent.MotorType.NEVEREST60_ENCODER);
            driveRight = hardwareMap.getMotor("drive_right", MotorComponent.MotorType.NEVEREST60_ENCODER);

            driveLeft.setTicksPerMeter(DRIVE_WHEEL_DIAMETER, DRIVE_GEAR_REDUCTION);
            driveRight.setTicksPerMeter(DRIVE_WHEEL_DIAMETER, DRIVE_GEAR_REDUCTION);

            driveLeft.setDirection(DcMotorSimple.Direction.REVERSE);

            driveLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            driveRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            driveLeft.setTarget(0);
            driveRight.setTarget(0);
            driveLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            driveRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        if (IMU_ENABLED) {
            imu = hardwareMap.getImu("imu_robot");

            AdafruitBNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
            parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
            parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
            parameters.loggingEnabled = true;
            parameters.loggingTag = "IMU";
            parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
            imu.getImu().initialize(parameters);

            imu.getImu().startAccelerationIntegration(new Position(), new Velocity(), 1000);
        }

        if (DEBUG) {
            new Thread() {
                @Override
                public void run() {
                    while (!isStopRequested()) {
                        if (DRIVE_ENABLED) {
                            telemetry.set("D|DRIVE_L_POWER", "%f", driveLeft.getPower());
                            telemetry.set("D|DRIVE_L_MODE", "%s", driveLeft.getMode().toString());
                            if (driveLeft.getMotorType().hasEncoder()) {
                                telemetry.set("D|DRIVE_L_TARGET", "%d", driveLeft.getTarget());
                                telemetry.set("D|DRIVE_L_POS", "%d", driveLeft.getPosition());
                            }

                            telemetry.set("D|DRIVE_R_POWER", "%f", driveRight.getPower());
                            telemetry.set("D|DRIVE_R_MODE", "%s", driveRight.getMode().toString());
                            if (driveRight.getMotorType().hasEncoder()) {
                                telemetry.set("D|DRIVE_R_TARGET", "%d", driveRight.getTarget());
                                telemetry.set("D|DRIVE_R_POS", "%d", driveRight.getPosition());
                            }
                        }

                        if (IMU_ENABLED) {
                            telemetry.set("D|IMU_HEADING", "%f", imu.getHeading());
                        }
                    }
                }
            }.start();
        }

        while (!isStarted()) {
            gamepadUpdate();

            if (gamepad1.X == ButtonState.PRESSED)
                alliance = Alliance.BLUE;
            else if (gamepad1.B == ButtonState.PRESSED)
                alliance = Alliance.RED;

            telemetry.set("ALLIANCE", "%s", alliance != null ? alliance.toString() : "not set");
        }
    }
}