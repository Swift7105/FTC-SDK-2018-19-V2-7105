/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


@Autonomous(name="Pushbot: Gyroautodouble", group="Pushbot")
//@Disabled
public class Gyroautodouble extends LinearOpMode {

    /* Declare OpMode members. */
    PrototypeHWSetup robot = new PrototypeHWSetup();   // Use a Pushbot's hardware
    private ElapsedTime     runtime = new ElapsedTime();

    BNO055IMU   imu;
    Orientation lastAngles  = new Orientation();
    double      globalAngle = 0;
    BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    private static final String VUFORIA_KEY = "AdaTTxT/////AAAAGZa0cW5OPUfNsaDTa3hXTXAVZzLmUtlM2vw1ea9hOvyg+YBpLFoEhYaqm5pdAUXUUXWi+vfLC8lTsa/FPWfqusPU4PTqqLE0Ojc6DWvH7NEI931kMAEfVBLxL+t5nyQDItuMEHfCRdCsLgbE71SPnxENwtX+3xP6p+hSw8Mx1rUjcIFug83wbhOYq2ERrbCqxWnbg63bjSdXLZofSZZlRvGKlDHvJKdKSLCGel5Ck6D0QBscg9CQExIFpT3n3OXdHKoTa+DgsF7y6TCEFeVEE1eVkxBr/mDJwGVGPllJAJVudtqt4MBNQsLAPWzUZTG6AOe2bgmjO/I5io5fByEbdknaaDUMMhBxnTLf3fGPh6lF";

    private VuforiaLocalizer vuforia;

    private TFObjectDetector tfod;

    private boolean loop = TRUE;

    private int cubepos = -1;

    double timerreset = 0;

    @Override public void runOpMode() {

        robot.init(hardwareMap);

        parameters.mode = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = false;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        telemetry.addData("Mode", "calibrating...");
        telemetry.update();

        if (tfod != null) {
            tfod.activate();
        }

        telemetry.addData("globalangle2", globalAngle);
        telemetry.update();

        waitForStart();

        telemetry.addData("globalangle2", globalAngle);
        telemetry.update();
        robot.arm2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);



        timerreset = getRuntime();
        robot.lift.setPower(-1);
        sleep(2100);

        while(getRuntime() - timerreset < 2.1 && !isStopRequested() && opModeIsActive()){

        }
        robot.lift.setPower(-.1);

        CameraDevice.getInstance().setFlashTorchMode(true) ;

        sleep(200);
        while (loop == TRUE && !isStopRequested() && opModeIsActive()) {
            if (tfod != null) {
                // getUpdatedRecognitions() will return null if no new information is available since
                // the last time that call was made.
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Object Detected", updatedRecognitions.size());

                    if (updatedRecognitions.size() > 0) {
                        int goldMineralX    = -1;
                        int silverMineral1X = -1;
                        int silverMineral2X = -1;
                        for (Recognition recognition : updatedRecognitions) {
                            if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                if (Math.abs(recognition.getHeight() - 140) < 60) {
                                    if (Math.abs(recognition.getWidth() - 140) < 50) {
                                        goldMineralX = (int) recognition.getTop();
                                        telemetry.addData("Gold mineral confidence", recognition.getConfidence());
                                        telemetry.addData("Gold mineral hiht", recognition.getHeight());
                                        telemetry.addData("Gold mineral width", recognition.getWidth());
                                        telemetry.addData("Gold mineral lable", recognition.getLabel());
                                    }

                                }
                            }

                            else if (silverMineral1X == -1) {
                                silverMineral1X = (int) recognition.getTop();
                            }
                            else {
                                silverMineral2X = (int) recognition.getLeft();
                            }
                        }

                        if (goldMineralX != -1) {
                            if (goldMineralX > 650) {
                                telemetry.addData("Gold Mineral Position", "Center");
                                loop = FALSE;
                                cubepos = 1;
                            } else {
                                telemetry.addData("Gold Mineral Position", "Left");
                                loop = FALSE;
                                cubepos = 0;
                            }
                        } else {
                            telemetry.addData("Gold Mineral Position", "Right");
                            cubepos = 2;
                            loop = FALSE;
                        }
                        if (getRuntime() - timerreset > 5) {
                            loop = FALSE;
                            cubepos = 1;
                        }
                        telemetry.update();
                    }
                }
            }
        }
        if (tfod != null) {
            tfod.shutdown();
        }
        CameraDevice.getInstance().setFlashTorchMode(false) ;

        resetAngle();
        robot.mineralarm.setPower(1);
        DriveForward(.7, 9, .7, 9);
        robot.lift.setPower(0);
        DriveStrafe(1, 40, 1, -40);
        DriveForward(1, 80, 1, 80);

        robot.arm.setPower(-.5);
        robot.arm2.setPower(-.5);

        onethirtyfive(-115, 45);
        turnangle(-135);
        robot.mineralarm.setPower(1);

        while ((robot.arm2.getCurrentPosition() / 35) < 38 && !isStopRequested() && opModeIsActive() ){
            robot.arm.setPower(-.7);
            robot.arm2.setPower(-.7);
    //        DrivePower(-.5,-.5);

        }
        robot.arm.setPower(0);
        robot.arm2.setPower(0);

        DrivePower(-.5, -.5);
        sleep(1000);
        DriveStop();


        robot.intake.setPower(1);
        sleep( 1000);
        robot.intake.setPower(0);

        robot.mineralarm.setPower(0);

        if (cubepos == 2){

            robot.mineralarm.setPower(1);

            while ((robot.arm2.getCurrentPosition() / 35) > 20  && !isStopRequested() && opModeIsActive()){
                robot.arm.setPower(.8);
                robot.arm2.setPower(.8);
                //        DrivePower(-.5,-.5);

            }
            robot.mineralarm.setPower(0);

            robot.arm.setPower(0);
            robot.arm2.setPower(0);
            DriveStrafe(1,30,1,-30);
            DriveForward(1,-30,1,-30);
            DriveForward(1,33,1,33);
            DriveStrafe(1,-30,1,30);

        }
        if (cubepos == 1){

            robot.mineralarm.setPower(1);

            while ((robot.arm2.getCurrentPosition() / 35) > 45 && !isStopRequested() && opModeIsActive()){
                robot.arm.setPower(.8);
                robot.arm2.setPower(.8);
                //        DrivePower(-.5,-.5);
            }
            robot.arm.setPower(0);
            robot.arm2.setPower(0);
            robot.mineralarm.setPower(0);


            DriveForward(.5,20,.5,20);
            turnangle(-85);

            while ((robot.arm2.getCurrentPosition() / 35) < 55 && !isStopRequested() && opModeIsActive()){
                robot.arm.setPower(-.6);
                robot.arm2.setPower(-.6);

            }
            robot.arm.setPower(0);
            robot.arm2.setPower(0);

            sleep(500);
            turnangle(-135);

            while ((robot.arm2.getCurrentPosition() / 35) > 20 && !isStopRequested() && opModeIsActive()){
                robot.arm.setPower(.8);
                robot.arm2.setPower(.8);
                //        DrivePower(-.5,-.5);

            }
            robot.arm.setPower(0);
            robot.arm2.setPower(0);




            turnangle(-135);

            DriveForward(.5,-20,.5,-20);


        }
        if (cubepos == 0){



            while ((robot.arm2.getCurrentPosition() / 35) > 20 && !isStopRequested() && opModeIsActive()){
                robot.arm.setPower(.8);
                robot.arm2.setPower(.8);
                //        DrivePower(-.5,-.5);

            }
            robot.arm.setPower(0);
            robot.arm2.setPower(0);


            DriveForward(.5,-55,.5,-55);
            turnangle(-90);

            while ((robot.arm2.getCurrentPosition() / 35) < 55 && !isStopRequested() && opModeIsActive()){
                robot.arm.setPower(-.6);
                robot.arm2.setPower(-.6);

            }
            robot.arm.setPower(0);
            robot.arm2.setPower(0);
            sleep(500);

            turnangle(-60);

            while ((robot.arm2.getCurrentPosition() / 35) > 20 && !isStopRequested() && opModeIsActive()){
                robot.arm.setPower(.8);
                robot.arm2.setPower(.8);
                //        DrivePower(-.5,-.5);

            }
            robot.arm.setPower(0);
            robot.arm2.setPower(0);


            robot.mineralarm.setPower(1);

            turnangle(-135);

            robot.mineralarm.setPower(0);


            DriveForward(.5,65,.5,65);


        }

        onethirtyfive(-250, 170);
        sleep(200);
        resetAngle();
        if(cubepos == 0){
            DriveStrafe(1, -60, 1, 60);
            DriveForward(.7, -40, .7, -40);
            DriveForward(.7, 40, .7, 40);
            DriveStrafe(1, -40, 1, 40);
        }

        if(cubepos == 1){
            DriveStrafe(1, -120, 1, 120);
            DriveForward(.7, -40, .7, -40);
            DriveForward(.7, 40, .7, 40);
        }

        if(cubepos == 2){
            DriveStrafe(1,-160,1,160);
            DriveForward(.7, -40, .7, -40);
            DriveForward(.7, 40, .7, 40);
            DriveStrafe(1,40,1,-40);
        }

        zeroing();
        DriveForward(.7, 15, .7, 15);

        robot.arm.setPower(-.5);
        robot.arm2.setPower(-.5);
        robot.lift.setPower(1);
        sleep(2000);
        robot.lift.setPower(0);
        robot.arm.setPower(0);
        robot.arm2.setPower(0);

    }




    public void DrivePower (double leftpower, double rightpower) {

        robot.rightFrontDrive.setPower(rightpower);
        robot.rightBackDrive.setPower(rightpower);
        robot.leftFrontDrive.setPower(leftpower);
        robot.leftBackDrive.setPower(leftpower);

    }

    //Stops all motors in the drivetrain
    public void DriveStop (){
        DrivePower(0,0);
    }


    //Allows the ability to run the Mechanum as a tank drive using the encoders to run to a spcific distance at a cetain speed.

    private void resetAngle()
    {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }

    /**
     * Get current cumulative angle rotation from last reset.
     * @return Angle in degrees. + = left, - = right.
     */

    private double getAngle()
    {
        // We experimentally determined the Z axis is the axis we want to use for heading angle.
        // We have to process the angle because the imu works in euler angles so the Z axis is
        // returned as 0 to +180 or 0 to -180 rolling back to -179 or +179 when rotation passes
        // 180 degrees. We detect this transition and track the total cumulative angle of rotation.

        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    public void angledrive (int time , int strafeangle){
        double angle = 0;
        double angle2 = 0;
        double robotangle = 0;


            getAngle();
            robotangle = globalAngle + strafeangle;
            robotangle = robotangle *  3.14159 / 180;
            angle = Math.cos(robotangle) + Math.sin(robotangle);
            angle2 = Math.cos(robotangle) - Math.sin(robotangle);
            angle = angle * .8;
            angle2 = angle2 * .8;


            robot.rightFrontDrive.setPower(angle2 ); //lb lf
            robot.rightBackDrive.setPower(angle );
            robot.leftFrontDrive.setPower(angle );
            robot.leftBackDrive.setPower(angle2 );

            sleep(time * 1000);

            telemetry.addData("globalangle2", globalAngle);
            telemetry.addData("angle", angle);
            telemetry.addData("angle2", angle2);

            telemetry.update();



        DriveStop();
    }

    public void onethirtyfive (int turnangle , int strafeangle){
        double angle = 0;
        double angle2 = 0;
        double direction = 0;

        double robotangle = 0;

        while (Math.abs(globalAngle) < Math.abs(turnangle) && !isStopRequested() && opModeIsActive()) {

            getAngle();
            robotangle = globalAngle + strafeangle;
            robotangle = robotangle *  3.14159 / 180;
            angle = Math.cos(robotangle) + Math.sin(robotangle);
            angle2 = Math.cos(robotangle) - Math.sin(robotangle);
            angle = angle * .3;
            angle2 = angle2 * .3;

            direction = .7 * (Math.abs(turnangle) / turnangle);

            robot.rightFrontDrive.setPower(angle2 - direction); //lb lf
            robot.rightBackDrive.setPower(angle - direction);
            robot.leftFrontDrive.setPower(angle + direction);
            robot.leftBackDrive.setPower(angle2 + direction);



        }

        DriveStop();
    }

    public void turnangle (int angle){

        double anglereseter = Math.abs(globalAngle);

        while (Math.abs(globalAngle) < Math.abs(angle) - 3 ||  Math.abs(globalAngle) > Math.abs(angle) + 3 && !isStopRequested() && opModeIsActive()) {

            getAngle();

            robot.rightFrontDrive.setPower((globalAngle - angle) / 25);
            robot.rightBackDrive.setPower((globalAngle - angle) / 25);
            robot.leftFrontDrive.setPower((angle - globalAngle) / 25);
            robot.leftBackDrive.setPower((angle - globalAngle) / 25);


        }

        DriveStop();
    }
    public void zeroing (){

        getAngle();

        while (Math.abs(globalAngle) > 5 && !isStopRequested() && opModeIsActive())  {

            getAngle();

            robot.rightFrontDrive.setPower(globalAngle / 25);
            robot.rightBackDrive.setPower(globalAngle / 25);
            robot.leftFrontDrive.setPower( -globalAngle / 25);
            robot.leftBackDrive.setPower( -globalAngle / 25);



        }

        DriveStop();
    }
    public void DriveForward (double leftpower, int leftdistance, double rightpower, int rightdistance){

        //sets the encoder values to zero

        robot.rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //sets the position(distance) to drive to
        robot.rightFrontDrive.setTargetPosition(rightdistance * 35);
        robot.rightBackDrive.setTargetPosition(rightdistance * 35);
        robot.leftFrontDrive.setTargetPosition(leftdistance * 35);
        robot.leftBackDrive.setTargetPosition(leftdistance * 35);

        //engages the encoders to start tracking revolutions of the motor axel
        robot.rightFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.leftFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        //powers up the left and right side of the drivetrain independently
        DrivePower(leftpower, rightpower);

        //will pause the program until the motors have run to the previously specified position
        while (robot.rightFrontDrive.isBusy() && robot.rightBackDrive.isBusy() &&
                robot.leftFrontDrive.isBusy() && robot.leftBackDrive.isBusy())
        {

        }

        //stops the motors and sets them back to normal operation mode
        DriveStop();
        robot.rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }


    public void DriveStrafe (double leftpower, int leftdistance, double rightpower, int rightdistance){


        //sets the encoder values to zero
        robot.rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //sets the position(distance) to drive to
        robot.rightFrontDrive.setTargetPosition(rightdistance * 35);
        robot.rightBackDrive.setTargetPosition(leftdistance * 35);
        robot.leftFrontDrive.setTargetPosition(leftdistance * 35);
        robot.leftBackDrive.setTargetPosition(rightdistance * 35);

        //engages the encoders to start tracking revolutions of the motor axel
        robot.rightFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.leftFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        //powers up the left and right side of the drivetrain independently
        DrivePower(leftpower, rightpower);

        //will pause the program until the motors have run to the previously specified position
        while (robot.rightFrontDrive.isBusy() && robot.rightBackDrive.isBusy() &&
                robot.leftFrontDrive.isBusy() && robot.leftBackDrive.isBusy())
        {

        }

        //stops the motors and sets them back to normal operation mode
        DriveStop();
        robot.rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;


        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }
}







