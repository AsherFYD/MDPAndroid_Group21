package com.example.mdp_android_grp25.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mdp_android_grp25.MainActivity;
import com.example.mdp_android_grp25.R;

import org.json.JSONException;

public class MapTabFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "MapFragment";

    private PageViewModel pageViewModel;

    Button resetMapBtn, updateButton;
    ImageButton directionChangeImageBtn, exploredImageBtn, obstacleImageBtn, clearImageBtn;
    ToggleButton setStartPointToggleBtn, setWaypointToggleBtn;
    Switch manualAutoToggleBtn;
    GridMap gridMap;

    Switch dragSwitch;
    Switch changeObstacleSwitch;
    Spinner spinner_imageID;
    Spinner spinner_imageBearing;
    private static boolean autoUpdate = false;

    static String imageID;
    static String imageBearing;
    static boolean dragStatus;
    static boolean changeObstacleStatus;

    // Declaration Variable
    // Shared Preferences
    SharedPreferences sharedPreferences;

    // Control Button
    Button moveForwardImageBtn, turnRightImageBtn, moveBackImageBtn, turnLeftImageBtn;
    ImageButton exploreResetButton, fastestResetButton;
    private static long exploreTimer, fastestTimer;
    public static ToggleButton exploreButton, fastestButton;
    TextView exploreTimeTextView, fastestTimeTextView, robotStatusTextView;

    public static MapTabFragment newInstance(int index) {
        MapTabFragment fragment = new MapTabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_map_config, container, false);

        gridMap = MainActivity.getGridMap();
        final DirectionFragment directionFragment = new DirectionFragment();

        resetMapBtn = root.findViewById(R.id.resetBtn);
        setStartPointToggleBtn = root.findViewById(R.id.startpointToggleBtn);
        setWaypointToggleBtn = root.findViewById(R.id.waypointToggleBtn);
        directionChangeImageBtn = root.findViewById(R.id.changeDirectionBtn);
        exploredImageBtn = root.findViewById(R.id.exploredImageBtn);
        obstacleImageBtn = root.findViewById(R.id.addObstacleBtn);
        clearImageBtn = root.findViewById(R.id.clearImageBtn);
        manualAutoToggleBtn = root.findViewById(R.id.autoManualSwitch);
        updateButton = root.findViewById(R.id.updateMapBtn);

        dragSwitch = root.findViewById(R.id.dragSwitch);
        changeObstacleSwitch = root.findViewById(R.id.changeObstacleSwitch);
        spinner_imageID = root.findViewById(R.id.imageIDSpinner);
        spinner_imageBearing = root.findViewById(R.id.bearingSpinner);
        spinner_imageID.setEnabled(false);
        spinner_imageBearing.setEnabled(false);

        // get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);

        // variable initialization
        //moveForwardImageBtn = MainActivity.getUpBtn();
        //turnRightImageBtn = MainActivity.getRightBtn();
        //moveBackImageBtn = MainActivity.getDownBtn();
        //turnLeftImageBtn = MainActivity.getLeftBtn();


        exploreTimeTextView = root.findViewById(R.id.exploreTimeTextView2);
        fastestTimeTextView = root.findViewById(R.id.fastestTimeTextView2);
        exploreButton = root.findViewById(R.id.exploreToggleBtn3);
        fastestButton = root.findViewById(R.id.fastestToggleBtn3);
        exploreResetButton = root.findViewById(R.id.exploreResetImageBtn2);
        fastestResetButton = root.findViewById(R.id.fastestResetImageBtn2);
        robotStatusTextView = MainActivity.getRobotStatusTextView();
        fastestTimer = 0;
        exploreTimer = 0;

        gridMap = MainActivity.getGridMap();

        // Create an ArrayAdapter using the string array and a default spinner layout
        // Specify the layout to use when the list of choices appears
        // Apply the adapter to the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.imageID_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_imageID.setAdapter(adapter);

        // Repeat for imageBearing
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this.getContext(),
                R.array.imageBearing_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_imageBearing.setAdapter(adapter2);

        spinner_imageID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> a, View v, int pos, long arg3) {
                imageID = a.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> a) {
            }
        });

        spinner_imageBearing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> a, View v, int pos, long arg3) {
                imageBearing = a.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> a) {
            }
        });

        resetMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked resetMapBtn");
                showToast("Reseting map...");
                gridMap.resetMap();
            }
        });

        // switch for dragging
        dragSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                showToast("Dragging is " + (isChecked ? "on" : "off"));
                dragStatus = isChecked;
                if (dragStatus == true) {
                    // disable imageID and imageBearing and disable setObstacle when drag is on
                    spinner_imageID.setEnabled(false);
                    spinner_imageBearing.setEnabled(false);
                    gridMap.setSetObstacleStatus(false);
                    changeObstacleSwitch.setChecked(false);
                }
            }
        });

        // switch for changing obstacle
        changeObstacleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                showToast("Changing Obstacle is " + (isChecked ? "on" : "off"));
                changeObstacleStatus = isChecked;
                if (changeObstacleStatus == true) {
                    // disable dragging, imageID and imageBearing and disable setObstacle
                    spinner_imageID.setEnabled(false);
                    spinner_imageBearing.setEnabled(false);
                    gridMap.setSetObstacleStatus(false);
                    dragSwitch.setChecked(false);
                }
            }
        });

        setStartPointToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked setStartPointToggleBtn");
                if (setStartPointToggleBtn.getText().equals("STARTING POINT"))
                    showToast("Cancelled selecting starting point");
                else if (setStartPointToggleBtn.getText().equals("CANCEL")
                        && !gridMap.getAutoUpdate()) {
                    showToast("Please select starting point");
                    gridMap.setStartCoordStatus(true);
                    gridMap.toggleCheckedBtn("setStartPointToggleBtn");
                } else
                    showToast("Please select manual mode");
                showLog("Exiting setStartPointToggleBtn");
            }
        });

        setWaypointToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked setWaypointToggleBtn");
                if (setWaypointToggleBtn.getText().equals("WAYPOINT"))
                    showToast("Cancelled selecting waypoint");
                else if (setWaypointToggleBtn.getText().equals("CANCEL")) {
                    showToast("Please select waypoint");
                    gridMap.setWaypointStatus(true);
                    gridMap.toggleCheckedBtn("setWaypointToggleBtn");
                } else
                    showToast("Please select manual mode");
                showLog("Exiting setWaypointToggleBtn");
            }
        });

        directionChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked directionChangeImageBtn");
                directionFragment.show(getActivity().getFragmentManager(),
                        "Direction Fragment");
                showLog("Exiting directionChangeImageBtn");
            }
        });

        obstacleImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked obstacleImageBtn");

                if (!gridMap.getSetObstacleStatus()) {
                    showToast("Please plot obstacles");
                    gridMap.setSetObstacleStatus(true);
                    spinner_imageID.setEnabled(true);
                    spinner_imageBearing.setEnabled(true);
                    gridMap.toggleCheckedBtn("obstacleImageBtn");
                } else if (gridMap.getSetObstacleStatus()) {
                    gridMap.setSetObstacleStatus(false);
                    spinner_imageID.setEnabled(false);
                    spinner_imageBearing.setEnabled(false);
                }

                changeObstacleSwitch.setChecked(false);
                dragSwitch.setChecked(false);
                showLog("obstacle status = " + gridMap.getSetObstacleStatus());
                showLog("Exiting obstacleImageBtn");
            }
        });

        manualAutoToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked manualAutoToggleBtn");
                if (manualAutoToggleBtn.getText().equals("MANUAL")) {
                    try {
                        gridMap.setAutoUpdate(true);
                        autoUpdate = true;
                        gridMap.toggleCheckedBtn("None");
                        updateButton.setClickable(false);
                        updateButton.setTextColor(Color.GRAY);
                        manualAutoToggleBtn.setText("AUTO");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("AUTO mode");
                } else if (manualAutoToggleBtn.getText().equals("AUTO")) {
                    try {
                        gridMap.setAutoUpdate(false);
                        autoUpdate = false;
                        gridMap.toggleCheckedBtn("None");
                        updateButton.setClickable(true);
                        updateButton.setTextColor(Color.BLACK);
                        manualAutoToggleBtn.setText("MANUAL");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("MANUAL mode");
                }
                showLog("Exiting manualAutoToggleBtn");
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked updateButton");

                gridMap.imageBearings.get(9)[5] = "South";
                gridMap.imageBearings.get(15)[15] = "South";
                gridMap.imageBearings.get(14)[7] = "West";
                gridMap.imageBearings.get(4)[15] = "West";
                gridMap.imageBearings.get(9)[12] = "East";
                gridMap.setObstacleCoord(5 + 1, 9 + 1);
                gridMap.setObstacleCoord(15 + 1, 15 + 1);
                gridMap.setObstacleCoord(7 + 1, 14 + 1);
                gridMap.setObstacleCoord(15 + 1, 4 + 1);
                gridMap.setObstacleCoord(12 + 1, 9 + 1);

                gridMap.invalidate();
                showLog("Exiting updateButton");
            }
        });

        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked exploreToggleBtn");
                ToggleButton exploreToggleBtn = (ToggleButton) v;

                if (exploreToggleBtn.getText().equals("Imagerec START")) { //press stop
                    showToast("Auto Movement/ImageRecog timer stop!");
                    robotStatusTextView.setText("Auto Movement Stopped");
                    timerHandler.removeCallbacks(timerRunnableExplore);
                }
                else if (exploreToggleBtn.getText().equals("STOP")) { //press wk8 start
                    String msg = gridMap.getObstacles();
                    MainActivity.printMessage(msg); //send obstacles to robot
                    MainActivity.stopTimerFlag = false;
                    showToast("Auto Movement/ImageRecog timer start!");
                    robotStatusTextView.setText("Auto Movement Started");
                    exploreTimer = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnableExplore, 0);
                }
                else {
                    showToast("Else statement: " + exploreToggleBtn.getText());
                }
                showLog("Exiting exploreToggleBtn");
            }
        });

        /**
         * for week 9 start button
         */
        fastestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked fastestToggleBtn");
                ToggleButton fastestToggleBtn = (ToggleButton) v;
                if (fastestToggleBtn.getText().equals("Fastest START")) { //press stop for wk9 start
                    showToast("Fastest car timer stop!");
                    robotStatusTextView.setText("Fastest Car Stopped");
                    timerHandler.removeCallbacks(timerRunnableFastest);
                }
                else if (fastestToggleBtn.getText().equals("STOP")) { //press start wk 9
                    showToast("Fastest car timer start!");
                    try {
                        MainActivity.printMessage("STM|G"); //send message for fastest path
                    } catch (Exception e) {
                        showLog(e.getMessage());
                    }
                    MainActivity.stopWk9TimerFlag = false;
                    robotStatusTextView.setText("Fastest Car Started");
                    fastestTimer = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnableFastest, 0);
                }
                else
                    showToast(fastestToggleBtn.getText().toString());
                showLog("Exiting fastestToggleBtn");
            }
        });

        exploreResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked exploreResetImageBtn");
                showToast("Resetting exploration time...");
                exploreTimeTextView.setText("00:00");
                robotStatusTextView.setText("Not Available");
                if(exploreButton.isChecked())
                    exploreButton.toggle();
                timerHandler.removeCallbacks(timerRunnableExplore);
                showLog("Exiting exploreResetImageBtn");
            }
        });

        fastestResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked fastestResetImageBtn");
                showToast("Resetting fastest time...");
                fastestTimeTextView.setText("00:00");
                robotStatusTextView.setText("Not Available");
                if(fastestButton.isChecked())
                    fastestButton.toggle();
                timerHandler.removeCallbacks(timerRunnableFastest);
                showLog("Exiting fastestResetImageBtn");
            }
        });

        return root;
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    // Timer
    public static Handler timerHandler = new Handler();

    public Runnable timerRunnableExplore = new Runnable() {
        @Override
        public void run() {
            long millisExplore = System.currentTimeMillis() - exploreTimer;
            int secondsExplore = (int) (millisExplore / 1000);
            int minutesExplore = secondsExplore / 60;
            secondsExplore = secondsExplore % 60;

            if (MainActivity.stopTimerFlag == false) {
                exploreTimeTextView.setText(String.format("%02d:%02d", minutesExplore,
                        secondsExplore));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    public Runnable timerRunnableFastest = new Runnable() {
        @Override
        public void run() {
            long millisFastest = System.currentTimeMillis() - fastestTimer;
            int secondsFastest = (int) (millisFastest / 1000);
            int minutesFastest = secondsFastest / 60;
            secondsFastest = secondsFastest % 60;

            if (MainActivity.stopWk9TimerFlag == false) {
                fastestTimeTextView.setText(String.format("%02d:%02d", minutesFastest,
                        secondsFastest));
                timerHandler.postDelayed(this, 500);
            }
        }
    };


}