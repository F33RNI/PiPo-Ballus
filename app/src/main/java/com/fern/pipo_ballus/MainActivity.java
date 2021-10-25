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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final int PERMISSION_REQUEST_CODE = 1;

    public static File settingsFile;
    private static SettingsContainer settingsContainer;

    private OpenCVHandler openCVHandler;

    private boolean paused = false;



    public static SettingsContainer getSettingsContainer() {
        return settingsContainer;
    }

    public static void setSettingsContainer(SettingsContainer settingsContainer) {
        MainActivity.settingsContainer = settingsContainer;
    }


    /**
     * Checks if OpenCV library is loaded
     */
    private final BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i("OpenCV", "OpenCV loaded successfully");

                // Parse settings
                settingsContainer = new SettingsContainer();
                SettingsHandler settingsHandler = new SettingsHandler(settingsFile,
                        MainActivity.this);
                settingsHandler.readSettings();

                // Request permissions
                if (hasPermissions(MainActivity.this, PERMISSIONS)) {
                    Log.i(TAG, "Permissions granted");

                    // Continue initialization
                    initModules();
                } else {
                    // Grant permissions
                    Log.w(TAG, "Not all permissions granted");
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS,
                            PERMISSION_REQUEST_CODE);
                }
            }
            else {
                super.onManagerConnected(status);
                Toast.makeText(MainActivity.this, "OpenCV not loaded!",
                        Toast.LENGTH_SHORT).show();

                // Close the application because OpenCV library not loaded
                finish();
                //System.exit(0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get settings file
        settingsFile = new File(getBaseContext().getExternalFilesDir( null),
                "settings.json");

        // Open layout
        setContentView(R.layout.activity_main);

        // Initialize OpenCVHandler class
        openCVHandler = new OpenCVHandler(findViewById(R.id.javaCameraView),
                getApplicationContext());

        // Load OpenCV library and init layout
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Internal OpenCV library not found." +
                    " Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,
                    this, baseLoaderCallback);
        } else {
            Log.i(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enable OpenCV view
        if (openCVHandler != null && openCVHandler.getCameraBridgeViewBase() != null)
            openCVHandler.getCameraBridgeViewBase().enableView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disable OpenCV view
        if (openCVHandler != null && openCVHandler.getCameraBridgeViewBase() != null)
            openCVHandler.getCameraBridgeViewBase().disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disable OpenCV view
        if (openCVHandler != null && openCVHandler.getCameraBridgeViewBase() != null)
            openCVHandler.getCameraBridgeViewBase().disableView();

        // Close thread
        ActivityCompat.finishAffinity(this);
        //System.exit(0);
    }

    /**
     * Checks for permissions
     * Code from: https://stackoverflow.com/a/34343101
     * @param context Activity
     * @param permissions List of permissions
     * @return true if all permissions were granted
     */
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calls on permission check result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check permissions
        if (hasPermissions(this, PERMISSIONS)) {
            Log.i(TAG, "Permissions granted");

            // Continue initialization
            initModules();
        }
        else {
            Toast.makeText(this, "Permissions not granted!",
                    Toast.LENGTH_LONG).show();

            // Close the application because the permissions are not granted
            finish();
            //System.exit(0);
        }
    }


    private void initModules() {
        openCVHandler.initView();

        Toast.makeText(this, "Here will be the initialization of modules",
                Toast.LENGTH_LONG).show();
    }


}