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

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OpenCVHandler implements CameraBridgeViewBase.CvCameraViewListener2 {
    private final String TAG = this.getClass().getName();

    private final CameraBridgeViewBase cameraBridgeViewBase;
    private final Context context;

    private Mat inputRGBA, outputRGBA, matRGBAt, matBGR, matBGRInverted, matHSV, matHSVInverted;
    private Mat invertColorMatrix;
    private Mat maskTable, maskTableCircle, maskBall, kernel, hierarchy;
    private HSVMaskRange hsvMaskRangeTable, hsvMaskRangeBall;
    private Scalar tableRectColor, tableMarksColor, tableTextColor, redColor, singleWhiteColor;

    private boolean scaled;
    private int displayOrientation;

    OpenCVHandler(CameraBridgeViewBase cameraBridgeViewBase, Context context) {
        this.cameraBridgeViewBase = cameraBridgeViewBase;
        this.context = context;

        this.scaled = false;
    }

    public CameraBridgeViewBase getCameraBridgeViewBase() {

        return cameraBridgeViewBase;
    }

    public void initView() {
        // Initialize CameraBridgeViewBase object
        cameraBridgeViewBase.setCvCameraViewListener(this);
        cameraBridgeViewBase.setCameraIndex(SettingsContainer.cameraID);
        cameraBridgeViewBase.setVisibility(CameraBridgeViewBase.VISIBLE);
        cameraBridgeViewBase.setMaxFrameSize(640, 480);

        // Initialize variables
        inputRGBA = new Mat();
        outputRGBA = new Mat();
        matRGBAt = new Mat();
        matBGR = new Mat();
        matBGRInverted = new Mat();
        matHSV = new Mat();
        matHSVInverted = new Mat();
        maskTable = new Mat();
        maskTableCircle = new Mat();
        maskBall = new Mat();
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        hierarchy = new Mat();
        scaled = false;
        hsvMaskRangeTable = new HSVMaskRange();
        hsvMaskRangeBall = new HSVMaskRange();
        tableRectColor = new Scalar(0, 255, 255);
        tableMarksColor = new Scalar(255, 0, 255);
        tableTextColor = new Scalar(255, 255, 0);
        redColor = new Scalar(255, 0, 0);
        singleWhiteColor = new Scalar(255);

        // Create table mask scalars
        hsvMaskRangeTable.fromIntColor(SettingsContainer.tableColor);
        hsvMaskRangeBall.fromIntColor(SettingsContainer.ballColor);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        scaled = false;
    }

    @Override
    public void onCameraViewStopped() {
        scaled = false;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        try {
            // Read input RGBA image
            inputRGBA = inputFrame.rgba();

            // Get new screen orientation
            if (!scaled)
                displayOrientation = ((WindowManager)
                        context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                        .getOrientation();

            // Rotate frame on different orientations
            if (displayOrientation == Surface.ROTATION_0) {
                Core.transpose(inputRGBA, matRGBAt);
                Core.flip(matRGBAt, inputRGBA, 1);
            } else if (displayOrientation == Surface.ROTATION_270) {
                Core.flip(inputRGBA, inputRGBA, 0);
                Core.flip(inputRGBA, inputRGBA, 1);
            } else if (displayOrientation == Surface.ROTATION_180) {
                Core.transpose(inputRGBA, matRGBAt);
                Core.flip(inputRGBA, inputRGBA, 0);
            }
            

            // Clone object for debug frame
            inputRGBA.copyTo(outputRGBA);

            // Convert to RGB
            Imgproc.cvtColor(inputRGBA, matBGR, Imgproc.COLOR_RGBA2BGR, 3);

            // Invert GRB
            if (!scaled)
                invertColorMatrix = new Mat(matBGR.rows(), matBGR.cols(), matBGR.type(),
                        new Scalar(255,255,255));
            Core.subtract(invertColorMatrix, matBGR, matBGRInverted);

            // Convert to HSV
            Imgproc.cvtColor(matBGR, matHSV, Imgproc.COLOR_BGR2HSV, 3);
            Imgproc.cvtColor(matBGRInverted, matHSVInverted, Imgproc.COLOR_BGR2HSV, 3);

            // Get table mask
            if (hsvMaskRangeTable.isInverted())
                Core.inRange(matHSVInverted, hsvMaskRangeTable.getLower(),
                        hsvMaskRangeTable.getUpper(), maskTable);
            else
                Core.inRange(matHSV, hsvMaskRangeTable.getLower(),
                        hsvMaskRangeTable.getUpper(), maskTable);

            // Filter table mask
            Imgproc.erode(maskTable, maskTable, kernel);
            Imgproc.dilate(maskTable, maskTable, kernel);

            // Get ball mask
            if (hsvMaskRangeBall.isInverted())
                Core.inRange(matHSVInverted, hsvMaskRangeBall.getLower(),
                        hsvMaskRangeBall.getUpper(), maskBall);
            else
                Core.inRange(matHSV, hsvMaskRangeBall.getLower(),
                        hsvMaskRangeBall.getUpper(), maskBall);

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

                    // Extract table's bounding rectangle
                    Rect tableBoundingRect = Imgproc.boundingRect(contours.get(tableContourIndex));

                    // Calculate table's radius
                    int tableCircleR = (tableBoundingRect.height + tableBoundingRect.width) / 4;

                    // Draw table's rectangle
                    Imgproc.rectangle(outputRGBA, tableBoundingRect.tl(),
                            tableBoundingRect.br(), tableRectColor, 2);

                    // Calculate frame reference points
                    int pX = (int) tableBoundingRect.tl().x + tableCircleR;
                    int pY = (int) tableBoundingRect.tl().y;
                    int rX = (int) tableBoundingRect.tl().x + tableCircleR
                            - (int) (tableCircleR / 2 * Math.sqrt(3.));
                    int rY = (int) tableBoundingRect.tl().y + tableCircleR + tableCircleR / 2;
                    int qX = (int) tableBoundingRect.tl().x + tableCircleR
                            + (int) (tableCircleR / 2 * Math.sqrt(3.));
                    int qY = (int) tableBoundingRect.tl().y + tableCircleR + tableCircleR / 2;

                    // Draw reference circles, text and lines
                    Imgproc.line(outputRGBA, new Point(pX, pY),
                            new Point(pX, pY + (int) (tableCircleR / 4)), tableMarksColor, 1);
                    Imgproc.circle(outputRGBA, new Point(pX, pY), 10, tableMarksColor, 1);
                    Imgproc.putText(outputRGBA, "P", new Point(pX - 5, pY + 5),
                            Core.FONT_HERSHEY_PLAIN, 1, tableTextColor, 1);
                    Imgproc.circle(outputRGBA, new Point(qX, qY), 10, tableMarksColor, 1);
                    Imgproc.putText(outputRGBA, "Q", new Point(qX - 5, qY + 5),
                            Core.FONT_HERSHEY_PLAIN, 1, tableTextColor, 1);
                    Imgproc.circle(outputRGBA, new Point(rX, rY), 10, tableMarksColor, 1);
                    Imgproc.putText(outputRGBA, "R", new Point(rX - 5, rY + 5),
                            Core.FONT_HERSHEY_PLAIN, 1, tableTextColor, 1);

                    // Initialize maskTableCircle Mat if not scaled
                    if (!scaled)
                        maskTableCircle = Mat.zeros(maskTable.rows(),
                                maskTable.cols(), CvType.CV_8UC1);

                    // Create circle mask of the table
                    Imgproc.circle(maskTableCircle,
                            new Point((int) tableBoundingRect.tl().x + tableCircleR,
                                    (int) tableBoundingRect.tl().y + tableCircleR),
                            tableCircleR, singleWhiteColor, -1);

                    // Erode mask to remove edges
                    Imgproc.erode(maskTableCircle, maskTableCircle, kernel);

                    // Calculate ball mask
                    Core.bitwise_and(maskBall, maskTableCircle, maskBall);

                    // Find ball contour
                    List<MatOfPoint> ballContours = new ArrayList<>();
                    Imgproc.findContours(maskTable, ballContours, hierarchy, Imgproc.RETR_EXTERNAL,
                            Imgproc.CHAIN_APPROX_SIMPLE);

                    // Check if there is at least one contour
                    if (ballContours.size() > 0) {
                        // Find largest contour (table)
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
                        if (ballContourIndex > 0) {
                            MatOfPoint2f ballContour = new MatOfPoint2f();
                            ballContours.get(ballContourIndex).convertTo(ballContour,
                                    CvType.CV_32F);
                            Point ballCenter = new Point();
                            float[] radius = new float[1];
                            Imgproc.minEnclosingCircle(ballContour, ballCenter, radius);

                            Imgproc.circle(outputRGBA, ballCenter, (int) radius[0], tableMarksColor, 1);

                        } else
                            Imgproc.putText(outputRGBA, "Wrong ball size!", new Point(50, 50),
                                    Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);
                    } else
                        Imgproc.putText(outputRGBA, "Ball not found!", new Point(50, 50),
                                Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);
                } else
                    Imgproc.putText(outputRGBA, "Table too small!", new Point(50, 50),
                            Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);
            } else
                Imgproc.putText(outputRGBA, "Table not found!", new Point(50, 50),
                        Core.FONT_HERSHEY_PLAIN, 2, redColor, 2);


            //Imgproc.cvtColor(maskTable, outputRGBA, Imgproc.COLOR_GRAY2RGBA, 4);


            

            

            // Resize to original size
            Imgproc.resize(outputRGBA, outputRGBA, inputFrame.rgba().size());

            // Set new scaled coefficient to match original aspect ratio
            if (!scaled && (displayOrientation == Surface.ROTATION_0
                    || displayOrientation == Surface.ROTATION_180))
                cameraBridgeViewBase.setScaleY((float)
                        ((inputRGBA.size().width * inputRGBA.size().width)
                                / (inputRGBA.size().height * inputRGBA.size().height)));

            // Set scaled flag
            if (!scaled)
                scaled = true;

            return outputRGBA;
        } catch (Exception e) {
            // Show error message
            Log.e(TAG, "Error processing frame!", e);
        }

        return inputFrame.rgba();
    }
}
