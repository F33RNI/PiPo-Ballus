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
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OpenCVHandler implements CameraBridgeViewBase.CvCameraViewListener2 {
    private final String TAG = this.getClass().getName();

    private final CameraBridgeViewBase cameraBridgeViewBase;
    private final Context context;

    private Mat inputRGBA, outputRGBA, outputRGBAt, matBGR, matBGRInverted, matHSV, matHSVInverted;
    private Mat invertColorMatrix;
    private Mat maskTable, maskBall;
    private HSVMaskRange hsvMaskRangeTable, hsvMaskRangeBall;

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
        outputRGBAt = new Mat();
        matBGR = new Mat();
        matBGRInverted = new Mat();
        matHSV = new Mat();
        matHSVInverted = new Mat();
        maskTable = new Mat();
        maskBall = new Mat();
        scaled = false;
        hsvMaskRangeTable = new HSVMaskRange();
        hsvMaskRangeBall = new HSVMaskRange();

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

            // Get ball mask
            if (hsvMaskRangeBall.isInverted())
                Core.inRange(matHSVInverted, hsvMaskRangeBall.getLower(),
                        hsvMaskRangeBall.getUpper(), maskBall);
            else
                Core.inRange(matHSV, hsvMaskRangeBall.getLower(),
                        hsvMaskRangeBall.getUpper(), maskBall);


            //Imgproc.cvtColor(maskTable, outputRGBA, Imgproc.COLOR_GRAY2RGBA, 4);


            // Get new screen orientation
            if (!scaled)
                displayOrientation = ((WindowManager)
                        context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                        .getOrientation();

            // Rotate frame on different orientations
            if (displayOrientation == Surface.ROTATION_0) {
                Core.transpose(outputRGBA, outputRGBAt);
                Core.flip(outputRGBAt, outputRGBA, 1);
            } else if (displayOrientation == Surface.ROTATION_270) {
                Core.flip(outputRGBA, outputRGBA, 0);
                Core.flip(outputRGBA, outputRGBA, 1);
            } else if (displayOrientation == Surface.ROTATION_180) {
                Core.transpose(outputRGBA, outputRGBAt);
                Core.flip(outputRGBA, outputRGBA, 0);
            }

            // Resize to original size
            Imgproc.resize(outputRGBA, outputRGBA, inputRGBA.size());

            // Set new scaled coefficient to match original aspect ratio
            if (!scaled && (displayOrientation == Surface.ROTATION_0
                    || displayOrientation == Surface.ROTATION_180))
                cameraBridgeViewBase.setScaleY((float)
                        ((inputRGBA.size().width * inputRGBA.size().width)
                                / (inputRGBA.size().height * inputRGBA.size().height)));

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
