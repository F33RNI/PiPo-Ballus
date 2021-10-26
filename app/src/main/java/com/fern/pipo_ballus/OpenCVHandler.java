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
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OpenCVHandler implements CameraBridgeViewBase.CvCameraViewListener2 {
    private final String TAG = this.getClass().getName();

    private final CameraBridgeViewBase cameraBridgeViewBase;
    private final Context context;

    private Mat inputRGBA, matRGBA, matRGBAt, matGray;

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
        cameraBridgeViewBase.setCvCameraViewListener(this);
        cameraBridgeViewBase.setCameraIndex(MainActivity.getSettingsContainer().cameraID);
        cameraBridgeViewBase.setVisibility(CameraBridgeViewBase.VISIBLE);
        cameraBridgeViewBase.setMaxFrameSize(640, 480);

        inputRGBA = new Mat();
        matRGBA = new Mat();
        matRGBAt = new Mat();
        matGray = new Mat();
        scaled = false;
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
        inputRGBA = inputFrame.rgba();

        Imgproc.cvtColor(inputRGBA, matGray, Imgproc.COLOR_BGRA2GRAY, 1);
        Imgproc.cvtColor(matGray, matRGBA, Imgproc.COLOR_GRAY2BGRA, 4);


        // Get new screen orientation
        if (!scaled)
            displayOrientation = ((WindowManager)
                    context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                    .getOrientation();

        // Rotate frame on different orientations
        if (displayOrientation == Surface.ROTATION_0) {
            Core.transpose(matRGBA, matRGBAt);
            Core.flip(matRGBAt, matRGBA, 1);
        }
        else if (displayOrientation == Surface.ROTATION_270) {
            Core.flip(matRGBA, matRGBA, 0);
            Core.flip(matRGBA, matRGBA, 1);
        }
        else if (displayOrientation == Surface.ROTATION_180) {
            Core.transpose(matRGBA, matRGBAt);
            Core.flip(matRGBAt, matRGBA, 0);
        }

        // Resize to original size
        Imgproc.resize(matRGBA, matRGBA, inputRGBA.size());

        // Set new scaled coefficient to match original aspect ratio
        if (!scaled && (displayOrientation == Surface.ROTATION_0
                || displayOrientation == Surface.ROTATION_180))
                cameraBridgeViewBase.setScaleY((float)
                        ((inputRGBA.size().width * inputRGBA.size().width)
                                / (inputRGBA.size().height * inputRGBA.size().height)));

        if (!scaled)
            scaled = true;

        return matRGBA;

    }
}
