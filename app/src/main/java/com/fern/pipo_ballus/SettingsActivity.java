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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.CameraBridgeViewBase;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private final static String[] cameraOptions = new String[]{"Any", "Back", "Front"};

    // Local settings
    private int cameraID;
    private int tableColor;
    private int ballColor;

    // Elements
    Spinner cameraIDSpinner;
    Button settingsTableColor;
    Button settingsBallColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize elements
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        cameraIDSpinner = findViewById(R.id.cameraIDSpinner);
        settingsTableColor = findViewById(R.id.settingsTableColor);
        settingsBallColor = findViewById(R.id.settingsBallColor);

        // Select home item
        bottomNavigationView.setSelectedItemId(R.id.menuSettings);

        // Add bottom menu clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menuHome) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                System.gc();
                finish();
            }
            else if (item.getItemId() == R.id.menuCamera) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                System.gc();
                finish();
            }
            return false;
        });

        // Connect Restore button
        findViewById(R.id.settingsResetBtn).setOnClickListener(view -> {
            // Reset settings to default
            SettingsContainer.resetToDefaults();

            // Update view
            updateView();
        });

        // Connect Save button
        findViewById(R.id.settingsSaveBtn).setOnClickListener(view -> saveSettings());

        // Connect camera ID spinner
        cameraIDSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView
                            , int position, long id) {
                        if (position == 1)
                            cameraID = CameraBridgeViewBase.CAMERA_ID_BACK;
                        else if (position == 2)
                            cameraID = CameraBridgeViewBase.CAMERA_ID_FRONT;
                        else
                            cameraID = CameraBridgeViewBase.CAMERA_ID_ANY;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                    }

                });

        // Connect table color button
        settingsTableColor.setOnClickListener(view -> {
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, tableColor);
            colorPickerDialog.setColorPickerListener(color -> {
                // Remember edited color
                tableColor = color;

                // Update view
                updateView();

            });
            colorPickerDialog.show();
        });

        // Connect ball color button
        settingsBallColor.setOnClickListener(view -> {
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, ballColor);
            colorPickerDialog.setColorPickerListener(color -> {
                // Remember edited color
                ballColor = color;

                // Update view
                updateView();

            });
            colorPickerDialog.show();
        });

        // Copy settings to local variables
        this.cameraID = SettingsContainer.cameraID;
        this.tableColor = SettingsContainer.tableColor;
        this.ballColor = SettingsContainer.ballColor;

        // Load view
        updateView();
    }

    private void updateView() {
        // Camera index
        cameraIDSpinner.setAdapter(new ArrayAdapter<>(this,
                R.layout.spinner_layout, R.id.textViewSpinner, cameraOptions));
        if (cameraID == CameraBridgeViewBase.CAMERA_ID_BACK)
            cameraIDSpinner.setSelection(1);
        else if (cameraID == CameraBridgeViewBase.CAMERA_ID_FRONT)
            cameraIDSpinner.setSelection(2);
        else
            cameraIDSpinner.setSelection(0);

        // Table color
        settingsTableColor.setBackgroundColor(tableColor);
        settingsTableColor.setTextColor(getContrastColor(tableColor));
        settingsTableColor.setText(String.format("#%06X", (0xFFFFFF & tableColor)));

        // Ball color
        settingsBallColor.setBackgroundColor(ballColor);
        settingsBallColor.setTextColor(getContrastColor(ballColor));
        settingsBallColor.setText(String.format("#%06X", (0xFFFFFF & ballColor)));
    }

    private void saveSettings() {
        try {
            // Copy settings from local variables
            SettingsContainer.cameraID = this.cameraID;
            SettingsContainer.tableColor = this.tableColor;
            SettingsContainer.ballColor = this.ballColor;

            // Save settings to file
            SettingsHandler.saveSettings(HomeActivity.settingsFile, this);
            Toast.makeText(this, "Settings saved successfully",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Wrong settings provided! Nothing saved",
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "Wrong settings provided!", e);
        }
    }

    public static int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color)
                + 114 * Color.blue(color)) / 1000.0;
        return y >= 128 ? Color.BLACK: Color.WHITE;
    }
}