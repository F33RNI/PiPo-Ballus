/*
 * Copyright (C) 2021 Fern H. (aka Pavel Neshumov), PiPo-Ballus Android application
 *
 * Licensed under the GNU Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * IT IS STRICTLY PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE)
 * FOR MILITARY PURPOSES. ALSO, IT IS STRICTLY PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE)
 * FOR ANY PURPOSE THAT MAY LEAD TO INJURY, HUMAN, ANIMAL OR ENVIRONMENTAL DAMAGE.
 * ALSO, IT IS PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE) FOR ANY PURPOSE THAT
 * VIOLATES INTERNATIONAL HUMAN RIGHTS OR HUMAN FREEDOM.
 * BY USING THE PROJECT (OR PART OF THE PROJECT / CODE) YOU AGREE TO ALL OF THE ABOVE RULES.
 */

package com.fern.pipo_ballus;

import android.app.Activity;
import android.util.Log;
import android.view.Surface;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class provides the main algorithm for this application.
 * Namely, it calculates the position of the ball on the table using computer vision
 *
 * TODO: Add ball altitude detection (ballVSTableZ, ballSetpointZ)
 */
public class OpenCVHandler implements CameraBridgeViewBase.CvCameraViewListener2 {
    private final String TAG = this.getClass().getName();

    private final static int ALLOWED_LOST_FRAMES = 5;

    private final PositionContainer positionContainer;

    private final CameraBridgeViewBase cameraBridgeViewBase;
    private final Activity activity;
    private final LinkedBlockingQueue<PositionContainer> positionContainers;

    private Mat inputRGBA, outputRGBA, matRGBAt, matBGR, matBGRInverted, matHSV, matHSVInverted;
    private Mat matHue, matSaturation, matValue;
    private List<Mat> channels;
    private Mat maskTable, maskBall, hierarchy;
    private Scalar colorTableLower, colorTableUpper;
    private Scalar colorBallLower, colorBallUpper;
    private boolean tableRangeInverted, ballRangeInverted;
    private Scalar tableEllipseColor, tableMarksColor, tableTextColor, ballColor, ballSetpointColor;
    private Scalar redColor, orangeColor, singleWhiteColor;

    private int rotationLast;
    private boolean initialized;
    private int lostFrames;

    OpenCVHandler(CameraBridgeViewBase cameraBridgeViewBase,
                  Activity activity,
                  LinkedBlockingQueue<PositionContainer> positionContainers) {
        this.cameraBridgeViewBase = cameraBridgeViewBase;
        this.activity = activity;
        this.positionContainers = positionContainers;

        this.positionContainer = new PositionContainer();

        this.initialized = false;
    }

    public CameraBridgeViewBase getCameraBridgeViewBase() {

        return cameraBridgeViewBase;
    }

    /**
     * Initializes the components of the class.
     * NOTE: Make sure the method is called no more than once to prevent memory leaks
     */
    public void initView() {
        // Initialize CameraBridgeViewBase object
        cameraBridgeViewBase.setCvCameraViewListener(this);
        cameraBridgeViewBase.setCameraIndex(SettingsContainer.cameraID);
        cameraBridgeViewBase.setVisibility(CameraBridgeViewBase.VISIBLE);
        cameraBridgeViewBase.setMaxFrameSize(640, 480);

        // Initialize variables
        rotationLast = -1;

        inputRGBA = new Mat();
        outputRGBA = new Mat();
        matRGBAt = new Mat();
        matBGR = new Mat();
        matBGRInverted = new Mat();
        matHSV = new Mat();
        matHSVInverted = new Mat();

        matHue = new Mat();
        matSaturation = new Mat();
        matValue = new Mat();
        channels = new ArrayList<>();

        maskTable = new Mat();
        maskBall = new Mat();
        hierarchy = new Mat();

        // Initialize HSVColor class for color conversion
        HSVColor hsvTableLower = new HSVColor(SettingsContainer.tableColorLower);
        HSVColor hsvTableUpper = new HSVColor(SettingsContainer.tableColorUpper);
        HSVColor hsvBallLower = new HSVColor(SettingsContainer.ballColorLower);
        HSVColor hsvBallUpper = new HSVColor(SettingsContainer.ballColorUpper);

        // Convert table range to Scalar
        if (hsvTableLower.getHue() == hsvTableUpper.getHue()) {
            tableRangeInverted = false;
            colorTableLower = new Scalar(0,
                    hsvTableLower.getSaturationInt(), hsvTableLower.getValueInt());
            colorTableUpper = new Scalar(179,
                    hsvTableUpper.getSaturationInt(), hsvTableUpper.getValueInt());
        } else if (hsvTableLower.getHue() > hsvTableUpper.getHue()) {
            tableRangeInverted = true;
            colorTableLower = new Scalar((int) (hsvTableLower.getHue() / 2) - 90,
                    hsvTableLower.getSaturationInt(), hsvTableLower.getValueInt());
            colorTableUpper = new Scalar((int) (hsvTableUpper.getHue() / 2) + 90,
                    hsvTableUpper.getSaturationInt(), hsvTableUpper.getValueInt());
        } else {
            tableRangeInverted = false;
            colorTableLower = new Scalar((int) (hsvTableLower.getHue() / 2),
                    hsvTableLower.getSaturationInt(), hsvTableLower.getValueInt());
            colorTableUpper = new Scalar((int) (hsvTableUpper.getHue() / 2),
                    hsvTableUpper.getSaturationInt(), hsvTableUpper.getValueInt());
        }

        // Convert ball range to Scalar
        if (Math.abs(hsvBallLower.getHue() - hsvBallUpper.getHue()) < 5) {
            ballRangeInverted = false;
            colorBallLower = new Scalar(0,
                    hsvBallLower.getSaturationInt(), hsvBallLower.getValueInt());
            colorBallUpper = new Scalar(179,
                    hsvBallUpper.getSaturationInt(), hsvBallUpper.getValueInt());
        } else if (hsvBallLower.getHue() > hsvBallUpper.getHue()) {
            ballRangeInverted = true;
            colorBallLower = new Scalar((int) (hsvBallLower.getHue() / 2) - 90,
                    hsvBallLower.getSaturationInt(), hsvBallLower.getValueInt());
            colorBallUpper = new Scalar((int) (hsvBallUpper.getHue() / 2) + 90,
                    hsvBallUpper.getSaturationInt(), hsvBallUpper.getValueInt());
        } else {
            ballRangeInverted = false;
            colorBallLower = new Scalar((int) (hsvBallLower.getHue() / 2),
                    hsvBallLower.getSaturationInt(), hsvBallLower.getValueInt());
            colorBallUpper = new Scalar((int) (hsvBallUpper.getHue() / 2),
                    hsvBallUpper.getSaturationInt(), hsvBallUpper.getValueInt());
        }

        // Initialize basic colors
        tableEllipseColor = new Scalar(0, 255, 255);
        tableMarksColor = new Scalar(255, 0, 255);
        tableTextColor = new Scalar(255, 255, 0);
        ballColor = new Scalar(255, 255, 0);
        ballSetpointColor = new Scalar(0, 255, 0);

        redColor = new Scalar(255, 0, 0);
        orangeColor = new Scalar(255, 63, 0);
        singleWhiteColor = new Scalar(255);

        // Set initialized flag
        initialized = true;
    }

    /**
     * @return true if initView() was called
     */
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        try {
            long timeStart = System.currentTimeMillis();

            // Read input RGBA image
            inputRGBA = inputFrame.rgba();

            // Get current screen rotation angle
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

            // Rotate frame on different orientations
            if (rotation == Surface.ROTATION_0) {
                Core.transpose(inputRGBA, matRGBAt);
                if (SettingsContainer.cameraID == CameraBridgeViewBase.CAMERA_ID_FRONT)
                    Core.flip(matRGBAt, inputRGBA, 0);
                else
                    Core.flip(matRGBAt, inputRGBA, 1);
            } else if (rotation == Surface.ROTATION_270) {
                Core.flip(inputRGBA, inputRGBA, 0);
                Core.flip(inputRGBA, inputRGBA, 1);
            } else if (rotation == Surface.ROTATION_180) {
                Core.transpose(inputRGBA, matRGBAt);
                if (SettingsContainer.cameraID == CameraBridgeViewBase.CAMERA_ID_FRONT)
                    Core.flip(matRGBAt, inputRGBA, 1);
                else
                    Core.flip(matRGBAt, inputRGBA, 0);
            }

            // Clone object for debug frame
            inputRGBA.copyTo(outputRGBA);

            // Convert to BGR
            Imgproc.cvtColor(inputRGBA, matBGR, Imgproc.COLOR_RGBA2BGR, 3);

            // Invert BGR
            Mat invertColorMatrix = new Mat(matBGR.rows(), matBGR.cols(), matBGR.type(),
                    new Scalar(255, 255, 255));
            Core.subtract(invertColorMatrix, matBGR, matBGRInverted);
            invertColorMatrix.release();

            // Convert to HSV
            Imgproc.cvtColor(matBGR, matHSV, Imgproc.COLOR_BGR2HSV, 3);
            Imgproc.cvtColor(matBGRInverted, matHSVInverted, Imgproc.COLOR_BGR2HSV, 3);

            // Extract channels with inverted Hue from HSV
            Core.extractChannel(matHSVInverted, matHue, 0);
            Core.extractChannel(matHSV, matSaturation, 1);
            Core.extractChannel(matHSV, matValue, 2);

            // Make inverted HSV mat
            channels.clear();
            channels.add(matHue);
            channels.add(matSaturation);
            channels.add(matValue);
            Core.merge(channels, matHSVInverted);

            // Get table mask
            if (tableRangeInverted)
                Core.inRange(matHSVInverted, colorTableLower, colorTableUpper, maskTable);
            else
                Core.inRange(matHSV, colorTableLower, colorTableUpper, maskTable);

            // Get ball mask
            if (ballRangeInverted)
                Core.inRange(matHSVInverted, colorBallLower, colorBallUpper, maskBall);
            else
                Core.inRange(matHSV, colorBallLower, colorBallUpper, maskBall);

            // Find table contours
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(maskTable, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE);

            // Check if there is at least one contour
            if (contours.size() > 0) {
                // Find largest contour (table)
                int maxContourArea = 0;
                int tableContourIndex = 0;
                for (int i = 0; i < contours.size(); i++) {
                    int contourArea = (int) Imgproc.contourArea(contours.get(i));
                    if (contourArea > maxContourArea) {
                        maxContourArea = contourArea;
                        tableContourIndex = i;
                    }
                }

                // Check table's area
                if (maxContourArea > 1000) {

                    // Extract table's bounding rectangle and ellipse
                    Rect tableBoundingRect = Imgproc.boundingRect(contours.get(tableContourIndex));
                    RotatedRect tableRotatedRect = Imgproc.fitEllipse(
                            new MatOfPoint2f(contours.get(tableContourIndex).toArray()));

                    // Reduce size of the table's ellipse to remove border
                    tableRotatedRect.size.width -= 10;
                    tableRotatedRect.size.height -= 10;

                    // Calculate table's radius
                    int tableCircleR = (tableBoundingRect.height + tableBoundingRect.width) / 4;

                    // Draw table's ellipse
                    Imgproc.ellipse(outputRGBA, tableRotatedRect, tableEllipseColor, 2);

                    // Draw table's center
                    Imgproc.circle(outputRGBA, tableRotatedRect.center, 5,
                            tableMarksColor, 1);

                    // Calculate frame reference points
                    int pqXK = (int) (tableBoundingRect.width / 4 * Math.sqrt(3.));
                    int pqYK = tableBoundingRect.height / 4;
                    Point p = new Point(tableRotatedRect.center.x,
                            (int) (tableRotatedRect.center.y - tableBoundingRect.height / 2));
                    Point q = new Point(tableRotatedRect.center.x + pqXK,
                            tableRotatedRect.center.y + pqYK);
                    Point r = new Point(tableRotatedRect.center.x - pqXK,
                            tableRotatedRect.center.y + pqYK);

                    // Draw reference marks
                    Imgproc.line(outputRGBA, p,
                            new Point(p.x, (int) (p.y + tableCircleR / 4)),
                            tableMarksColor, 1);
                    Imgproc.circle(outputRGBA, p, 10, tableMarksColor, 1);
                    Imgproc.putText(outputRGBA, "P", new Point(p.x - 5, p.y + 5),
                            Core.FONT_HERSHEY_PLAIN, 1, tableTextColor, 1);
                    Imgproc.circle(outputRGBA, q, 10, tableMarksColor, 1);
                    Imgproc.putText(outputRGBA, "Q", new Point(q.x - 5, q.y + 5),
                            Core.FONT_HERSHEY_PLAIN, 1, tableTextColor, 1);
                    Imgproc.circle(outputRGBA, r, 10, tableMarksColor, 1);
                    Imgproc.putText(outputRGBA, "R", new Point(r.x - 5, r.y + 5),
                            Core.FONT_HERSHEY_PLAIN, 1, tableTextColor, 1);

                    // Draw ball's setpoint
                    Imgproc.circle(outputRGBA,
                            new Point(map((int) positionContainer.ballSetpointX,
                                    1000, 2000,
                                    (int) (tableRotatedRect.center.x -
                                            tableBoundingRect.width / 2),
                                    (int) (tableRotatedRect.center.x +
                                            tableBoundingRect.width / 2)),
                                    map((int) positionContainer.ballSetpointY,
                                            1000, 2000,
                                            (int) (tableRotatedRect.center.y
                                                    - tableBoundingRect.height / 2),
                                            (int) (tableRotatedRect.center.y
                                                    + tableBoundingRect.height / 2))),
                            20, ballSetpointColor, 2);

                    // Initialize maskTableCircle
                    Mat maskTableCircle =
                            Mat.zeros(maskTable.rows(), maskTable.cols(), maskTable.type());

                    // Create circle mask of the table
                    Imgproc.ellipse(maskTableCircle, tableRotatedRect,
                            singleWhiteColor, -1);

                    // Calculate ball mask
                    Core.bitwise_and(maskBall, maskTableCircle, maskBall);

                    // Remove maskTableCircle from memory
                    maskTableCircle.release();

                    // Find ball contour
                    List<MatOfPoint> ballContours = new ArrayList<>();
                    Imgproc.findContours(maskBall, ballContours, hierarchy, Imgproc.RETR_EXTERNAL,
                            Imgproc.CHAIN_APPROX_SIMPLE);

                    // Check if there is at least one contour
                    if (ballContours.size() > 0) {
                        // Find largest contour (ball)
                        int maxBallArea = 100;
                        int ballContourIndex = -1;
                        for (int i = 0; i < ballContours.size(); i++) {
                            int contourArea = (int) Imgproc.contourArea(ballContours.get(i));
                            if (contourArea > maxBallArea && contourArea < maxContourArea / 4) {
                                maxBallArea = contourArea;
                                ballContourIndex = i;
                            }
                        }

                        // Check if correct size found
                        if (ballContourIndex >= 0) {
                            // Find ball position
                            MatOfPoint2f ballContour = new MatOfPoint2f();
                            ballContours.get(ballContourIndex).convertTo(ballContour,
                                    CvType.CV_32F);
                            Point ballCenter = new Point();
                            float[] radius = new float[1];
                            Imgproc.minEnclosingCircle(ballContour, ballCenter, radius);

                            // Calculate ball position relative to table's center (1000-2000)
                            int ballVSTableX = map((int) (ballCenter.x - tableRotatedRect.center.x),
                                    -tableBoundingRect.width / 2,
                                    tableBoundingRect.width / 2, 1000, 2000);
                            int ballVSTableY = map((int) (ballCenter.y - tableRotatedRect.center.y),
                                    -tableBoundingRect.height / 2,
                                    tableBoundingRect.height / 2, 1000, 2000);

                            // Check ball position
                            if (ballVSTableX >= 1000 && ballVSTableX <= 2000
                                    && ballVSTableY >= 1000 && ballVSTableY <= 2000) {

                                // Set new coordinates without filtering
                                // if before that the ball was not found
                                if (!positionContainer.ballDetected) {
                                    positionContainer.ballVSTableX = ballVSTableX;
                                    positionContainer.ballVSTableY = ballVSTableY;
                                    positionContainer.ballVSTableZ = 1500;
                                    positionContainer.ballDetected = true;
                                }

                                // Set new filtered coordinates
                                else {
                                    positionContainer.ballVSTableX =
                                            positionContainer.ballVSTableX
                                                    * SettingsContainer.positionFilter +
                                                    (double) ballVSTableX
                                                            * (1 - SettingsContainer.
                                                            positionFilter);
                                    positionContainer.ballVSTableY =
                                            positionContainer.ballVSTableY
                                                    * SettingsContainer.positionFilter +
                                                    (double) ballVSTableY
                                                            * (1 - SettingsContainer.
                                                            positionFilter);
                                    positionContainer.ballVSTableZ = 1500;
                                }

                                // Update lost counter
                                lostFrames = ALLOWED_LOST_FRAMES;

                                // Draw ball's position
                                Imgproc.circle(outputRGBA,
                                        new Point(map((int) positionContainer.ballVSTableX,
                                                1000, 2000,
                                                (int) (tableRotatedRect.center.x
                                                        - tableBoundingRect.width / 2),
                                                (int) (tableRotatedRect.center.x
                                                        + tableBoundingRect.width / 2)),
                                                map((int) positionContainer.ballVSTableY,
                                                        1000, 2000,
                                                        (int) (tableRotatedRect.center.y
                                                                - tableBoundingRect.height / 2),
                                                        (int) (tableRotatedRect.center.y
                                                                + tableBoundingRect.height / 2))),
                                        (int) radius[0], ballColor, 2);
                            } else
                                Imgproc.putText(outputRGBA, "Wrong ball position!", new Point(30, 50),
                                        Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);
                        } else
                            Imgproc.putText(outputRGBA, "Wrong ball size!", new Point(30, 50),
                                    Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);
                    } else
                        Imgproc.putText(outputRGBA, "Ball not found!", new Point(30, 50),
                                Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);
                } else
                    Imgproc.putText(outputRGBA, "Table too small!", new Point(30, 50),
                            Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);
            } else
                Imgproc.putText(outputRGBA, "Table not found!", new Point(30, 50),
                        Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);

            // Decrement lostFrames counter every frame
            if (lostFrames > 0)
                lostFrames--;

                // Clear ballDetected flag if more frames lost than threshold
            else
                positionContainer.ballDetected = false;

            // Send new ball's position or clear the LinkedBlockingQueue
            try {
                if (positionContainers != null) {
                    if (positionContainer.ballDetected && positionContainers.size() < 30)
                        positionContainers.put(positionContainer);
                    else
                        positionContainers.clear();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error putting new position to LinkedBlockingQueue!", e);
            }

            // Display a message about low performance if the frame time is more than 33 ms (30 fps)
            if (System.currentTimeMillis() - timeStart > 33)
                Imgproc.putText(outputRGBA, "WARNING! Low performance!",
                        new Point(30, 100),
                        Core.FONT_HERSHEY_PLAIN, 2, orangeColor, 2);

            // Resize to original size
            Imgproc.resize(outputRGBA, outputRGBA, inputFrame.rgba().size());

            // On rotation changed
            if (rotation != rotationLast) {
                // Set MAX_PRIORITY
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                // Set new scaling factor
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
                    cameraBridgeViewBase.setScaleY((float)
                            ((inputRGBA.size().width * inputRGBA.size().width)
                                    / (inputRGBA.size().height * inputRGBA.size().height)));
                else
                    cameraBridgeViewBase.setScaleY(1);
            }

            // Remember new rotation
            rotationLast = rotation;

            return outputRGBA;
        } catch (Exception e) {
            // Show error message
            Log.e(TAG, "Error processing frame!", e);
        }

        // Return raw frame if error occurs
        return inputFrame.rgba();
    }

    /**
     * Re-maps a number from one range to another.
     * That is, a value of fromLow would get mapped to toLow, a value of fromHigh to toHigh,
     * values in-between to values in-between, etc.
     *
     * This function is from Arduino
     * https://www.arduino.cc/reference/en/language/functions/math/map/
     *
     * @param value the number to map
     * @param in_min the lower bound of the value’s current range
     * @param in_max the upper bound of the value’s current range
     * @param out_min the lower bound of the value’s target range
     * @param out_max the upper bound of the value’s target range
     * @return the mapped value
     */
    private int map(int value, int in_min, int in_max, int out_min, int out_max) {
        return (value - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}
